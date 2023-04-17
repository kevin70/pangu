package cool.houge.jjwt;

import io.avaje.jsonb.JsonException;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.jsonwebtoken.io.SerializationException;
import io.jsonwebtoken.io.Serializer;

import java.util.Map;

/**
 * avaje-jsonb 实现.
 *
 * @author ZY (kzou227@qq.com)
 */
public class JsonbSerializer implements Serializer<Map<String, ?>> {

    private final JsonType<Map<String, ?>> type;

    public JsonbSerializer() {
        this(Jsonb.builder().build());
    }

    public JsonbSerializer(Jsonb jsonb) {
        this.type = jsonb.type(Types.mapOf(Object.class));
    }

    @Override
    public byte[] serialize(Map<String, ?> stringMap) throws SerializationException {
        try {
            return type.toJsonBytes(stringMap);
        } catch (JsonException e) {
            throw new SerializationException("序列化JWT失败", e);
        }
    }
}
