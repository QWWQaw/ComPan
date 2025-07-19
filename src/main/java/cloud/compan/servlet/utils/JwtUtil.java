package cloud.compan.servlet.utils;

import io.jsonwebtoken.Claims;

public interface JwtUtil {
    String generateToken(String obj);
    Claims validateToken(String token);
}
