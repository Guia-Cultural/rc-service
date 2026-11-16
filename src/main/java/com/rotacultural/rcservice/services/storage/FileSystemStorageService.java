package com.rotacultural.rcservice.services.storage;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

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
        ImageFileValidator.validateImage(file);

        String safeFolder = ImageFileValidator.sanitizeFolder(folder);
        String extension = ImageFileValidator.extractExtension(file.getOriginalFilename(), file.getContentType());
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

            return ImageFileValidator.UPLOADS_PREFIX + safeFolder.replace("\\", "/") + "/" + filename;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a imagem enviada"
            );
        }
    }

    @Override
    public StoredResource load(String fileUrl) {
        String key = ImageFileValidator.extractStorageKey(fileUrl);
        if (key == null) {
            throw new EntityNotFoundException("Imagem não encontrada");
        }
        Path target = uploadRoot.resolve(key).normalize();
        if (!target.startsWith(uploadRoot) || !Files.isRegularFile(target)) {
            throw new EntityNotFoundException("Imagem não encontrada");
        }
        try {
            byte[] content = Files.readAllBytes(target);
            String probed = Files.probeContentType(target);
            String contentType = probed != null ? probed : ImageFileValidator.inferContentType(key);
            return new StoredResource(content, contentType, target.getFileName().toString());
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível ler a imagem solicitada"
            );
        }
    }

    @Override
    public void delete(String fileUrl) {
        String key = ImageFileValidator.extractStorageKey(fileUrl);
        if (key == null) {
            return;
        }
        Path target = uploadRoot.resolve(key).normalize();
        if (!target.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Falha na limpeza física não deve impedir a remoção do registro
        }
    }

    @Override
    public String providerName() {
        return "local";
    }
}
