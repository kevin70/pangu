/**
 * @author ZY (kzou227@qq.com)
 */
@GeneratePrisms({
    @GeneratePrism(value = OpenAPIDefinition.class, publicAccess = true),
    @GeneratePrism(value = SecurityScheme.class, publicAccess = true),
    @GeneratePrism(value = SecurityRequirement.class, publicAccess = true)
})
package cool.houge.pangu.apt.generator;

import io.avaje.prism.GeneratePrism;
import io.avaje.prism.GeneratePrisms;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
