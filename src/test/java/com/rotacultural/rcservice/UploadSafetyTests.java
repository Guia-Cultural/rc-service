package com.rotacultural.rcservice;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.rotacultural.rcservice.services.storage.AzureBlobStorageService;
import com.rotacultural.rcservice.services.storage.ImageFileValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UploadSafetyTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

    @Test
    void validImageBytesMustNotBypassForbiddenExtension() {
        var file = new MockMultipartFile("files", "payload.exe", "image/png", PNG);
        assertThatThrownBy(() -> ImageFileValidator.validateImage(file))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void headerFailureAfterAzureUploadMustRemoveUnreferencedBlob() {
        var container = mock(BlobContainerClient.class);
        var blob = mock(BlobClient.class);
        when(container.exists()).thenReturn(true);
        when(container.getBlobClient(anyString())).thenReturn(blob);
        doThrow(new IllegalStateException("header failure")).when(blob).setHttpHeaders(any(BlobHttpHeaders.class));
        var storage = new AzureBlobStorageService(container);
        var file = new MockMultipartFile("files", "photo.png", "image/png", PNG);
        assertThatThrownBy(() -> storage.store(file, "places/1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(blob).deleteIfExists();
    }
}
