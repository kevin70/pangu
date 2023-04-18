import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.PathParam;
import io.jooby.annotation.QueryParam;

public class HelloController {

    /**
     *
     * @param q 查询参数
     * @return
     */
    @GET("/hello/{path_param_name}")
    public String hello(@QueryParam String q, @PathParam("path_param_name") String path) {
        return "Hello World";
    }
}
