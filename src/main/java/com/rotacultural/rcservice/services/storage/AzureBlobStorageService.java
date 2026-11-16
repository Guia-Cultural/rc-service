package com.rotacultural.rcservice.services.storage;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient containerClient;

    @Autowired
    public AzureBlobStorageService(
            @Value("${azure.storage.account-name:}") String accountName,
            @Value("${azure.storage.container-name:places-media}") String containerName,
            @Value("${azure.storage.connection-string:}") String connectionString
    ) {
        if (!StringUtils.hasText(containerName)) {
            throw new IllegalStateException("AZURE_STORAGE_CONTAINER_NAME deve ser configurado");
        }

        BlobServiceClient serviceClient;
        if (StringUtils.hasText(connectionString)) {
            serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString.trim())
                    .buildClient();
        } else {
            if (!StringUtils.hasText(accountName)) {
                throw new IllegalStateException(
                        "AZURE_STORAGE_ACCOUNT_NAME deve ser configurado quando APP_STORAGE_PROVIDER=azure"
                );
            }
            String endpoint = "https://" + accountName.trim() + ".blob.core.windows.net";
            serviceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
        }

        this.containerClient = serviceClient.getBlobContainerClient(containerName.trim());
    }

    public AzureBlobStorageService(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    @Override
    public String store(MultipartFile file, String folder) {
        ImageFileValidator.validateImage(file);

        String safeFolder = ImageFileValidator.sanitizeFolder(folder);
        String extension = ImageFileValidator.extractExtension(file.getOriginalFilename(), file.getContentType());
        String blobKey = safeFolder + "/" + UUID.randomUUID() + extension;

        try {
            if (!containerClient.exists()) {
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(blobKey);
            byte[] bytes = file.getBytes();
            blobClient.upload(BinaryData.fromBytes(bytes), true);

            String contentType = StringUtils.hasText(file.getContentType())
                    ? file.getContentType()
                    : ImageFileValidator.inferContentType(blobKey);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

            // Salva apenas referência canônica interna (nunca URL SAS temporária no banco)
            return ImageFileValidator.UPLOADS_PREFIX + blobKey;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível enviar a imagem para o Azure Blob Storage"
            );
        }
    }

    @Override
    public StoredResource load(String fileUrl) {
        String blobKey = ImageFileValidator.extractStorageKey(fileUrl);
        if (blobKey == null) {
            throw new EntityNotFoundException("Imagem não encontrada");
        }

        BlobClient blobClient = containerClient.getBlobClient(blobKey);
        if (!Boolean.TRUE.equals(blobClient.exists())) {
            throw new EntityNotFoundException("Imagem não encontrada");
        }

        byte[] content = blobClient.downloadContent().toBytes();
        String contentType = null;
        try {
            contentType = blobClient.getProperties().getContentType();
        } catch (RuntimeException ignored) {
            // fallback para detecção pela extensão
        }
        if (!StringUtils.hasText(contentType)) {
            contentType = ImageFileValidator.inferContentType(blobKey);
        }
        String filename = blobKey.contains("/") ? blobKey.substring(blobKey.lastIndexOf('/') + 1) : blobKey;
        return new StoredResource(content, contentType, filename);
    }

    @Override
    public void delete(String fileUrl) {
        String blobKey = ImageFileValidator.extractStorageKey(fileUrl);
        if (blobKey == null) {
            return;
        }
        try {
            containerClient.getBlobClient(blobKey).deleteIfExists();
        } catch (RuntimeException ignored) {
            // Falha na remoção remota não deve impedir exclusão lógica
        }
    }

    @Override
    public String providerName() {
        return "azure";
    }
}
