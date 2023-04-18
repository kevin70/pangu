package cool.houge.pangu.jooby;

import java.util.Map;
import java.util.Objects;

/**
 * @author ZY (kzou227@qq.com)
 */
public record JavadocDesc(String summary, String description, String returnDescription, Map<String, String> params) {

    public String fullDescription() {
        if (Objects.equals(summary, description)) {
            return summary;
        }

        var sb = new StringBuilder();
        if (summary != null && !summary.isEmpty()) {
            sb.append(summary);
        }
        if (description != null && !description.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(description);
        }
        return sb.toString();
    }

    @Override
    public String summary() {
        return summary;
    }

    public String param(String name) {
        return params.get(name);
    }
}
