package com.rotacultural.rcservice.services.storage;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

public final class ImageFileValidator {

    public static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    public static final String UPLOADS_PREFIX = "/uploads/";

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private ImageFileValidator() {
    }

    public static void validateImage(MultipartFile file) {
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

    public static boolean hasValidImageSignature(MultipartFile file) {
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

    public static String sanitizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            return "general";
        }
        String sanitized = folder.replace("..", "").replaceAll("[^a-zA-Z0-9/_-]", "");
        while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized.isEmpty() ? "general" : sanitized;
    }

    public static String extractExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                String ext = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ALLOWED_EXTENSIONS.contains(ext)) {
                    return ext;
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Extensão de arquivo não permitida. Utilize .jpg, .jpeg, .png ou .webp");
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

    public static String extractStorageKey(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        String withoutQuery = stripQuery(fileUrl.trim());
        if (!withoutQuery.startsWith(UPLOADS_PREFIX)) {
            return null;
        }
        String key = withoutQuery.substring(UPLOADS_PREFIX.length());
        if (key.isBlank() || key.contains("..")) {
            return null;
        }
        return key;
    }

    public static String stripQuery(String url) {
        if (url == null) {
            return null;
        }
        int qIdx = url.indexOf('?');
        return qIdx >= 0 ? url.substring(0, qIdx) : url;
    }

    public static String inferContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
