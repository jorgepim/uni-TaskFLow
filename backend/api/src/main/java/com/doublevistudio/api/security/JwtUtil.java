package com.doublevistudio.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String email, List<String> roles) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + expirationMs);

        String rolesClaim = String.join(",", roles);

        return io.jsonwebtoken.Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("roles", rolesClaim)
                .setIssuedAt(new Date(now))
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    // Validate signature and expiration
    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            // verify signature HMAC-SHA256
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            String signingInput = header + "." + payload;
            byte[] sig = hmac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
            if (!computed.equals(signature)) return false;

            // parse payload and check exp
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payloadJson);
            if (node.has("exp")) {
                long expVal = node.get("exp").asLong();
                long expMillis = expVal;
                // exp might be seconds; heuristics: if value looks like seconds (<= 10^10), multiply by 1000
                if (expVal < 10000000000L) expMillis = expVal * 1000L;
                if (Instant.now().toEpochMilli() > expMillis) return false;
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payload = parts[1];
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payloadJson);
            if (node.has("sub")) {
                return Long.valueOf(node.get("sub").asText());
            }
            if (node.has("userId")) {
                return Long.valueOf(node.get("userId").asText());
            }
            // if subject present as numeric in 'sub' or as 'userId' handled above
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<String> getRolesFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return List.of();
            String payload = parts[1];
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payloadJson);
            if (node.has("roles")) {
                JsonNode r = node.get("roles");
                if (r.isTextual()) {
                    String rolesStr = r.asText();
                    if (rolesStr.isEmpty()) return List.of();
                    String[] arr = rolesStr.split(",");
                    return java.util.Arrays.asList(arr);
                }
                if (r.isArray()) {
                    List<String> out = new ArrayList<>();
                    for (JsonNode rn : r) out.add(rn.asText());
                    return out;
                }
            }
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }
}
