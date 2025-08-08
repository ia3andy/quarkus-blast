import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.debug.adapter.RegisterDebugServerAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DebugQuteEngineObserver {

    private RegisterDebugServerAdapter registar = new RegisterDebugServerAdapter();

    void configureEngine(@Observes EngineBuilder builder) {
        builder.enableTracing(true);
        builder.addEngineListener(registar);
    }
}