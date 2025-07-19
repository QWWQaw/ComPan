package cloud.compan.servlet.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtUtilImplTest {

    private JwtUtilImpl jwtUtil;

    private final String secretKey = "aV93YW50X3RvX2dvX2hvbWVfaV93YW50X3N1bW1lcl92YWNhdGlvbl9pX3dhbnRfbGliZXJ0eQ==";

    private final long EXPIRATION_TIME = 3600000;

    @BeforeAll
    void setUp() {
        // Since JwtUtilImpl loads config in constructor, we instantiate it here
        // so it picks up the test application.properties
        jwtUtil = new JwtUtilImpl(secretKey, EXPIRATION_TIME);
    }

    @Test
    void testGenerateToken_Success() {
        String subject = "testUser";
        String token = jwtUtil.generateToken(subject);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken_Success() {
        String subject = "testUser";
        String token = jwtUtil.generateToken(subject);
        Claims claims = jwtUtil.validateToken(token);
        assertNotNull(claims);
        assertEquals(subject, claims.getSubject());
    }

    @Test
    void testValidateToken_ExpiredToken() throws Exception {
        // Use reflection to temporarily change expiration time for this test
        Field expirationField = JwtUtilImpl.class.getDeclaredField("EXPIRATION_TIME");
        expirationField.setAccessible(true);
        long originalExpiration = (long) expirationField.get(jwtUtil);
        
        // Set expiration to 1 millisecond
        expirationField.set(jwtUtil, 1L);

        String token = jwtUtil.generateToken("testUser");
        
        // Wait for the token to expire
        TimeUnit.MILLISECONDS.sleep(5);

        // This should return null as per current implementation
        assertNull(jwtUtil.validateToken(token));

        // Restore original expiration time
        expirationField.set(jwtUtil, originalExpiration);
    }


    @Test
    void testValidateToken_InvalidSignature() {
        // Generate a token with the default utility
        String token = jwtUtil.generateToken("testUser");

        // Create another instance with a different key to simulate an invalid signature
        JwtUtilImpl otherJwtUtil = new JwtUtilImpl(secretKey, EXPIRATION_TIME) {
            @Override
            public Claims validateToken(String t) {
                // We can't easily change the final SECRET_KEY, so we'll just check against a bad token
                // Let's tamper with the token
                String[] parts = t.split("\\.");
                String tamperedPayload = Base64.getEncoder().encodeToString("{\"sub\":\"hacker\"}".getBytes());
                String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];
                return super.validateToken(tamperedToken);
            }
        };
        
        // The current implementation catches the exception and returns null.
        // A more robust implementation might throw the exception.
        assertNull(jwtUtil.validateToken(token + "invalid"));
    }

    @Test
    void testValidateToken_MalformedToken() {
        String malformedToken = "this.is.not.a.valid.jwt";
        assertNull(jwtUtil.validateToken(malformedToken));
    }
}
