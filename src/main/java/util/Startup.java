package util;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import model.BoardEntity;
import model.ScoreEntity;
import model.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
    @Transactional
    public void start(@Observes StartupEvent evt) throws IOException {
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            User user = new User();
            user.email = "nobody@example.com";
            user.firstName = "dev";
            user.lastName = "dev";
            user.tenantId = "manual";
            user.authId = "dev";
            user.userName = "dev";
            user.isAdmin = true;
            user.persist();
            final InputStream boards = Startup.class.getResourceAsStream("/boards.json");
            final String boardsJson = new String(boards.readAllBytes(), StandardCharsets.UTF_8);
            final JsonArray boardsList = new JsonArray(boardsJson);
            for (int i = 0; i < boardsList.size(); i++) {
                final BoardEntity boardEntity = boardsList.getJsonObject(i).mapTo(BoardEntity.class);
                boardEntity.persist();
            }
        }

    }
}
