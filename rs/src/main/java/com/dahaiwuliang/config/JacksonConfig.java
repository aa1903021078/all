package com.dahaiwuliang.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * 全局 JSON 日期时间格式配置。
 *
 * 背景:
 *  - Spring 默认对 java.time.LocalDateTime 使用 ISO-8601(带 'T')解析,
 *    前端若提交 "yyyy-MM-dd HH:mm:ss"(空格分隔)会抛 DateTimeParseException(index 10)。
 *  - application.yml 里的 spring.jackson.date-format 只对 java.util.Date 生效,
 *    对 LocalDateTime 无效, 因此需要在此显式注册 JSR-310 的序列化/反序列化器。
 *
 * 策略:
 *  - 序列化: 统一输出 "yyyy-MM-dd HH:mm:ss"(与前端展示约定一致)。
 *  - 反序列化: LocalDateTime 同时兼容 'T' 与 空格 分隔、有无秒、可选毫秒/时区后缀,
 *    保证任意前端页面(ShopDetail 用 'T'、FoodMap 用空格)提交都不再报错。
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE = "yyyy-MM-dd";
    private static final String TIME = "HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonDateTimeCustomizer() {
        return builder -> {
            DateTimeFormatter dateTime = DateTimeFormatter.ofPattern(DATE_TIME);
            DateTimeFormatter date = DateTimeFormatter.ofPattern(DATE);
            DateTimeFormatter time = DateTimeFormatter.ofPattern(TIME);

            // 序列化: 统一输出 yyyy-MM-dd HH:mm:ss / yyyy-MM-dd / HH:mm:ss
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTime));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(date));
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(time));

            // 反序列化: LocalDateTime 使用宽松解析器, 兼容多种前端格式
            builder.deserializerByType(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(date));
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(time));
        };
    }

    /**
     * 宽松的 LocalDateTime 反序列化器:
     *  兼容 "yyyy-MM-dd'T'HH:mm[:ss]" 与 "yyyy-MM-dd HH:mm[:ss]",
     *  并自动忽略毫秒(.SSS)与时区标记(Z)。
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm")
                .optionalStart()
                .appendPattern(":ss")
                .optionalEnd()
                .toFormatter();

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null) {
                return null;
            }
            String s = text.trim();
            if (s.isEmpty()) {
                return null;
            }
            // 统一分隔符: 'T' -> 空格
            s = s.replace('T', ' ');
            // 去掉毫秒部分
            int dot = s.indexOf('.');
            if (dot > 0) {
                s = s.substring(0, dot);
            }
            // 去掉末尾时区标记
            if (s.endsWith("Z") || s.endsWith("z")) {
                s = s.substring(0, s.length() - 1).trim();
            }
            return LocalDateTime.parse(s, FORMATTER);
        }
    }
}
