package cool.houge.pangu.jooby;

import com.google.common.base.Strings;
import io.jooby.Context;
import io.jooby.FileUpload;
import io.swagger.v3.core.util.PathUtils;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.toolisticon.aptk.tools.TypeUtils;
import io.toolisticon.aptk.tools.wrapper.ElementWrapper;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author ZY (kzou227@qq.com)
 */
class MethodDocBuilder {

    private final DocContext ctx;
    private final String basePath;
    private final TypeElement beanElement;
    private final ExecutableElement element;
    private JavadocDesc javadoc;

    MethodDocBuilder(DocContext ctx, String basePath, TypeElement beanElement, ExecutableElement element) {
        this.ctx = ctx;
        this.basePath = basePath;
        this.beanElement = beanElement;
        this.element = element;
    }

    void build() {
        var methodInfo = readMethodInfo();
        if (methodInfo == null) {
            return;
        }
        this.javadoc = ctx.parserJavadoc(element);

        // 请求访问路径
        var path = PathUtils.collectPath(basePath, methodInfo.path);

        var operation = new Operation();
        operation.summary(javadoc.summary()).description(javadoc.description());

        // 添加到OpenAPI文档中
        ctx.addOperation(path, methodInfo.method, operation);

        this.addTags(operation);
        this.addSecurityRequirements(operation);
        this.addRequestParameters(operation);

        // 构建响应文档
        ApiResponses responses = new ApiResponses();
        operation.setResponses(responses);

        ApiResponse response = new ApiResponse();
        response.setDescription(Optional.ofNullable(javadoc.returnDescription()).orElse(""));

        String responseCode = "200";
        final var contentMediaType = methodInfo.produces == null ? "application/json" : methodInfo.produces;
        if (isVoid()) {
            if (Strings.isNullOrEmpty(response.getDescription())) {
                response.setDescription("No content");
            }
            responseCode = "204";
        } else {
            response.setContent(ctx.createContent(returnType(), contentMediaType));
        }
        responses.addApiResponse(responseCode, response);
    }

    private void addTags(Operation operation) {
        var tags = new LinkedHashSet<String>();
        for (Element e : List.of(beanElement, element)) {
            var list = TagWrapper.wrap(e);
            for (TagWrapper t : list) {
                tags.add(t.name());
            }
        }
        tags.forEach(operation::addTagsItem);
    }

    private void addSecurityRequirements(Operation operation) {
        var elements = new ArrayList<Element>();
        elements.add(beanElement);
        elements.add(element);

        for (Element e : List.copyOf(elements)) {
            for (AnnotationMirror annotationMirror : e.getAnnotationMirrors()) {
                elements.add(annotationMirror.getAnnotationType().asElement());
            }
        }

        var securityNames = new LinkedHashSet<String>();
        for (Element e : elements) {
            for (SecurityRequirementWrapper wrap : SecurityRequirementWrapper.wrap(e)) {
                if (securityNames.add(wrap.name())) {
                    var item = new SecurityRequirement();
                    securityNames.forEach(item::addList);
                    operation.addSecurityItem(item);
                }
            }
        }
    }

    private void addRequestParameters(Operation operation) {
        var parameters = element.getParameters();
        boolean form = false;
        boolean multipart = false;
        for (VariableElement ve : parameters) {
            if (FormParamWrapper.isAnnotated(ve)) {
                form = true;
            }

            if (ElementWrapper.wrap(ve).asType().isAssignableTo(FileUpload.class)) {
                form = true;
                multipart = true;
            }
        }

        for (VariableElement ve : parameters) {
            if (ContextParamWrapper.isAnnotated(ve)
                    || TypeUtils.TypeComparison.isAssignableTo(ve.asType(), Context.class)) {
                continue;
            }
            if (PathParamWrapper.isAnnotated(ve)) {
                addParameter(operation, PathParamWrapper.wrap(ve).value(), ve, new PathParameter());
                continue;
            }
            if (QueryParamWrapper.isAnnotated(ve)) {
                addParameter(operation, QueryParamWrapper.wrap(ve).value(), ve, new QueryParameter());
                continue;
            }
            if (HeaderWrapper.isAnnotated(ve)) {
                addParameter(operation, HeaderWrapper.wrap(ve).value(), ve, new HeaderParameter());
                continue;
            }
            if (HeaderParamWrapper.isAnnotated(ve)) {
                addParameter(operation, HeaderParamWrapper.wrap(ve).value(), ve, new HeaderParameter());
                continue;
            }
            // 请求JSON
            if (!form) {
                ctx.schemaBuilder.addRequestBody(
                        operation,
                        ctx.schemaBuilder.toSchema(ve.asType(), ve),
                        false,
                        javadoc.param(ve.getSimpleName().toString()));
            } else {
                // FileUpload 文件上传
                if (ElementWrapper.wrap(ve).asType().isAssignableTo(FileUpload.class)) {
                    var httpName = ve.getSimpleName().toString();
                    var schema = ctx.schemaBuilder.toSchema(element.asType(), ve);
                    ctx.schemaBuilder.addFormItem(
                            operation,
                            httpName,
                            schema,
                            javadoc.param(ve.getSimpleName().toString()),
                            multipart);
                }
                // @FormParam 表单参数
                if (FormParamWrapper.isAnnotated(ve)) {
                    var httpName = Optional.ofNullable(FormParamWrapper.wrap(ve).value())
                            .filter(s -> !s.isEmpty())
                            .orElseGet(() -> ve.getSimpleName().toString());
                    var schema = ctx.schemaBuilder.toSchema(element.asType(), ve);
                    ctx.schemaBuilder.addFormItem(
                            operation,
                            httpName,
                            schema,
                            javadoc.param(ve.getSimpleName().toString()),
                            multipart);
                }
            }
        }
    }

    private void addParameter(Operation operation, String paramName, VariableElement varElement, Parameter parameter) {
        var schema = ctx.schemaBuilder.toSchema(varElement.asType(), varElement);
        paramName = Optional.ofNullable(paramName)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> varElement.getSimpleName().toString());
        var desc = javadoc.param(varElement.getSimpleName().toString());

        parameter.name(paramName).schema(schema);
        if (!Strings.isNullOrEmpty(desc)) {
            parameter.setDescription(desc);
        }
        operation.addParametersItem(parameter);
    }

    private void addRequestBody(Operation operation) {
        //
    }

    private MethodInfo readMethodInfo() {
        HttpMethod method = null;
        String[] path = null;
        String[] produces = null;
        String[] consumes = null;

        if (GETWrapper.isAnnotated(element)) {
            method = HttpMethod.GET;
            var a = GETWrapper.wrap(element);
            path = a.pathIsDefaultValue() ? a.value() : a.path();
            produces = a.produces();
            consumes = a.consumes();
        }
        if (POSTWrapper.isAnnotated(element)) {
            method = HttpMethod.POST;
            var a = POSTWrapper.wrap(element);
            path = a.pathIsDefaultValue() ? a.value() : a.path();
            produces = a.produces();
            consumes = a.consumes();
        }
        if (PUTWrapper.isAnnotated(element)) {
            method = HttpMethod.PUT;
            var a = PUTWrapper.wrap(element);
            path = a.pathIsDefaultValue() ? a.value() : a.path();
            produces = a.produces();
            consumes = a.consumes();
        }
        if (PATCHWrapper.isAnnotated(element)) {
            method = HttpMethod.PATCH;
            var a = PATCHWrapper.wrap(element);
            path = a.pathIsDefaultValue() ? a.value() : a.path();
            produces = a.produces();
            consumes = a.consumes();
        }
        if (DELETEWrapper.isAnnotated(element)) {
            method = HttpMethod.DELETE;
            var a = DELETEWrapper.wrap(element);
            path = a.pathIsDefaultValue() ? a.value() : a.path();
            produces = a.produces();
            consumes = a.consumes();
        }

        if (method == null) {
            return null;
        }

        if (PathWrapper.isAnnotated(element)) {
            path = PathWrapper.wrap(element).value();
        }
        if (ConsumesWrapper.isAnnotated(element)) {
            consumes = ConsumesWrapper.wrap(element).value();
        }
        if (ProducesWrapper.isAnnotated(element)) {
            produces = ProducesWrapper.wrap(element).value();
        }

        String $produces = null;
        if (produces != null && produces.length > 0) {
            $produces = produces[0];
        }
        String $consumes = null;
        if (consumes != null && consumes.length > 0) {
            $consumes = consumes[0];
        }

        if (path == null) {
            path = new String[0];
        }
        return new MethodInfo(method, PathUtils.collectPath(path), $produces, $consumes);
    }

    private TypeMirror returnType() {
        return element.getReturnType();
    }

    private boolean isVoid() {
        return element.getReturnType().getKind() == TypeKind.VOID;
    }

    record MethodInfo(HttpMethod method, String path, String produces, String consumes) {}
}
