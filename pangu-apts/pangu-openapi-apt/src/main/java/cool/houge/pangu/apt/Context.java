package cool.houge.pangu.apt;

import io.swagger.v3.oas.models.OpenAPI;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * @author ZY (kzou227@qq.com)
 */
public class Context {

    public final ProcessingEnvironment env;
    public final OpenAPI openAPI;

    public Context(ProcessingEnvironment env, OpenAPI openAPI) {
        this.env = env;
        this.openAPI = openAPI;
    }

    public ProcessingEnvironment getEnv() {
        return env;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }
}
