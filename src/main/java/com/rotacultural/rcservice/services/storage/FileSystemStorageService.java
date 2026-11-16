package com.rotacultural.rcservice.services.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadRoot;

    public FileSystemStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível inicializar o diretório de uploads", e);
        }
    }

    @Override
    public String store(MultipartFile file, String folder) {
        validateImage(file);

        String safeFolder = sanitizeFolder(folder);
        String extension = extractExtension(file.getOriginalFilename(), file.getContentType());
        String filename = UUID.randomUUID() + extension;

        try {
            Path targetDir = uploadRoot.resolve(safeFolder).normalize();
            if (!targetDir.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diretório de destino inválido");
            }
            Files.createDirectories(targetDir);

            Path destination = targetDir.resolve(filename).normalize();
            if (!destination.startsWith(targetDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo inválido");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + safeFolder.replace("\\", "/") + "/" + filename;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a imagem enviada"
            );
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        String relative = fileUrl.substring("/uploads/".length());
        Path target = uploadRoot.resolve(relative).normalize();
        if (!target.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Falha na limpeza física não deve impedir a remoção do registro
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione ao menos uma imagem válida");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada imagem deve ter no máximo 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato inválido. Envie imagens JPG, PNG ou WEBP"
            );
        }

        String extension = extractExtension(file.getOriginalFilename(), contentType);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Extensão de arquivo não permitida. Utilize .jpg, .jpeg, .png ou .webp"
            );
        }

        if (!hasValidImageSignature(file)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Conteúdo do arquivo não corresponde a uma imagem JPG, PNG ou WEBP válida"
            );
        }
    }

    private boolean hasValidImageSignature(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(12);
            if (header.length < 4) {
                return false;
            }
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
                return true;
            }
            // WEBP: RIFF....WEBP
            return header.length >= 12
                    && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        } catch (IOException e) {
            return false;
        }
    }

    private String sanitizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            return "general";
        }
        return folder.replace("..", "").replaceAll("[^a-zA-Z0-9/_-]", "");
    }

    private String extractExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                String ext = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ALLOWED_EXTENSIONS.contains(ext)) {
                    return ext;
                }
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }
}
