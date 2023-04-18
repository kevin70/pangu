/**
 * @author ZY (kzou227@qq.com)
 */
@AnnotationWrapper(
        value = {
            OpenAPIDefinition.class,
            Schema.class,
            Tag.class,
            SecurityRequirement.class,
            SecurityRequirements.class,
            SecurityScheme.class,
            Hidden.class,
            // ====================================================== //
            Path.class,
            GET.class,
            POST.class,
            PUT.class,
            PATCH.class,
            DELETE.class,
            Consumes.class,
            Produces.class,
            QueryParam.class,
            PathParam.class,
            FormParam.class,
            Header.class,
            HeaderParam.class,
            ContextParam.class,
        },
        usePublicVisibility = true)
package cool.houge.pangu.jooby;

import io.jooby.annotation.Consumes;
import io.jooby.annotation.ContextParam;
import io.jooby.annotation.DELETE;
import io.jooby.annotation.FormParam;
import io.jooby.annotation.GET;
import io.jooby.annotation.Header;
import io.jooby.annotation.HeaderParam;
import io.jooby.annotation.PATCH;
import io.jooby.annotation.POST;
import io.jooby.annotation.PUT;
import io.jooby.annotation.Path;
import io.jooby.annotation.PathParam;
import io.jooby.annotation.Produces;
import io.jooby.annotation.QueryParam;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.toolisticon.aptk.annotationwrapper.api.AnnotationWrapper;
