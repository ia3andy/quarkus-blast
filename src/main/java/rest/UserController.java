package rest;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkus.runtime.LaunchMode;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Blocking
public class UserController extends HxController {

    @Inject
    RenardeSecurity security;

    @GET
    @Path("login/dev")
    public Response loginDev() {
        if(!LaunchMode.current().isDevOrTest()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
        }
        model.User user = model.User.findByAuthId("manual", "dev");
        notFoundIfNull(user);
        return Response.seeOther(Router.getURI(GameController::index)).cookie(security.makeUserCookie(user)).build();
    }

}