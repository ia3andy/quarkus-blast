package util;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "blast")
public interface BlastConfig {

    Optional<Set<String>> admins();
}
