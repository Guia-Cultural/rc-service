package com.rotacultural.rcservice.services.storage;

import com.rotacultural.rcservice.entities.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class MediaAccessService {

    private static final long SIGNED_URL_TTL_SECONDS = 1800L;
    private final SecretKey jwtSecretKey;

    public MediaAccessService(SecretKey jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public String toAccessibleUrl(String canonicalUrl, Status placeStatus) {
        String cleanUrl = ImageFileValidator.stripQuery(canonicalUrl);
        if (!StringUtils.hasText(cleanUrl) || !cleanUrl.startsWith(ImageFileValidator.UPLOADS_PREFIX)) {
            return canonicalUrl;
        }
        if (placeStatus == Status.APROVADO) {
            return cleanUrl;
        }
        long exp = Instant.now().getEpochSecond() + SIGNED_URL_TTL_SECONDS;
        String sig = sign(cleanUrl, exp);
        return cleanUrl + "?exp=" + exp + "&sig=" + sig;
    }

    public boolean isValidSignature(String canonicalUrl, Long exp, String sig) {
        if (!StringUtils.hasText(canonicalUrl) || exp == null || !StringUtils.hasText(sig)) {
            return false;
        }
        if (exp < Instant.now().getEpochSecond()) {
            return false;
        }
        String expected = sign(canonicalUrl, exp);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                sig.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sign(String canonicalUrl, long exp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(jwtSecretKey);
            byte[] raw = mac.doFinal((canonicalUrl + ":" + exp).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erro ao assinar URL temporária de mídia", e);
        }
    }
}
