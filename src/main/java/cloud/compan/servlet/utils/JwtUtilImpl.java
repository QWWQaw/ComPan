// package cloud.compan.servlet.utils;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import com.google.inject.Singleton;
// import javax.crypto.SecretKey;
// import java.util.Date;

// @Singleton
// public class JwtUtilImpl implements JwtUtil {
    
//     private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//     private final long expiration = 86400000; // 24小时
    
//     @Override
//     public String generateToken(String obj) {
//         return Jwts.builder()
//                 .setSubject(obj)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                 .signWith(key)
//                 .compact();
//     }
    
//     @Override
//     public Claims validateToken(String token) {
//         try {
//             return Jwts.parser()
//                     .verifyWith(key)
//                     .build()
//                     .parseSignedClaims(token)
//                     .getPayload();
//         } catch (Exception e) {
//             throw new RuntimeException("Invalid JWT token", e);
//         }
//     }
    
// }
package cloud.compan.servlet.utils;

import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Value;
import cloud.compan.servlet.config.ConfigLoader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT工具类实现
 */
@Singleton
public class JwtUtilImpl implements JwtUtil{

    @Value("jwt.secret")
    private String secretKey; 
    
    @Value("jwt.expiration.seconds")
    private long EXPIRATION_TIME;

    private final SecretKey SECRET_KEY;

    public JwtUtilImpl(){
        ConfigLoader.inject(this);
        System.out.println("======= Trying to construct JwtUtilImpl =======");
        if (this.secretKey == null || this.secretKey.isEmpty()) {
            throw new IllegalStateException("JWT secret key was not loaded by ConfigLoader. Check application.properties.");
        }
        // 将秒转换为毫秒
        this.EXPIRATION_TIME = this.EXPIRATION_TIME * 1000;
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        SECRET_KEY = Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * 构造函数，用于测试
     * @param secretKey JWT secret key
     * @param EXPIRATION_TIME JWT expiration time
     */
    public JwtUtilImpl(String secretKey, long EXPIRATION_TIME){
        this.secretKey = secretKey;
        this.EXPIRATION_TIME = EXPIRATION_TIME;
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        SECRET_KEY = Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * 生成JWT token
     * @param obj 需要编码的对象
     * @return 生成的JWT token
     */
    @Override
    public String generateToken(String obj) {
        if(secretKey == null || secretKey.isEmpty()){
            throw new IllegalArgumentException("Secret key is not set");
        }
        if(EXPIRATION_TIME <= 0){
            throw new IllegalArgumentException("Expiration time is not set");
        }
        if(SECRET_KEY == null){
            throw new IllegalArgumentException("Secret key is not set");
        }
        return Jwts.builder()
                .claims()
                    .add("sub", obj)
                    .add("exp", new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .and()
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 验证JWT token
     * @param token JWT token
     * @return Claims
     */
    @Override
    public Claims validateToken(String token) {
        if(secretKey == null || secretKey.isEmpty() || SECRET_KEY == null){
            throw new IllegalStateException("JWT secret key not initialized.");
        }
        
        // 检查token是否为空或null
        if(token == null || token.trim().isEmpty()){
            return null;
        }
        
        try {
            JwtParser parser = Jwts.parser()
                                   .verifyWith(SECRET_KEY)  // 设置签名密钥并自动验证签名
                                   .build();

            return parser.parseSignedClaims(token).getPayload(); // 返回 Claims（payload）

        } catch (JwtException e) {
            // Token 无效或签名失败或已过期
            System.out.println("Invalid JWT: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            // Token格式错误（如空字符串）
            System.out.println("Invalid token format: " + e.getMessage());
            return null;
        }
    }


}

