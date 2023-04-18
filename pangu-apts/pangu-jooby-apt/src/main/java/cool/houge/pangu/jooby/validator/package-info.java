/**
 * @author ZY (kzou227@qq.com)
 */
@AnnotationWrapper(
        value = {
            Email.class,
            Size.class,
        },
        usePublicVisibility = true)
package cool.houge.pangu.jooby.validator;

import io.toolisticon.aptk.annotationwrapper.api.AnnotationWrapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
