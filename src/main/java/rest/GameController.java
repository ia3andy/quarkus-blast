package rest;

import game.Cell;
import game.Coords;
import game.Game;
import game.GameGenerator;
import game.QuarkType;
import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateGlobal;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import model.BoardEntity;
import model.GameEntity;
import model.ScoreEntity;
import model.User;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.reactive.RestPath;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static model.GameEntity.findOrCreateBoardGame;

@Path("/")
@Blocking
public class GameController extends HxController {
    GameGenerator gameGenerator = new GameGenerator();

    @TemplateGlobal
    public static class Globals {
        public static final List<String> QUARK_TYPES = QuarkType.TYPES.stream().map(Enum::toString)
                .collect(Collectors.toList());

        public static final boolean DEV_USER = ConfigProvider.getConfig().getValue("blast.dev-user", Boolean.class);

        public static final boolean OIDC_GOOGLE = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.oidc.google.provider", String.class).isPresent();
        public static final boolean OIDC_GITHUB = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.oidc.github.provider", String.class).isPresent();

        public static List<String> quarkTypes() {
            return QUARK_TYPES;
        }
    }

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance guide(List<BoardEntity> boards);
        public static native TemplateInstance guide$content();

        public static native TemplateInstance game(GameData game, List<BoardEntity> boards);

        public static native TemplateInstance gamePicker(GameData game, List<BoardEntity> boards);

        public static native TemplateInstance game$content(GameData game);
    }

    @Inject
    RenardeSecurity security;

    private User getUser() {
        return (User) security.getUser();
    }

    @Path("/")
    public TemplateInstance index() {
        User user = getUser();
        if (user != null) {
            return guide();
        }
        return Templates.game(null, null);
    }

    public TemplateInstance guide() {
        final List<BoardEntity> boards = BoardEntity.listAll();
        return isHxRequest() ? concatTemplates(Templates.guide$content(), Templates.gamePicker(null, boards)) : Templates.guide(boards);
    }

    @Authenticated
    @Transactional
    @Path("/game/start")
    public TemplateInstance start() {
        final List<BoardEntity> boards = BoardEntity.listAll();
        if(!boards.isEmpty()) {
            final BoardEntity board = boards.get(0);
            final GameEntity game = findOrCreateBoardGame(getUser(), board);
            game(game.id);
        }
        return game(null);
    }

    @Authenticated
    @Path("/game/{id}")
    public TemplateInstance game(@NotNull @RestPath Long id) {
        final Optional<GameEntity> game = GameEntity.findByIdOptional(id);
        if(game.isPresent() && game.get().completed != null) {
            seeOther(Router.getURI(BoardController::leaderboard, game.get().board.id));
        }
        final List<BoardEntity> boards = BoardEntity.listAll();
        final GameData gameData = game.map(g -> new GameData(g, flash.get("context"))).orElse(null);
        if (isHxRequest()) {
            response.headers()
                    .set("HX-Push-Url", Router.getURI(GameController::game, id).getPath());
            return concatTemplates(Templates.game$content(gameData), Templates.gamePicker(gameData, boards));
        } else {
            return Templates.game(gameData, boards);
        }
    }

    @Authenticated
    @Transactional
    @Path("/game/start/board/{id}")
    public void startGameFromBoard(@NotNull @RestPath Long id) {
        final BoardEntity board = BoardEntity.findById(id);
        notFoundIfNull(board);
        final GameEntity gameToPlay = findOrCreateBoardGame(getUser(), board);
        game(gameToPlay.id);
    }

    @Authenticated
    @POST
    @Path("/game/{id}/{coords}/select")
    public void select(@NotNull @RestPath Long id, @NotNull @RestPath String coords) throws Exception {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        game.setGame(game.toGame().cleanCharge());
        game.persist();
        final Coords parsed = Coords.parse(coords);
        final Map<String, String> context = game.toGame().findSwappableCells(parsed)
                .stream()
                .collect(Collectors.toMap(Coords::toString, s -> "swappable"));
        context.put(coords, "selected");
        context.put("selected", coords);
        flash("context", context);
        game(id);
    }

    @Authenticated
    @POST
    @Path("/game/{id}/{from}/{to}/play")
    public void play(@NotNull @RestPath Long id, @NotNull @RestPath String from, @NotNull @RestPath String to)
            throws Exception {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        if(game.completed != null) {
            seeOther(Router.getURI(BoardController::leaderboard, game.board.id));
        }
        final Game played = Game.play(game.toGame(), Coords.parse(from), Coords.parse(to));
        game.setGame(played);
        if (played.isCompleted()) {
            game.completed = Timestamp.from(Instant.now());
            game.persist();
            final ScoreEntity score = saveScore(game);
            seeOther(Router.getURI(BoardController::leaderboard, score.board.id));
        }
        game.persist();
        game(id);
    }

    @Authenticated
    @POST
    @Path("/game/restart")
    public Response restart(@NotNull @RestPath Long id) {
        onlyHxRequest();
        final GameEntity existingGame = GameEntity.findById(id);
        notFoundIfNull(existingGame);
        final BoardEntity board = existingGame.board;
        existingGame.delete();
        return seeOther(Router.getURI(GameController::startGameFromBoard, board.id));
    }


    private ScoreEntity saveScore(GameEntity game) {
        final BoardEntity board = BoardEntity.findById(game.board.id);
        notFoundIfNull(board);
        ScoreEntity score = ScoreEntity.findUserScore(getUser(), board);
        flash("score", game.score);
        if (score != null) {
            if(score.score < game.score) {
                score.score = game.score;
                score.persist();
                flash("newBestScore", true);
            }
        } else {
            score = new ScoreEntity();
            score.board = board;
            score.user = getUser();
            score.score = game.score;
            score.persist();
            flash("newScore", true);
        }
        return score;
    }

    public record GameData(Long id, Long boardId, String name, List<List<Cell>> grid, Map<String, String> context, int score, Date completed) {
        public GameData(GameEntity game, Map<String, String> context) {
            this(game.id, game.board.id, game.board.name, game.toGame().asGrid(), context == null ? Map.of() : context, game.score, game.completed);
        }

    }
}