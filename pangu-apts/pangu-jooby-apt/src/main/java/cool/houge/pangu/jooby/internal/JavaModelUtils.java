package cool.houge.pangu.jooby.internal;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;

/**
 * @author ZY (kzou227@qq.com)
 */
public class JavaModelUtils {

    /**
     * Get the class name for the given type element. Handles {@link NestingKind}.
     *
     * @param typeElement The type element
     * @return The class name
     */
    public static String getClassName(TypeElement typeElement) {
        Name qualifiedName = typeElement.getQualifiedName();
        NestingKind nestingKind;
        try {
            nestingKind = typeElement.getNestingKind();
            if (nestingKind == NestingKind.MEMBER) {
                TypeElement enclosingElement = typeElement;
                StringBuilder builder = new StringBuilder();
                while (nestingKind == NestingKind.MEMBER) {
                    builder.insert(0, '$').insert(1, enclosingElement.getSimpleName());
                    Element enclosing = enclosingElement.getEnclosingElement();

                    if (enclosing instanceof TypeElement) {
                        enclosingElement = (TypeElement) enclosing;
                        nestingKind = enclosingElement.getNestingKind();
                    } else {
                        break;
                    }
                }
                Name enclosingName = enclosingElement.getQualifiedName();
                return enclosingName.toString() + builder;
            } else {
                return qualifiedName.toString();
            }
        } catch (RuntimeException e) {
            return qualifiedName.toString();
        }
    }
}
