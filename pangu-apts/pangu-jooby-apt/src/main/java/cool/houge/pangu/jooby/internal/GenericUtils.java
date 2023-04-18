package cool.houge.pangu.jooby.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

/**
 * @author ZY (kzou227@qq.com)
 */
public class GenericUtils {

    private final Elements elementUtils;
    private final Types typeUtils;

    /**
     * @param elementUtils The {@link Elements}
     * @param typeUtils    The {@link Types}
     */
    public GenericUtils(Elements elementUtils, Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    /**
     * Builds type argument information for the given type.
     *
     * @param dt The declared type
     * @return The type argument information
     */
    public Map<String, Map<String, TypeMirror>> buildGenericTypeArgumentInfo(DeclaredType dt) {
        Element element = dt.asElement();
        return buildGenericTypeArgumentInfo(element, dt, Collections.emptyMap());
    }

    private Map<String, Map<String, TypeMirror>> buildGenericTypeArgumentInfo(@NonNull Element element, @Nullable DeclaredType dt, Map<String, TypeMirror> boundTypes) {
        Map<String, Map<String, TypeMirror>> beanTypeArguments = new LinkedHashMap<>();
        if (dt != null) {
            List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                TypeElement typeElement = (TypeElement) element;

                Map<String, TypeMirror> directTypeArguments = resolveBoundTypes(dt);
                if (!directTypeArguments.isEmpty()) {
                    beanTypeArguments.put(typeElement.getQualifiedName().toString(), directTypeArguments);
                }
            }
        }

        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            if (!boundTypes.isEmpty()) {
                beanTypeArguments.put(JavaModelUtils.getClassName(typeElement), boundTypes);
            }
            populateTypeArguments(typeElement, beanTypeArguments);
        }
        return beanTypeArguments;
    }

    /**
     * Finds the generic types for the given interface for the given class element.
     *
     * @param element       The class element
     * @param interfaceName The interface
     * @return The generic types or an empty list
     */
    public List<? extends TypeMirror> interfaceGenericTypesFor(TypeElement element, String interfaceName) {
        for (TypeMirror tm : element.getInterfaces()) {
            DeclaredType declaredType = (DeclaredType) tm;
            Element declaredElement = declaredType.asElement();
            if (declaredElement instanceof TypeElement te) {
                if (interfaceName.equals(te.getQualifiedName().toString())) {
                    return declaredType.getTypeArguments();
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Resolve the generic type arguments for the given type mirror and bound type arguments.
     *
     * @param type        The declaring type
     * @param typeElement The type element
     * @param boundTypes  The bound types
     * @return A map of generic type arguments
     */
    private Map<String, TypeMirror> resolveGenericTypes(DeclaredType type, TypeElement typeElement, Map<String, TypeMirror> boundTypes) {
        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        Map<String, TypeMirror> resolvedParameters = new LinkedHashMap<>();
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        if (typeArguments.size() == typeParameters.size()) {
            Iterator<? extends TypeMirror> i = typeArguments.iterator();
            for (TypeParameterElement typeParameter : typeParameters) {
                String parameterName = typeParameter.toString();
                TypeMirror mirror = i.next();

                TypeKind kind = mirror.getKind();
                switch (kind) {
                    case DECLARED -> resolvedParameters.put(parameterName, mirror);
                    case TYPEVAR -> {
                        TypeVariable tv = (TypeVariable) mirror;
                        if (boundTypes.containsKey(tv.toString())) {
                            resolvedParameters.put(parameterName, boundTypes.get(tv.toString()));
                        } else {
                            TypeMirror upperBound = tv.getUpperBound();
                            TypeMirror lowerBound = tv.getLowerBound();
                            if (upperBound.getKind() != TypeKind.NULL) {
                                resolvedParameters.put(parameterName, resolveTypeReference(upperBound, boundTypes));
                            } else if (lowerBound.getKind() != TypeKind.NULL) {
                                resolvedParameters.put(parameterName, resolveTypeReference(lowerBound, boundTypes));
                            }
                        }
                    }
                    case ARRAY, BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT -> {
                        resolveGenericTypeParameter(resolvedParameters, parameterName, mirror, boundTypes);
                    }
                    case WILDCARD -> {
                        WildcardType wcType = (WildcardType) mirror;
                        TypeMirror extendsBound = wcType.getExtendsBound();
                        TypeMirror superBound = wcType.getSuperBound();
                        if (extendsBound != null) {
                            resolveGenericTypeParameter(resolvedParameters, parameterName, extendsBound, boundTypes);
                        } else if (superBound != null) {
                            if (superBound instanceof TypeVariable superTypeVar) {
                                final TypeMirror upperBound = superTypeVar.getUpperBound();
                                if (upperBound != null && !type.equals(upperBound)) {
                                    resolveGenericTypeParameter(resolvedParameters, parameterName, superBound, boundTypes);
                                }
                            } else {
                                resolveGenericTypeParameter(resolvedParameters, parameterName, superBound, boundTypes);
                            }
                        } else {
                            resolvedParameters.put(parameterName, elementUtils.getTypeElement(Object.class.getName()).asType());
                        }
                    }
                    default -> {
                        // no-op
                    }
                }
            }
        }
        return resolvedParameters;
    }

    /**
     * Resolve a type reference to use for the given type mirror taking into account generic type variables.
     *
     * @param mirror     The mirror
     * @param boundTypes The already bound types for any type variable
     * @return A type reference
     */
    protected TypeMirror resolveTypeReference(TypeMirror mirror, Map<String, TypeMirror> boundTypes) {
        TypeKind kind = mirror.getKind();
        switch (kind) {
            case TYPEVAR -> {
                TypeVariable tv = (TypeVariable) mirror;
                String name = tv.toString();
                if (boundTypes.containsKey(name)) {
                    return boundTypes.get(name);
                } else {
                    return resolveTypeReference(tv.getUpperBound(), boundTypes);
                }
            }
            case WILDCARD -> {
                WildcardType wcType = (WildcardType) mirror;
                TypeMirror extendsBound = wcType.getExtendsBound();
                TypeMirror superBound = wcType.getSuperBound();
                if (extendsBound == null && superBound == null) {
                    return elementUtils.getTypeElement(Object.class.getName()).asType();
                } else if (extendsBound != null) {
                    return resolveTypeReference(typeUtils.erasure(extendsBound), boundTypes);
                } else {
                    return resolveTypeReference(superBound, boundTypes);
                }
            }
            case ARRAY -> {
                ArrayType arrayType = (ArrayType) mirror;
                TypeMirror reference = resolveTypeReference(arrayType.getComponentType(), boundTypes);
                return typeUtils.getArrayType(reference);
            }
            default -> {
                return resolveTypeReference(mirror);
            }
        }
    }

    /**
     * Resolves a type reference for the given type mirror. A type reference is either a reference to the concrete
     * {@link Class} or a String representing the type name.
     *
     * @param type The type
     * @return The type reference
     */
    TypeMirror resolveTypeReference(TypeMirror type) {
        TypeKind typeKind = type.getKind();
        if (typeKind.isPrimitive()) {
            return type;
        } else if (typeKind == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) type;
            if (dt.getTypeArguments().isEmpty()) {
                return dt;
            }
            return typeUtils.erasure(type);
        } else {
            return typeUtils.erasure(type);
        }
    }

    /**
     * Resolve bound types for the given declared type.
     *
     * @param type The declaring type
     * @return The type bounds
     */
    protected Map<String, TypeMirror> resolveBoundTypes(DeclaredType type) {
        Map<String, TypeMirror> boundTypes = new LinkedHashMap<>(2);
        TypeElement element = (TypeElement) type.asElement();

        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        if (typeArguments.size() == typeParameters.size()) {
            Iterator<? extends TypeMirror> i = typeArguments.iterator();
            for (TypeParameterElement typeParameter : typeParameters) {
                boundTypes.put(typeParameter.toString(), resolveTypeReference(i.next(), boundTypes));
            }
        }

        return boundTypes;
    }

    private void resolveGenericTypeParameter(Map<String, TypeMirror> resolvedParameters, String parameterName, TypeMirror mirror, Map<String, TypeMirror> boundTypes) {
        if (mirror instanceof DeclaredType) {
            resolvedParameters.put(
                parameterName,
                mirror
            );
        } else if (mirror instanceof TypeVariable tv) {
            String variableName = tv.toString();
            if (boundTypes.containsKey(variableName)) {
                resolvedParameters.put(
                    parameterName,
                    boundTypes.get(variableName)
                );
            } else {
                TypeMirror upperBound = tv.getUpperBound();
                if (upperBound instanceof DeclaredType) {
                    resolveGenericTypeParameter(
                        resolvedParameters,
                        parameterName,
                        upperBound,
                        boundTypes
                    );
                }
            }
        }
    }

    private void populateTypeArguments(TypeElement typeElement, Map<String, Map<String, TypeMirror>> typeArguments) {
        TypeElement current = typeElement;
        while (current != null) {

            populateTypeArgumentsForInterfaces(typeArguments, current);
            TypeMirror superclass = current.getSuperclass();

            if (superclass.getKind() == TypeKind.NONE) {
                current = null;
            } else {
                if (superclass instanceof DeclaredType dt) {
                    List<? extends TypeMirror> superArguments = dt.getTypeArguments();


                    Element te = dt.asElement();
                    if (te instanceof TypeElement) {
                        TypeElement child = current;
                        current = (TypeElement) te;
                        if (!superArguments.isEmpty()) {
                            Map<String, TypeMirror> boundTypes = typeArguments.get(JavaModelUtils.getClassName(child));
                            if (boundTypes != null) {
                                Map<String, TypeMirror> types = resolveGenericTypes(dt, current, boundTypes);

                                String name = JavaModelUtils.getClassName(current);
                                typeArguments.put(name, types);
                            } else {
                                List<? extends TypeParameterElement> typeParameters = current.getTypeParameters();
                                Map<String, TypeMirror> types = new LinkedHashMap<>(typeParameters.size());
                                if (typeParameters.size() == superArguments.size()) {
                                    Iterator<? extends TypeMirror> i = superArguments.iterator();
                                    for (TypeParameterElement typeParameter : typeParameters) {
                                        String n = typeParameter.getSimpleName().toString();
                                        types.put(n, i.next());
                                    }
                                }
                                typeArguments.put(JavaModelUtils.getClassName(current), types);
                            }
                        }

                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void populateTypeArgumentsForInterfaces(Map<String, Map<String, TypeMirror>> typeArguments, TypeElement child) {
        for (TypeMirror anInterface : child.getInterfaces()) {
            if (anInterface instanceof DeclaredType declaredType) {
                Element element = declaredType.asElement();
                if (element instanceof TypeElement te) {
                    String name = JavaModelUtils.getClassName(te);
                    if (!typeArguments.containsKey(name)) {
                        Map<String, TypeMirror> boundTypes = typeArguments.get(JavaModelUtils.getClassName(child));
                        if (boundTypes == null) {
                            boundTypes = Collections.emptyMap();
                        }
                        Map<String, TypeMirror> types = resolveGenericTypes(declaredType, te, boundTypes);
                        if (!types.isEmpty()) {
                            typeArguments.put(name, types);
                        }
                    }
                    populateTypeArgumentsForInterfaces(typeArguments, te);
                }
            }
        }
    }
}
