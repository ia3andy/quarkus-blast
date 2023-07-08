package util;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkus.oidc.TenantResolver;
import io.quarkus.resteasy.reactive.server.runtime.exceptionmappers.UnauthorizedExceptionMapper;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import rest.GameController;

import java.net.URI;
import java.util.Objects;

import static io.quarkiverse.renarde.htmx.HxController.HX_REQUEST_HEADER;

@ApplicationScoped
public class MyUnauthorizedExceptionMapper {

    @Inject
    TenantResolver tenantResolver;

    @Inject
    protected HttpHeaders httpHeaders;

    UnauthorizedExceptionMapper delegate = new UnauthorizedExceptionMapper();

    @ServerExceptionMapper(value = UnauthorizedException.class, priority = 1)
    public Uni<Response> handle(RoutingContext routingContext) {
        var tenant = tenantResolver.resolve(routingContext);
        if (tenant == null) {
            final URI uri = Router.getURI(GameController::index);
            if (Objects.equals(httpHeaders.getHeaderString(HX_REQUEST_HEADER), "true")) {
              return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).header(HxController.HxResponseHeader.LOCATION.key(), uri).build());
            }
            return Uni.createFrom().item(Response.seeOther(uri).build());
        } else {
            return delegate.handle(routingContext);
        }
    }
}
