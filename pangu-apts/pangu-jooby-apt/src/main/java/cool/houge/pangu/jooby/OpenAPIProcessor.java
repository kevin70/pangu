package cool.houge.pangu.jooby;

import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.google.common.base.Strings;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import io.jooby.annotation.*;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import io.toolisticon.aptk.tools.AbstractAnnotationProcessor;
import io.toolisticon.aptk.tools.ElementUtils;
import io.toolisticon.aptk.tools.wrapper.ElementWrapper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author ZY (kzou227@qq.com)
 */
@SupportedAnnotationTypes({
    Constants.PATH,
    "io.swagger.v3.oas.annotations.OpenAPIDefinition",
    "io.swagger.v3.oas.annotations.security.SecurityScheme"
})
public class OpenAPIProcessor extends AbstractAnnotationProcessor {

    private OpenAPI openAPI;
    private DocContext ctx;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        var javaDocParser = JavaDocParserBuilder.withAllKnownTags().build();
        var htmlConverter = FlexmarkHtmlConverter.builder().build();
        this.openAPI = new OpenAPI();
        this.ctx = new DocContext(openAPI, javaDocParser, htmlConverter);
    }

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv
            .getElementsAnnotatedWithAny(
                Set.of(OpenAPIDefinition.class, io.swagger.v3.oas.annotations.security.SecurityScheme.class))
            .forEach(element -> {
                this.readOpenAPIDefinition(element);
                this.readSecurityScheme(element);
            });

        //
        // roundEnv.getElementsAnnotatedWith(OpenAPIDefinition.class).stream().forEach(this::readOpenAPIDefinition);
        //
        // roundEnv.getElementsAnnotatedWith(io.swagger.v3.oas.annotations.security.SecurityScheme.class).stream()
        //                .forEach(this::readSecurityScheme);

        var typeElements = new LinkedHashSet<TypeElement>();
        var annos = Set.of(Path.class, GET.class, POST.class, PUT.class, DELETE.class, PATCH.class);
        roundEnv.getElementsAnnotatedWithAny(annos).forEach(element -> {
            if (ElementUtils.CheckKindOfElement.isMethod(element)) {
                var wrap = ElementWrapper.toExecutableElementWrapper(ElementWrapper.wrap(element));
                var opt = wrap.getFirstEnclosingElementWithKind(ElementKind.CLASS);
                if (opt.isPresent()) {
                    typeElements.add(
                        getElements().getTypeElement(opt.get().asType().getQualifiedName()));
                } else {
                    // TODO: write error
                }
            }
            if (ElementUtils.CheckKindOfElement.isClass(element)) {
                typeElements.add((TypeElement) element);
            }
        });

        for (TypeElement element : typeElements) {
            if (element == null) {
                // TODO: write error
            }
            new PathDocBuilder(ctx, element).build();
        }

        if (roundEnv.processingOver()) {
            this.ctx.combineSchemas();
            // 写入文档
            writeOpenAPI();
        }
        return false;
    }

    void readOpenAPIDefinition(Element element) {
        var wrapper = OpenAPIDefinitionWrapper.wrap(element);
        if (wrapper == null) {
            return;
        }

        var infoWrapper = wrapper.info();
        var info = new Info();
        if (!Strings.isNullOrEmpty(infoWrapper.title())) {
            info.title(infoWrapper.title());
        }
        if (!Strings.isNullOrEmpty(infoWrapper.description())) {
            info.description(infoWrapper.description());
        }
        if (!Strings.isNullOrEmpty(infoWrapper.version())) {
            info.version(infoWrapper.version());
        }
        openAPI.info(info);

        // 读取标签
        for (TagWrapper tw : wrapper.tags()) {
            var tag = new Tag();
            tag.setName(tw.name());
            tag.setDescription(tw.description());
            openAPI.addTagsItem(tag);
        }
    }

    void readSecurityScheme(Element element) {
        var list = SecuritySchemeWrapper.wrap(element);
        if (list == null || list.isEmpty()) {
            return;
        }

        var components = ctx.components();
        for (SecuritySchemeWrapper ssw : list) {
            var item = new SecurityScheme();
            item.type(SecurityScheme.Type.valueOf(ssw.type().name()));
            if (!ssw.paramNameIsDefaultValue()) {
                item.name(ssw.paramName());
            }
            if (!ssw.descriptionIsDefaultValue()) {
                item.description(ssw.description());
            }
            if (!ssw.inIsDefaultValue()) {
                item.in(SecurityScheme.In.valueOf(ssw.in().name()));
            }
            if (!ssw.bearerFormatIsDefaultValue()) {
                item.bearerFormat(ssw.bearerFormat());
            }

            components.addSecuritySchemes(ssw.name(), item);
        }
    }

    void writeOpenAPI() {
        try {
            var yaml = getFiler().createResource(StandardLocation.CLASS_OUTPUT, "openapi", "openapi.yaml");
            try (var writer = yaml.openWriter()) {
                Yaml31.pretty().writeValue(writer, openAPI);
            }

            var json = getFiler().createResource(StandardLocation.CLASS_OUTPUT, "openapi", "openapi.json");
            try (var writer = json.openWriter()) {
                Json31.pretty().writeValue(writer, openAPI);
            }

            // 测试
            try (var writer = new FileWriter("build/openapi.yaml")) {
                Yaml31.pretty().writeValue(writer, openAPI);
            }
        } catch (IOException e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Kind.ERROR, "写入openapi.json异常");
        }
    }
}
