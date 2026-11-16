package com.rotacultural.rcservice.services.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String folder);

    StoredResource load(String fileUrl);

    void delete(String fileUrl);

    default String providerName() {
        return "local";
    }
}
