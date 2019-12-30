package top.thinkin.delayhunter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import top.thinkin.delayhunter.error.MessageException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jsons {
    private Jsons() {
    }

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeFactory TF = OM.getTypeFactory();

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        OM.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        OM.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
    }

    public static final <T> T readValue(String json, Class<T> type) {
        try {
            return OM.readValue(json, type);
        } catch (IOException e) {
            throw new MessageException(-1, e.getMessage());
        }
    }


    public static final String writeAsString(Object value) {
        try {
            return OM.writeValueAsString(value);
        } catch (IOException e) {
            throw new MessageException(-1, e.getMessage());
        }
    }








}
