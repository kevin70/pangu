package cool.houge.pangu.apt;

import com.google.common.base.Strings;
import cool.houge.pangu.apt.generator.OpenAPIDefinitionPrism;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author ZY (kzou227@qq.com)
 */
@SupportedAnnotationTypes("*")
public class OpenAPIProcessor extends AbstractProcessor {

    Context ctx;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        ctx = new Context(processingEnv, new OpenAPI());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(OpenAPIDefinition.class)) {
            this.readOpenAPIDefinition(element);
        }
        return false;
    }

    void readOpenAPIDefinition(Element e) {
        var prism = OpenAPIDefinitionPrism.getInstanceOn(e);
        if (prism == null) {
            return;
        }
        ctx.openAPI.info(transformInfo(prism.info()));
    }

    Info transformInfo(OpenAPIDefinitionPrism.InfoPrism prism) {
        var info = new Info();
        if (!Strings.isNullOrEmpty(prism.title())) {
            info.title(prism.title());
        }
        if (!Strings.isNullOrEmpty(prism.description())) {
            info.description(prism.description());
        }
        if (!Strings.isNullOrEmpty(prism.version())) {
            info.version(prism.version());
        }
        return info;
    }

}
