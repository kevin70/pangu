package cool.houge.pangu.jooby;

import static io.toolisticon.aptk.tools.ProcessingEnvironmentUtils.getElements;

import com.github.chhorz.javadoc.JavaDocParser;
import com.github.chhorz.javadoc.tags.BlockTag;
import com.github.chhorz.javadoc.tags.ParamTag;
import com.github.chhorz.javadoc.tags.ReturnTag;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.toolisticon.aptk.tools.MessagerUtils;
import java.util.HashMap;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

/**
 * @author ZY (kzou227@qq.com)
 */
class DocContext {

    final OpenAPI openAPI;
    final JavaDocParser javaDocParser;
    final FlexmarkHtmlConverter htmlConverter;
    final KnownTypes knownTypes;
    final SchemaDocBuilder schemaBuilder;

    DocContext(OpenAPI openAPI, JavaDocParser javaDocParser, FlexmarkHtmlConverter htmlConverter) {
        this.openAPI = openAPI;
        this.javaDocParser = javaDocParser;
        this.htmlConverter = htmlConverter;
        this.knownTypes = new KnownTypes();
        // FIXME 完善
        this.schemaBuilder = new SchemaDocBuilder(this);
    }

    void combineSchemas() {
        var components = components();
        var schemas = components.getSchemas();
        if (schemas == null) {
            schemas = new HashMap<>();
        }
        components.schemas(schemas);
        schemas.putAll(schemaBuilder.getSchemas());
    }

    void addOperation(String path, HttpMethod method, Operation operation) {
        var paths = openAPI.getPaths();
        if (paths == null) {
            paths = new Paths();
            openAPI.paths(paths);
        }

        var pathItem = paths.get(path);
        if (pathItem == null) {
            pathItem = new PathItem();
            paths.addPathItem(path, pathItem);
        }

        switch (method) {
            case GET:
                pathItem.get(operation);
                break;
            case POST:
                pathItem.post(operation);
                break;
            case PUT:
                pathItem.put(operation);
                break;
            case PATCH:
                pathItem.patch(operation);
                break;
            case DELETE:
                pathItem.delete(operation);
                break;
            default:
                MessagerUtils.getMessager().printMessage(Kind.NOTE, "不支持[" + method + "]方法的OpenAPI文档生成 path=" + path);
        }
    }

    Content createContent(TypeMirror returnType, String mediaType) {
        return schemaBuilder.createContent(returnType, mediaType);
    }

    JavadocDesc parserJavadoc(Element e) {
        var str = getElements().getDocComment(e);
        var javadoc = javaDocParser.parse(str);

        var summary = convertDesc(javadoc.getSummary());
        var description = convertDesc(javadoc.getDescription());
        String returnDescription = null;
        var params = new HashMap<String, String>();
        for (BlockTag tag : javadoc.getTags()) {
            if (tag instanceof ParamTag pt) {
                params.put(pt.getParamName(), convertDesc(pt.getParamDescription()));
            }
            if (tag instanceof ReturnTag pt) {
                returnDescription = convertDesc(pt.getDescription());
            }
        }
        return new JavadocDesc(summary, description, returnDescription, params);
    }

    private String convertDesc(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        s = s.trim();
        // 删除最后的句号
        var lastChar = s.charAt(s.length() - 1);
        if (lastChar == '.') {
            s = s.substring(0, s.length() - 1);
        }

        s = htmlConverter.convert(s.trim());
        lastChar = s.charAt(s.length() - 1);
        if (lastChar == '\n') {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public Components components() {
        var components = openAPI.getComponents();
        if (components == null) {
            components = new Components();
        }
        openAPI.components(components);
        return components;
    }
    /**
     * Return the type removing validation annotations etc.
     */
    public static String typeDef(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;

            return declaredType.asElement().toString();
        } else {
            return trimAnnotations(typeMirror.toString());
        }
    }

    public static String trimAnnotations(String type) {
        int pos = type.indexOf("@");
        if (pos == -1) {
            return type;
        }
        return type.substring(0, pos) + type.substring(type.lastIndexOf(' ') + 1);
    }
}
