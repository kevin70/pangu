package cool.houge.pangu.jooby;

import io.swagger.v3.core.util.PathUtils;
import io.toolisticon.aptk.tools.ElementUtils;
import java.util.Optional;
import javax.lang.model.element.TypeElement;

/**
 * @author ZY (kzou227@qq.com)
 */
class PathDocBuilder {

    private DocContext ctx;
    private TypeElement element;

    PathDocBuilder(DocContext ctx, TypeElement element) {
        this.ctx = ctx;
        this.element = element;
    }

    void build() {
        var wrapper = PathWrapper.wrap(element);
        ElementUtils.AccessEnclosedElements.getEnclosedMethods(element).stream()
            .filter(e -> ElementUtils.CheckModifierOfElement.hasPublicModifier(e))
            .forEach(e -> {
                // 处理请求函数
                var p = Optional.ofNullable(wrapper).map(PathWrapper::value).orElse(new String[0]);
                new MethodDocBuilder(ctx, PathUtils.collectPath(p), element, e).build();
            });
    }
}
