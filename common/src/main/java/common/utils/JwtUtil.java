package common.utils;

import com.github.f4b6a3.ulid.UlidCreator;
import common.common.AccessPayload;
import common.common.RefreshPayload;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.private-key-path:private.pem}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path:public.pem}")
    private String publicKeyPath;

    public Long accessExpiration = TimeUnit.HOURS.toMillis(2);  // 认证token过期时间

    public Long remExpiration = TimeUnit.DAYS.toMillis(7);  // 七天免登录的刷新token过期时间

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${jwt.iss:2240709249}")
    public String ISS;

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
        Date expiryDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
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

    @Description("生成认证token负载")
    public Map<String, Object> setAccessClaims(AccessPayload payload){
        Map<String, Object> accessClaim = new HashMap<>();
        accessClaim.put("iss",ISS); // 签发机关
        accessClaim.put("sub",payload.getUUid()); // 主题（用户ID）
        accessClaim.put("aud","trip-api"); // 接收方
        accessClaim.put("role",payload.getRole()); // 角色
        accessClaim.put("email",payload.getEmail()); // 邮箱
        accessClaim.put("jti",payload.getJit()); // jwt标识
        accessClaim.put("userName",encodeChinese(payload.getUserName())); // 用户名
        accessClaim.put("avatar",payload.getAvatar()); // 头像
        accessClaim.put("status",payload.getStatus());  // 用户状态
        accessClaim.put("nickName",encodeChinese(payload.getNickName())); // 用户昵称
        accessClaim.put("tokenType","access");
        return  accessClaim;
    }

    /**
     * 对包含中文的字符串进行 Base64 编码
     */
    private String encodeChinese(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // 检查是否包含中文字符
        if (value.matches(".*[\\u4e00-\\u9fa5].*")) {
            return Base64.getEncoder().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return value;
    }

    @Description("生成刷新token负载")
    public Map<String, Object> setRefreshClaims(RefreshPayload payload){
        Map<String, Object> refreshClaim = new HashMap<>();
        refreshClaim.put("iss",ISS); // 签发机关
        refreshClaim.put("sub",payload.getUUid()); // 主题（用户ID）
        refreshClaim.put("aud","trip-auth"); // 接收方
        refreshClaim.put("jti",payload.getJit()); // jwt标识
        refreshClaim.put("userName",payload.getUserName()); // 用户名
        refreshClaim.put("tokenType","refresh");
//        refreshClaim.put("deviceId",""); // 设备标识
        return  refreshClaim;
    }

    public String getUUidFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("sub", String.class);
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
