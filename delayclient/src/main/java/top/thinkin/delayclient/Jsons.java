package top.thinkin.delayclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;


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

    public static final <T> T readValue(String json, Class<T> type) throws JsonProcessingException {

            return OM.readValue(json, type);

    }


    public static final String writeAsString(Object value) throws JsonProcessingException {

            return OM.writeValueAsString(value);

    }








}
