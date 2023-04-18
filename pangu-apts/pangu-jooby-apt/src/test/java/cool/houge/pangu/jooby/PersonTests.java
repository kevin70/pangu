package cool.houge.pangu.jooby;

import io.toolisticon.cute.CompileTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author ZY (kzou227@qq.com)
 */
class PersonTests {

    @Test
    void compile_PersonController() {
        CompileTestBuilder.compilationTest()
            .addSources("/person/PersonController.java", "/person/PersonDto.java")
            .addProcessors(OpenAPIProcessor.class)
            .compilationShouldSucceed()
            .executeTest();
    }

    @Test
    void compile_PersonController2() {
        CompileTestBuilder.compilationTest()
            .addSources("/person/PersonController2.java")
            .addProcessors(OpenAPIProcessor.class)
            .compilationShouldSucceed()
            .executeTest();
    }
}
