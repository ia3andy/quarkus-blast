package rest;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Blocking
public class UserController extends HxController {

    @Inject
    RenardeSecurity security;

    @POST
    @Path("login/dev")
    public Response loginDev() {
        model.User user = model.User.findByAuthId("manual", "dev");
        return Response.seeOther(Router.getURI(GameController::index)).cookie(security.makeUserCookie(user)).build();
    }


}