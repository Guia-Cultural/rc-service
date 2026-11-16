package com.rotacultural.rcservice.services.storage;

public record StoredResource(byte[] content, String contentType, String filename) {
}
