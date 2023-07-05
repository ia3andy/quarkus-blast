package rest;

import game.Cell;
import game.Game;
import game.GameGenerator;
import game.QuarkType;
import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateGlobal;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import model.BoardEntity;
import model.GameEntity;
import model.User;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static model.GameEntity.findOrCreateBoardGame;

@Path("/")
@Blocking
public class GameController extends HxController {
    GameGenerator gameGenerator = new GameGenerator();

    @TemplateGlobal
    public static class Globals {
        public static final List<String> QUARK_TYPES = QuarkType.TYPES.stream().map(Enum::toString).collect(Collectors.toList());

        public static final boolean DEV_MODE = LaunchMode.current() == LaunchMode.DEVELOPMENT;

        public static List<String> quarkTypes(){
            return QUARK_TYPES;
        }
    }

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance game(GameData game, List<BoardEntity> boards);

        public static native TemplateInstance gamePicker(GameData game, List<BoardEntity> boards);

        public static native TemplateInstance saveScore(GameData game);

        public static native TemplateInstance game$content(GameData game);

        public static native TemplateInstance gameNotFound(Long id);

    }

    @Inject
    RenardeSecurity security;

    private User getUser() {
        return (User) security.getUser();
    }


    @Path("/")
    @Transactional
    public TemplateInstance index() {
        User user = getUser();
        if(user != null) {
            final List<BoardEntity> boards = BoardEntity.listAll();
            final BoardEntity board = boards.get(0);
            final GameEntity game = findOrCreateBoardGame(user, board);
            return Templates.game(new GameData(game), boards);
        } else {
            return Templates.game(null, null);
        }
    }

    @Authenticated
    @Path("/game/{id}")
    public TemplateInstance game(@NotNull @RestPath Long id) {
        onlyHxRequest();
        final Optional<GameEntity> game = GameEntity.findByIdOptional(id);
        final List<BoardEntity> boards = BoardEntity.listAll();
        if (game.isPresent()) {
            final GameData gameData = new GameData(game.get());
            return concatTemplates(Templates.game$content(gameData), Templates.gamePicker(gameData, boards));
        } else {
            return Templates.gameNotFound(id);
        }
    }

    @Authenticated
    @Transactional
    @Path("/game/start/board/{id}")
    public void startGameFromBoard(@NotNull @RestPath Long id) {
        onlyHxRequest();
        final BoardEntity board = BoardEntity.findById(id);
        notFoundIfNull(board);
        final GameEntity gameToPlay = findOrCreateBoardGame(getUser(), board);
        game(gameToPlay.id);
    }



    @Authenticated
    @POST
    @Path("/game/play")
    public void play(@NotNull @RestPath Long id, @NotNull @RestPath int row, @NotNull @RestPath int column,
            @NotNull @RestForm("type")
            QuarkType type) {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        final Game played = Game.play(game.toGame(), row, column, type);
        game.setGame(played);
        if (played.isCompleted()) {
            game.completed = Timestamp.from(Instant.now());
        }
        game(id);
    }

    @Authenticated
    @POST
    @Path("/game/restart")
    public void restart(@NotNull @RestPath Long id) {
        onlyHxRequest();
        final GameEntity existingGame = GameEntity.findById(id);
        notFoundIfNull(existingGame);
        final BoardEntity board = existingGame.board;
        existingGame.delete();
        final GameEntity gameToPlay = GameEntity.createBoardGame(getUser(), board);
        game(gameToPlay.id);
    }


    @Authenticated
    @POST
    @Path("/game/score/save")
    public void saveScore(@NotNull @RestPath Long id, @RestForm String nickname) {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        final BoardEntity board = BoardEntity.findById(game.id);
        notFoundIfNull(board);
        board.bestScores.put(nickname, game.score);
        board.persist();
    }

    public record GameData(Long id, Long boardId, String name, List<List<Cell>> grid, int score, Date completed) {
        public GameData(GameEntity game) {
            this(game.id, game.board.id, game.board.name, game.toGame().asGrid(), game.score, game.completed);
        }

    }
}