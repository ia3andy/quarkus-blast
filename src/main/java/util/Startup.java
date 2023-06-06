package util;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
    public void start(@Observes StartupEvent evt) {

    }
}
