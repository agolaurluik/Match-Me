package backend.utilities.HelperMethods;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import backend.matching.dto.UserMatchDetailDTO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.TreeSet;

@Component
public class MatchSigningUtility {

    private final byte[] secretKey;

    public MatchSigningUtility(
            @Value("${match.signing.secret}") String secret) {
        this.secretKey = Base64.getDecoder().decode(secret);

        if (secretKey.length < 32) {
            throw new IllegalArgumentException(
                    "Match signing key must be at least 32 bytes");
        }
    }

    public String signMatchData(UserMatchDetailDTO dto) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hmac(canonicalBytes(dto)));
    }

    public boolean verifyMatchData(UserMatchDetailDTO dto) {
        if (dto == null || dto.getSignature() == null) {
            return false;
        }

        try {
            byte[] supplied = Base64.getUrlDecoder()
                    .decode(dto.getSignature());

            byte[] expected = hmac(canonicalBytes(dto));

            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException e) {
            // Malformed Base64 signature.
            return false;
        }
    }

    private byte[] hmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to generate match signature", e);
        }
    }

    private byte[] canonicalBytes(UserMatchDetailDTO dto) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            try (DataOutputStream out = new DataOutputStream(buffer)) {
                writeString(out, "user-match-signature-v1");

                writeLong(out, dto.getViewerId());
                writeLong(out, dto.getCandidateId());

                out.writeLong(
                        Double.doubleToLongBits(dto.getMatchPercentage()));

                writeSet(out, dto.getSharedInterestsSet());
                writeSet(out, dto.getSharedPersonalitiesSet());

                out.writeLong(Double.doubleToLongBits(dto.getDistance()));

                Instant createdAt = dto.getCreatedAt();
                out.writeBoolean(createdAt != null);

                if (createdAt != null) {
                    out.writeLong(createdAt.getEpochSecond());
                    out.writeInt(createdAt.getNano());
                }
            }

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to serialize match data", e);
        }
    }

    private void writeLong(DataOutputStream out, Long value)
            throws IOException {
        out.writeBoolean(value != null);

        if (value != null) {
            out.writeLong(value);
        }
    }

    private void writeString(DataOutputStream out, String value)
            throws IOException {
        out.writeBoolean(value != null);

        if (value != null) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    private void writeSet(DataOutputStream out, Set<String> values)
            throws IOException {
        out.writeBoolean(values != null);

        if (values == null) {
            return;
        }

        // Sort to ensure deterministic serialization.
        Set<String> sorted = new TreeSet<>(values);
        out.writeInt(sorted.size());

        for (String value : sorted) {
            writeString(out, value);
        }
    }

    public void validateMatchData(Long authenticatedUserId, UserMatchDetailDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Match data is required");
        }

        if (dto.getViewerId() == null
                || !dto.getViewerId().equals(authenticatedUserId)) {
            System.out
                    .println(
                            "Dto viewer ID: " + dto.getViewerId() + " and authenticatedUserID: " + authenticatedUserId);
            throw new IllegalArgumentException(
                    "Viewer does not match authenticated user");
        }

        if (dto.getCandidateId() == null
                || dto.getCandidateId().equals(dto.getViewerId())) {
            throw new IllegalArgumentException(
                    "Invalid candidate");
        }

        if (!Double.isFinite(dto.getMatchPercentage())
                || dto.getMatchPercentage() < 0
                || dto.getMatchPercentage() > 100) {
            throw new IllegalArgumentException(
                    "Invalid match percentage");
        }

        if (!Double.isFinite(dto.getDistance())
                || dto.getDistance() < 0) {
            throw new IllegalArgumentException(
                    "Invalid distance");
        }

        if (dto.getCreatedAt() == null) {
            throw new IllegalArgumentException(
                    "Recommendation timestamp is required");
        }

        Instant now = Instant.now();

        if (dto.getCreatedAt().isAfter(now)
                || !dto.getCreatedAt().isAfter(
                        now.minus(Duration.ofMinutes(15)))) {
            throw new IllegalArgumentException(
                    "Recommendation has expired or has an invalid timestamp");
        }

        if (!verifyMatchData(dto)) {
            throw new IllegalArgumentException(
                    "Invalid match signature");
        }
    }
}
