package cool.houge.helidon.http.media;

import io.helidon.common.Weighted;
import io.helidon.common.config.Config;
import io.helidon.http.media.MediaSupport;
import io.helidon.http.media.spi.MediaSupportProvider;

/**
 * @author ZY (kzou227@qq.com)
 */
public class JsonbMediaSupportProvider implements MediaSupportProvider, Weighted {

    @Override
    public String configKey() {
        return "avaje-jsonb";
    }

    @Override
    public MediaSupport create(Config config, String name) {
        return JsonbSupport.create(config, name);
    }

    @Override
    public double weight() {
        return 1;
    }
}
