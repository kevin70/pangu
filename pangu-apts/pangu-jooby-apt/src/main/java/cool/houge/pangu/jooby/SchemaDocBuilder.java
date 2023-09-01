package cool.houge.pangu.jooby;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import cool.houge.pangu.jooby.validator.EmailWrapper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.toolisticon.aptk.tools.ElementUtils;
import io.toolisticon.aptk.tools.ProcessingEnvironmentUtils;
import io.toolisticon.aptk.tools.TypeUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.*;

/**
 * Help build OpenAPI Schema objects.
 */
class SchemaDocBuilder {

    private static final String APP_FORM = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String APP_JSON = "application/json";

    private final DocContext ctx;
    private final KnownTypes knownTypes;
    private final Map<String, Schema<?>> schemas = new TreeMap<>();
    private final Map<TypeMirror, Map<String, TypeMirror>> genericTypeMap = new HashMap<>();

    SchemaDocBuilder(DocContext ctx) {
        this.ctx = ctx;
        this.knownTypes = ctx.knownTypes;
    }

    private TypeMirror getIterableType() {
        return TypeUtils.getTypes()
            .erasure(ProcessingEnvironmentUtils.getElements()
                .getTypeElement("java.lang.Iterable")
                .asType());
    }

    private TypeMirror getMapType() {
        return TypeUtils.getTypes()
            .erasure(ProcessingEnvironmentUtils.getElements()
                .getTypeElement("java.util.Map")
                .asType());
    }

    private TypeMirror getCompletableFutureType() {
        return TypeUtils.getTypes()
            .erasure(ProcessingEnvironmentUtils.getElements()
                .getTypeElement("java.util.concurrent.CompletableFuture")
                .asType());
    }

    Map<String, Schema<?>> getSchemas() {
        return schemas;
    }

    Content createContent(TypeMirror returnType, String mediaType) {
        MediaType mt = new MediaType();
        mt.setSchema(toSchema(returnType, returnType));
        Content content = new Content();
        content.addMediaType(mediaType, mt);
        return content;
    }

    /**
     * Add parameter as a form parameter.
     */
    void addFormParam(Operation operation, String varName, Schema schema) {
        RequestBody body = requestBody(operation);
        Schema formSchema = requestFormParamSchema(body);
        formSchema.addProperty(varName, schema);
    }

    void addFormItem(Operation operation, String httpName, Schema schema, String description, boolean multipart) {
        var body = requestBody(operation);
        var content = body.getContent();
        MediaType mediaType;
        if (multipart) {
            mediaType = content.get(MULTIPART_FORM_DATA);
            if (mediaType == null) {
                mediaType = new MediaType();
                mediaType.schema(new ObjectSchema());
                content.addMediaType(MULTIPART_FORM_DATA, mediaType);
            }
        } else {
            mediaType = content.get(APP_FORM);
            if (mediaType == null) {
                mediaType = new MediaType();
                mediaType.schema(new ObjectSchema());
                content.addMediaType(APP_FORM, mediaType);
            }
        }

        if (!Strings.isNullOrEmpty(description)) {
            schema.setDescription(description);
        }
        mediaType.getSchema().addProperty(httpName, schema);
    }

    private Schema requestFormParamSchema(RequestBody body) {
        final Content content = body.getContent();
        MediaType mediaType = content.get(APP_FORM);

        Schema schema;
        if (mediaType != null) {
            schema = mediaType.getSchema();
        } else {
            schema = new Schema();
            schema.setType("object");
            mediaType = new MediaType();
            mediaType.schema(schema);
            content.addMediaType(APP_FORM, mediaType);
        }
        return schema;
    }

    /**
     * Add as request body.
     */
    void addRequestBody(Operation operation, Schema schema, boolean asForm, String description) {
        RequestBody body = requestBody(operation);
        body.setDescription(description);

        MediaType mt = new MediaType();
        mt.schema(schema);

        String mime = asForm ? APP_FORM : APP_JSON;
        body.getContent().addMediaType(mime, mt);
    }

    private RequestBody requestBody(Operation operation) {
        RequestBody body = operation.getRequestBody();
        if (body == null) {
            body = new RequestBody();
            body.setRequired(true);
            Content content = new Content();
            body.setContent(content);
            operation.setRequestBody(body);
        }
        return body;
    }

    private static TypeMirror typeArgument(TypeMirror type) {
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
        return typeArguments.get(0);
    }

    Schema<?> toSchema(TypeMirror owner, Element element) {
        final var schema = toSchema(owner, element.asType());
        setLengthMinMax(element, schema);
        setFormatFromValidation(element, schema);

        // 整合自定义的 Schema
        if (SchemaWrapper.isAnnotated(element)) {
            var wrap = SchemaWrapper.wrap(element);
            if (!wrap.nameIsDefaultValue()) {
                schema.setName(wrap.name());
            }
            if (!wrap.descriptionIsDefaultValue()) {
                schema.setDescription(wrap.description());
            }
            if (!wrap.formatIsDefaultValue()) {
                schema.setFormat(wrap.format());
            }
        }
        return schema;
    }

    Schema<?> toSchema(TypeMirror owner, TypeMirror type) {
        var types = TypeUtils.getTypes();
        if (types.isAssignable(type, getCompletableFutureType())) {
            type = typeArgument(type);
        }

        Schema<?> schema = knownTypes.createSchema(ctx.typeDef(type));
        if (schema != null) {
            return schema;
        }
        if (types.isAssignable(type, getMapType())) {
            return buildMapSchema(owner, type);
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return buildArraySchema(owner, type);
        }
        if (types.isAssignable(type, getIterableType())) {
            return buildIterableSchema(owner, type);
        }
        Element e = types.asElement(type);
        if (e != null && e.getKind() == ElementKind.ENUM) {
            return buildEnumSchema(e);
        }
        return buildObjectSchema(type);
    }

    private Schema<?> buildEnumSchema(Element e) {
        var schema = new StringSchema();
        e.getEnclosedElements().stream()
            .filter(ec -> ec.getKind().equals(ElementKind.ENUM_CONSTANT))
            .forEach(ec -> schema.addEnumItem(ec.getSimpleName().toString()));

        var doc = ctx.parserJavadoc(e);
        schema.setDescription(doc.fullDescription());
        return schema;
    }

    private Schema<?> buildObjectSchema(TypeMirror type) {
        String objectSchemaKey = getObjectSchemaName(type);

        Schema objectSchema = schemas.get(objectSchemaKey);
        if (objectSchema == null) {
            // Put first to resolve recursive stack overflow
            objectSchema = new ObjectSchema();
            schemas.put(objectSchemaKey, objectSchema);
            populateObjectSchema(type, objectSchema);
        }

        Schema ref = new Schema();
        ref.$ref("#/components/schemas/" + objectSchemaKey);
        return ref;
    }

    private Schema<?> buildIterableSchema(TypeMirror owner, TypeMirror type) {
        Schema<?> itemSchema = new ObjectSchema().format("unknownIterableType");

        if (type.getKind() == TypeKind.DECLARED) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
            if (typeArguments.size() == 1) {
                var realType = getGenericTypes(owner).get(typeArguments.get(0).toString());
                if (realType != null) {
                    itemSchema = toSchema(owner, realType);
                } else {
                    itemSchema = toSchema(owner, typeArguments.get(0));
                }
            }
        }

        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(itemSchema);
        return arraySchema;
    }

    private Schema<?> buildArraySchema(TypeMirror owner, TypeMirror type) {
        ArrayType arrayType = (ArrayType) type;
        Schema<?> itemSchema = toSchema(owner, arrayType.getComponentType());

        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(itemSchema);
        return arraySchema;
    }

    private Schema<?> buildMapSchema(TypeMirror owner, TypeMirror type) {
        Schema<?> valueSchema = new ObjectSchema().format("unknownMapValueType");

        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() == 2) {
                valueSchema = toSchema(owner, typeArguments.get(1));
            }
        }

        MapSchema mapSchema = new MapSchema();
        mapSchema.setAdditionalProperties(valueSchema);
        return mapSchema;
    }

    private String getObjectSchemaName(TypeMirror type) {
        var element = TypeUtils.getTypes().asElement(type);
        var name = new StringBuilder();
        name.append(element.getSimpleName());

        if (type instanceof DeclaredType dt) {
            for (TypeMirror t : dt.getTypeArguments()) {
                name.append('$').append(TypeUtils.getTypes().asElement(t).getSimpleName());
            }
        }
        return name.toString();
    }

    private Map<String, TypeMirror> getGenericTypes(TypeMirror type) {
        if (genericTypeMap.containsKey(type)) {
            return genericTypeMap.get(type);
        }

        var m = new HashMap<String, TypeMirror>();
        if (type instanceof DeclaredType dt) {
            var arg1 = ((DeclaredType) dt.asElement().asType()).getTypeArguments();
            var arg2 = dt.getTypeArguments();

            for (int i = 0; i < arg1.size(); i++) {
                m.put(arg1.get(i).toString(), arg2.get(i));
            }
        }

        genericTypeMap.put(type, m);
        return m;
    }

    private <T> void populateObjectSchema(TypeMirror objectType, Schema<T> objectSchema) {
        Element element = TypeUtils.getTypes().asElement(objectType);
        for (VariableElement field : allFields(element)) {
            Schema<?> propSchema = toSchema(objectType, field);
            if (Strings.isNullOrEmpty(propSchema.getDescription())) {
                setDescription(element, field, propSchema);
            }
            // setLengthMinMax(field, propSchema);
            if (Strings.isNullOrEmpty(propSchema.getFormat())) {
                setFormatFromValidation(field, propSchema);
            }

            var fieldName = CaseFormat.LOWER_CAMEL.to(
                CaseFormat.LOWER_UNDERSCORE, field.getSimpleName().toString());

            if (isNotNullable(field)) {
                objectSchema.addRequiredItem(fieldName);
            }
            objectSchema.addProperty(fieldName, propSchema);
        }
    }

    private void setFormatFromValidation(Element element, Schema<?> propSchema) {
        if (EmailWrapper.isAnnotated(element)) {
            propSchema.format("email");
        }
    }

    private void setDescription(Element owner, Element element, Schema<?> propSchema) {
        if (ElementUtils.CheckKindOfElement.isOfKind(owner, ElementKind.RECORD)
            || ElementUtils.CheckKindOfElement.isOfKind(owner, ElementKind.RECORD_COMPONENT)) {
            var doc = ctx.parserJavadoc(owner);
            propSchema.setDescription(doc.param(element.getSimpleName().toString()));
        } else {
            var doc = ctx.parserJavadoc(element);
            propSchema.setDescription(doc.fullDescription());
        }
    }

    private void setLengthMinMax(Element element, Schema<?> propSchema) {
        // FIXME
    }

    private boolean isNotNullable(Element element) {
        return element.getAnnotationMirrors().stream()
            .anyMatch(m -> {
                var name = m.getAnnotationType().asElement().getSimpleName().toString();
                return name.equals("NotNull") || name.equals("NotBlank") || name.equals("NotEmpty");
            });
    }

    /**
     * Gather all the fields (properties) for the given bean element.
     */
    private List<VariableElement> allFields(Element element) {
        List<VariableElement> list = new ArrayList<>();
        gatherProperties(list, element);
        return list;
    }

    /**
     * Recursively gather all the fields (properties) for the given bean element.
     */
    private void gatherProperties(List<VariableElement> fields, Element element) {
        if (element == null) {
            return;
        }
        if (element instanceof TypeElement) {
            Element mappedSuper = TypeUtils.getTypes().asElement(((TypeElement) element).getSuperclass());
            if (mappedSuper != null && !"java.lang.Object".equals(mappedSuper.toString())) {
                gatherProperties(fields, mappedSuper);
            }
            for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
                if (!ignoreField(field)) {
                    fields.add(field);
                }
            }
        }
    }

    /**
     * Ignore static or transient fields.
     */
    private boolean ignoreField(VariableElement field) {
        return isStaticOrTransient(field) || isHiddenField(field);
    }

    private boolean isHiddenField(VariableElement field) {
        if (HiddenWrapper.isAnnotated(field)) {
            return true;
        }
        if (SchemaWrapper.isAnnotated(field)) {
            var w = SchemaWrapper.wrap(field);
            if (w.hidden()) {
                return true;
            }
        }

        for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
            String simpleName = annotationMirror
                .getAnnotationType()
                .asElement()
                .getSimpleName()
                .toString();
            if ("JsonIgnore".equals(simpleName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticOrTransient(VariableElement field) {
        Set<Modifier> modifiers = field.getModifiers();
        return (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT));
    }
}
