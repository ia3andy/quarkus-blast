package util;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonArray;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.BoardEntity;
import model.ScoreEntity;
import model.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class Startup {

    @Inject
    BlastConfig config;

    /**
     * This method is executed at the start of your application
     */
    @Transactional
    public void start(@Observes StartupEvent evt) throws IOException {
        if (config.devUser() && User.findByAuthId("manual", "dev") == null) {
            final InputStream boards = Startup.class.getResourceAsStream("/boards.json");
            final String boardsJson = new String(boards.readAllBytes(), StandardCharsets.UTF_8);
            final JsonArray boardsList = new JsonArray(boardsJson);
            for (int i = 0; i < boardsList.size(); i++) {
                final BoardEntity boardEntity = boardsList.getJsonObject(i).mapTo(BoardEntity.class);
                boardEntity.persist();
            }
            User user = new User();
            user.email = "nobody@example.com";
            user.firstName = "dev";
            user.lastName = "dev";
            user.tenantId = "manual";
            user.authId = "dev";
            user.userName = "dev";
            user.isAdmin = true;
            user.persist();
            for (int i = 1; i < boardsList.size(); i++) {
                ScoreEntity score = new ScoreEntity();
                score.score = 50;
                score.board = BoardEntity.findById(i);
                score.user = user;
                score.persist();
            }
        }

    }
}
