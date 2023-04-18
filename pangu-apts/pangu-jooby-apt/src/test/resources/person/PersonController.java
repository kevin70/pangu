package person;

import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.PUT;
import io.jooby.annotation.Path;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.QUERY,
        name = "JWT",
        paramName = "access_token",
        description = "JWT查询参数认证")
@Path("/persons")
public class PersonController {

    /**
     * 获取用户.
     * @return 用户信息
     */
    @GET
    public PersonDto getPerson() {
        var p = new PersonDto();
        return p;
    }

    /**
     * 保存用户.
     * @param dto 用户数据
     */
    @POST
    public void savePerson(PersonDto dto) {
        System.out.println(dto);
    }

    @PUT
    public void updatePerson(PersonUForm dto) {
        System.out.println(dto);
    }

    /**
     * 分页查询.
     * @return
     */
    @GET("/pages")
    public PageResult<PersonDto> findPage() {
        return new PageResult<PersonDto>();
    }
}
