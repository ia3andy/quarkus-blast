package util;

import io.quarkiverse.renarde.oidc.RenardeOidcHandler;
import io.quarkiverse.renarde.oidc.RenardeOidcSecurity;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkiverse.renarde.security.RenardeUser;
import io.quarkiverse.renarde.security.RenardeUserProvider;
import io.quarkiverse.renarde.util.Flash;
import io.quarkiverse.renarde.util.RedirectException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import me.atrox.haikunator.Haikunator;
import model.User;
import rest.GameController;

import java.util.Set;

@ApplicationScoped
public class MySecuritySetup implements RenardeUserProvider, RenardeOidcHandler {

    @Inject
    RenardeSecurity security;

    @Inject
    RenardeOidcSecurity oidcSecurity;

    @Inject
    Flash flash;

    @Inject
    BlastConfig config;

    @Override
    public RenardeUser findUser(String tenantId, String id) {
    	return User.findByAuthId(tenantId, id);
    }

    /**
     * This will be called on every successful OIDC authentication,
     * be it a first-time user registration, or existing user login
     */
    @Transactional
    @Override
    public void oidcSuccess(String tenantId, String authId) {
        User user = User.findByAuthId(tenantId, authId);
        if(user == null) {
            // registration
            user = new User();
            user.tenantId = tenantId;
            user.authId = authId;

            if(config.admins().orElse(Set.of()).contains(tenantId + ":" + authId)) {
                user.isAdmin = true;
            }

            user.email = oidcSecurity.getOidcEmail();
            user.firstName = oidcSecurity.getOidcFirstName();
            user.lastName = oidcSecurity.getOidcLastName();
            user.userName = oidcSecurity.getOidcUserName();
            if(user.userName == null) {
            	user.userName = user.firstName.substring(0, 1) + user.lastName;
            }
            user.persist();
        } // else regular login
        throw new RedirectException(Response.seeOther(Router.getURI(GameController::index)).build());
    }

    /**
     * This will be called if the existing user has a valid OIDC session,
     * and attempts to login again, so we check if the user exists, and is
     * fully registered.
     */
    @Override
    public void loginWithOidcSession(String tenantId, String authId) {
        RenardeUser user = findUser(tenantId, authId);
        // old cookie, no such user
        if(user == null) {
            flash.flash("message", "Invalid user: "+authId);
            throw new RedirectException(security.makeLogoutResponse());
        }
        flash.flash("message", "Already logged in");
        throw new RedirectException(Response.seeOther(Router.getURI(GameController::index)).build());
    }
}