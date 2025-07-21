package cloud.compan.servlet.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Claims;

/**
 * JWT工具类测试
 */
@DisplayName("JWT工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckp1bml0VGVzdGluZ09ubHlEb05vdFVzZUluUHJvZHVjdGlvbg==";
    private static final long TEST_EXPIRATION = 3600000; // 1小时

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtilImpl(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("测试JWT token生成")
    void testGenerateToken() {
        String subject = "testuser";
        String token = jwtUtil.generateToken(subject);
        
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.split("\\.").length == 3, "JWT token should have 3 parts");
        
        System.out.println("Generated token: " + token);
    }

    @Test
    @DisplayName("测试JWT token验证")
    void testValidateToken() {
        String subject = "testuser";
        String token = jwtUtil.generateToken(subject);
        
        Claims claims = jwtUtil.validateToken(token);
        
        assertNotNull(claims, "Claims should not be null");
        assertEquals(subject, claims.getSubject(), "Subject should match");
        assertNotNull(claims.getExpiration(), "Expiration should not be null");
        
        System.out.println("Token validation successful for subject: " + claims.getSubject());
    }

    @Test
    @DisplayName("测试无效token验证")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        Claims claims = jwtUtil.validateToken(invalidToken);
        
        assertNull(claims, "Claims should be null for invalid token");
        System.out.println("Invalid token correctly rejected");
    }

    @Test
    @DisplayName("测试空字符串token验证")
    void testValidateEmptyToken() {
        Claims claims = jwtUtil.validateToken("");
        
        assertNull(claims, "Claims should be null for empty token");
        System.out.println("Empty token correctly rejected");
    }

    @Test
    @DisplayName("测试null token验证")
    void testValidateNullToken() {
        Claims claims = jwtUtil.validateToken(null);
        
        assertNull(claims, "Claims should be null for null token");
        System.out.println("Null token correctly rejected");
    }

    @Test
    @DisplayName("测试不同subject的token生成和验证")
    void testDifferentSubjects() {
        String[] subjects = {"user1", "user2", "admin", "guest"};
        
        for (String subject : subjects) {
            String token = jwtUtil.generateToken(subject);
            Claims claims = jwtUtil.validateToken(token);
            
            assertNotNull(claims, "Claims should not be null for subject: " + subject);
            assertEquals(subject, claims.getSubject(), "Subject should match for: " + subject);
            
            System.out.println("✓ Token for subject '" + subject + "' generated and validated successfully");
        }
    }

    @Test
    @DisplayName("测试token过期时间")
    void testTokenExpiration() {
        String subject = "testuser";
        String token = jwtUtil.generateToken(subject);
        
        Claims claims = jwtUtil.validateToken(token);
        assertNotNull(claims, "Claims should not be null");
        
        long currentTime = System.currentTimeMillis();
        long expirationTime = claims.getExpiration().getTime();
        
        assertTrue(expirationTime > currentTime, "Token should not be expired");
        assertTrue(expirationTime <= currentTime + TEST_EXPIRATION, "Token expiration should be within expected range");
        
        System.out.println("Token expiration time: " + claims.getExpiration());
        System.out.println("Current time: " + new java.util.Date(currentTime));
    }
}
