package person;

import io.jooby.annotation.PUT;
import io.jooby.annotation.Path;

@Path("/persons")
public class PersonController2 {

    @PUT
    public void updatePerson(PersonUForm dto) {
        System.out.println(dto);
    }
}
