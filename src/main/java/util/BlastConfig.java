package util;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "blast")
public interface BlastConfig {

    @WithDefault("false")
    boolean devUser();
    Optional<Set<String>> admins();

}
