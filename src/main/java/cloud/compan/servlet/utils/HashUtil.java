package cloud.compan.servlet.utils;

import com.google.inject.Singleton;  
import org.mindrot.jbcrypt.BCrypt;  

import java.io.IOException;  
import java.io.InputStream;  
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  

/**  
 * 一个安全的哈希工具服务，由 Guice 管理。  
 * 提供了针对文件内容和用户密码的、不同安全策略的哈希方法。  
 */  
@Singleton  
public class HashUtil {  
    
    private static final int WORK_FACTOR = 10;

    // --- 密码哈希部分 (使用 BCrypt) ---  

    /**  
     * 对明文密码进行哈希处理。  
     * BCrypt 内部会自动生成一个随机的盐 (salt) 并将其包含在最终的哈希字符串中。  
     * @param plainTextPassword 用户输入的明文密码  
     * @return 一个安全的、加盐的哈希字符串   
     */  
    public String hashPassword(String plainTextPassword) {    
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));  
    }  

    /**  
     * 验证一个明文密码是否与一个已哈希的密码匹配。  
     * @param plainTextPassword 用户在登录时输入的明文密码  
     * @param hashedPasswordFromDb 从数据库中取出的哈希字符串  
     * @return 如果密码匹配则返回 true，否则返回 false  
     */  
    public boolean checkPassword(String plainTextPassword, String hashedPasswordFromDb) {  
        if (plainTextPassword == null || hashedPasswordFromDb == null) {  
            return false;  
        }  
        try {  
            return BCrypt.checkpw(plainTextPassword, hashedPasswordFromDb);  
        } catch (IllegalArgumentException e) {  
            // 如果哈希格式不正确，BCrypt 会抛出异常  
            return false;  
        }  
    }  

    /**
     * 验证明文密码是否与哈希密码匹配
     * @param plainTextPassword 明文密码
     * @param hashedPassword 哈希密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return checkPassword(plainTextPassword, hashedPassword);
    }

    // --- 文件哈希部分 (使用 SHA-256) ---

    /**  
     * 计算一个输入流 (如文件) 的 SHA-256 哈希值。  
     * 这种方法效率很高，因为它不需要将整个文件加载到内存中。  
     * @param inputStream 文件的输入流  
     * @return 文件的 SHA-256 哈希值的十六进制字符串 (64个字符)  
     * @throws IOException 如果读取流时发生错误  
     */  
    public String generateFileHash(InputStream inputStream) throws IOException {  
        try {  
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  
            byte[] buffer = new byte[8192]; // 8KB 缓冲区  
            int bytesRead;  
            while ((bytesRead = inputStream.read(buffer)) != -1) {  
                digest.update(buffer, 0, bytesRead);  
            }  
            byte[] hashedBytes = digest.digest();  
            return bytesToHex(hashedBytes);  
        } catch (NoSuchAlgorithmException e) {  
            // SHA-256 是 Java 标准库的一部分，理论上永远不会发生此异常  
            throw new RuntimeException("SHA-256 algorithm not found", e);  
        }  
    }  

    /**  
     * 一个辅助方法，用于将字节数组转换为十六进制字符串。
     * @param hash 字节数组
     * @return 十六进制字符串
     */  
    private String bytesToHex(byte[] hash) {  
        StringBuilder hexString = new StringBuilder(2 * hash.length);  
        for (byte b : hash) {  
            String hex = Integer.toHexString(0xff & b);  
            if (hex.length() == 1) {  
                hexString.append('0');  
            }  
            hexString.append(hex);  
        }  
        return hexString.toString();  
    }  

    public static void main(String[] args) {
        HashUtil hashUtil = new HashUtil();
        String password = "123456";
        String hashedPassword = hashUtil.hashPassword(password);
        System.out.println(hashedPassword);
    }

    public static int getWorkFactor() {
        return WORK_FACTOR;
    }
}