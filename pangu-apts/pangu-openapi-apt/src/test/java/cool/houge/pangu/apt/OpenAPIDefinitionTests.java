package cool.houge.pangu.apt;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.junit.Managed;
import io.github.ascopes.jct.workspaces.Workspace;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author ZY (kzou227@qq.com)
 */
@ExtendWith(JctExtension.class)
class OpenAPIDefinitionTests {

    @Managed
    Workspace workspace;

    @JavacCompilerTest(minVersion = 11, maxVersion = 11)
    void render(JctCompiler<?, ?> compiler) {
        workspace.createSourcePathPackage()
            .createFile("samples/OpenAPIDefinitionSample.java").copiedFromClassPath("samples/OpenAPIDefinitionSample.java")
        ;

        compiler.addAnnotationProcessors(new OpenAPIProcessor())
            .failOnWarnings(true)
            .showDeprecationWarnings(true)
            .compile(workspace);
    }

}
