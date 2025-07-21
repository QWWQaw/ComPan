// ConfigUtil.java (完整实现)
package cloud.compan.servlet.utils;

import java.util.Properties;
import java.io.InputStream;

public class ConfigUtil {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigUtil.class
                .getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
            // 忽略异常，使用默认值
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}