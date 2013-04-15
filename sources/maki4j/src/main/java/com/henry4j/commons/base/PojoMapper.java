package com.henry4j.commons.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import lombok.Cleanup;
import lombok.SneakyThrows;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class PojoMapper {
    private JsonFactory jsonFactory = new JsonFactory();
    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
            .enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY)
            .setSerializationInclusion(Include.NON_NULL) // excludes null-valued properties.
            .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    public PojoMapper(Module... modules) {
        assert null != modules;
        for (Module m : modules) {
            objectMapper.registerModule(m);
        }
    }

    @SneakyThrows({ JsonParseException.class, IOException.class })
    public <T> T fromJson(byte[] bytes, Class<T> pojoClass) {
        return objectMapper.readValue(bytes, pojoClass);
    }

    @SneakyThrows({ JsonParseException.class, IOException.class })
    public <T> T fromJson(String string, Class<T> pojoClass) {
        return objectMapper.readValue(string, pojoClass);
    }

    @SneakyThrows({ JsonParseException.class, IOException.class })
    public <T> T fromJson(String input, TypeReference<T> typeRef) {
        return objectMapper.readValue(input, typeRef);
    }

    @SneakyThrows({ JsonParseException.class, IOException.class })
    public <T> T fromJson(InputStream input, Class<T> pojoClass) {
        return objectMapper.readValue(input, pojoClass);
    }

    @SneakyThrows({ JsonParseException.class, IOException.class })
    public <T> T fromJson(Reader input, Class<T> pojoClass) {
        return objectMapper.readValue(input, pojoClass);
    }

    public <T> String toJson(T pojo) {
        return toJson(pojo, false);
    }

    @SneakyThrows({ JsonGenerationException.class, IOException.class })
    public <T> String toJson(T pojo, boolean prettyPrint) {
        StringWriter writer = new StringWriter();
        @Cleanup JsonGenerator jg = jsonFactory.createJsonGenerator(writer);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        objectMapper.writeValue(jg, pojo);
        return writer.toString();
    }

    @SneakyThrows({ JsonGenerationException.class, IOException.class })
    public <T> OutputStream toJson(T pojo, OutputStream output, boolean prettyPrint) {
        @Cleanup JsonGenerator jg = jsonFactory.createJsonGenerator(output);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        objectMapper.writeValue(jg, pojo);
        return output;
    }

    @SneakyThrows({ JsonGenerationException.class, IOException.class })
    public <T> Writer toJson(T pojo, Writer output, boolean prettyPrint) {
        @Cleanup JsonGenerator jg = jsonFactory.createJsonGenerator(output);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        objectMapper.writeValue(jg, pojo);
        return output;
    }
}
