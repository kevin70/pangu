package cool.houge.pangu.jooby;

import io.toolisticon.cute.CompileTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author ZY (kzou227@qq.com)
 */
class OpenAPIDefinitionTests {

    @Test
    void h() {
        CompileTestBuilder.compilationTest()
                .addSources("/OpenAPIDefinitionApplication.java")
                .addProcessors(OpenAPIProcessor.class)
                .compilationShouldSucceed()
                .executeTest();
    }

    @Test
    void hello_controller() {
        CompileTestBuilder.compilationTest()
                .addSources("/OpenAPIDefinitionApplication.java", "/HelloController.java")
                .addProcessors(OpenAPIProcessor.class)
                .compilationShouldSucceed()
                .executeTest();
    }
}
