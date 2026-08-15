package com.chenpperr.xhs.common.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 脱敏序列化器
 *
 * 在 Jackson 序列化时，根据字段上的 @Sensitive 注解，自动调用对应的打码方法
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    /**
     * 当前字段的脱敏类型
     */
    private SensitiveType type;

    /**
     * 无参构造（Jackson 反射创建实例时调用）
     */
    public SensitiveJsonSerializer() {
    }

    /**
     * 有参构造（内部使用，携带脱敏类型）
     */
    private SensitiveJsonSerializer(SensitiveType type) {
        this.type = type;
    }

    /**
     * 序列化核心方法：把字段值打码后写入 JSON
     */
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(mask(value, type));
    }

    /**
     * 上下文方法：Jackson 在序列化某个字段前调用，
     * 读取该字段上的 @Sensitive 注解，拿到脱敏类型
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            //从 property（这个字段）上，查找类型为 Sensitive 的注解，如果找到了，返回这个注解的实例对象。
            if (annotation != null) {
                return new SensitiveJsonSerializer(annotation.type());
            }
        }
        return this;
    }

    /**
     * 根据脱敏类型调用对应的打码方法
     */
    private String mask(String value, SensitiveType type) {
        switch (type) {
            case MOBILE:
                return SensitiveUtils.maskMobile(value);
            case EMAIL:
                return SensitiveUtils.maskEmail(value);
            case ID_CARD:
                return SensitiveUtils.maskIdCard(value);
            default:
                return value;
        }
    }
}
