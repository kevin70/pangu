package cool.houge.jjwt;

import io.avaje.jsonb.JsonException;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;

import java.util.Map;

/**
 * avaje-jsonb 实现.
 *
 * @author ZY (kzou227@qq.com)
 */
public class JsonbDeserializer implements Deserializer<Map<String, ?>> {

    private final JsonType<Map<String, ?>> type;

    public JsonbDeserializer() {
        this(Jsonb.builder().build());
    }

    public JsonbDeserializer(Jsonb jsonb) {
        this.type = jsonb.type(Types.mapOf(Object.class));
    }

    @Override
    public Map<String, ?> deserialize(byte[] bytes) throws DeserializationException {
        try {
            return type.fromJson(bytes);
        } catch (JsonException e) {
            throw new DeserializationException("反序列化JWT失败", e);
        }
    }
}
