package common.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.private-key-path:private.pem}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path:public.pem}")
    private String publicKeyPath;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String key;
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            key = new String(resource.getInputStream().readAllBytes());
        } else {
            key = new String(Files.readAllBytes(Paths.get(path)));
        }
        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                  .replace("-----END PRIVATE KEY-----", "")
                  .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        String key;
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            key = new String(resource.getInputStream().readAllBytes());
        } else {
            key = new String(Files.readAllBytes(Paths.get(path)));
        }
        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PrivateKey getPrivateKey() {
        if (privateKey == null) {
            try {
                privateKey = loadPrivateKey(privateKeyPath);
            } catch (Exception e) {
                log.error("加载私钥失败", e);
                throw new RuntimeException("加载私钥失败", e);
            }
        }
        return privateKey;
    }

    private PublicKey getPublicKey() {
        if (publicKey == null) {
            try {
                publicKey = loadPublicKey(publicKeyPath);
            } catch (Exception e) {
                log.error("加载公钥失败", e);
                throw new RuntimeException("加载公钥失败", e);
            }
        }
        return publicKey;
    }

    public String generateToken(Map<String, Object> claims) {
        return createToken(claims);
    }

    public String generateToken(Map<String, Object> claims, Long expiration) {
        return createToken(claims, expiration);
    }

    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setSubject("jwt")
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    private String createToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("Token已过期");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token unsupported: {}", e.getMessage());
            throw new RuntimeException("不支持的Token");
        } catch (MalformedJwtException e) {
            log.error("JWT token malformed: {}", e.getMessage());
            throw new RuntimeException("Token格式错误");
        } catch (IllegalArgumentException e) {
            log.error("JWT token illegal: {}", e.getMessage());
            throw new RuntimeException("非法Token");
        }
    }

    public Boolean getAdminFlagFromToken(String token){
        Claims claims = parseToken(token);
        return claims.get("adminFlag", Boolean.class);
    }



    public String getUserCodeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("sub", String.class);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    public String getFirstNameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("firstname", String.class);
    }

    public String getLastNameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("lastname", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 获取Token剩余有效期（毫秒）
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            return -1;
        }
    }
}
