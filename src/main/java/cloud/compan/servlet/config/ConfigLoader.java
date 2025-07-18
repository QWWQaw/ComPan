package cloud.compan.servlet.config;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import cloud.compan.servlet.annotations.data.field.Value;

public class ConfigLoader {
    //Properties类继承自Hashtable
    //可以从.properties文件中加载键值对
    private static final Properties properties = new Properties();

    static {
        try(InputStream in = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public static void inject(Object target) {
        for(Field field : target.getClass().getDeclaredFields()) {
            Value valueAnno = field.getAnnotation(Value.class);
            if(valueAnno != null) {
                String key = valueAnno.value();
                if (key.isEmpty()) {
                    key = field.getName(); // 如果没有指定key，则使用字段名
                }
                String propertyValue = properties.getProperty(key);
                field.setAccessible(true); // 设置字段可访问
                if (propertyValue != null) {
                    try {
                        Class<?> fieldType = field.getType();
                        Object convertedValue = convertValue(propertyValue, fieldType);
                        field.set(target, convertedValue);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to inject value for field: " + field.getName(), e);
                    }
                } else {
                    throw new RuntimeException("Property not found for key: " + key);
                }
            }
        }
    }

    static Object convertValue(String value, Class<?> targetType) {
        try{
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else {
                return value; // 默认处理为String
            }
        }catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert value for field: " + value, e);
        }
    }
}
