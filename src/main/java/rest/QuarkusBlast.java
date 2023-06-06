package rest;

import game.Cell;
import game.Game;
import game.GameGenerator;
import game.QuarkType;
import io.quarkiverse.renarde.htmx.HxController;
import io.smallrye.common.annotation.Blocking;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import jakarta.ws.rs.core.MediaType;
import model.GameEntity;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/")
@Blocking
public class QuarkusBlast extends HxController {
    public static final List<String> QUARK_TYPES = QuarkType.TYPES.stream().map(Enum::toString).collect(Collectors.toList());
    private static final GlobalData GLOBAL_DATA = new GlobalData(QUARK_TYPES);
    GameGenerator gameGenerator = new GameGenerator(4, 4, 1, 1000);


    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance index(GlobalData global, GameData game);

        public static native TemplateInstance index$game(GlobalData global, GameData game);

        public static native TemplateInstance gameNotFound(GlobalData global, Long id);

    }

    @Path("/")
    public TemplateInstance index() {
        return Templates.index(GLOBAL_DATA, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public void play(@NotNull @RestPath Long id, @NotNull @RestPath int row, @NotNull @RestPath int column, @NotNull @RestForm("type")
            QuarkType type) {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        final Game played = Game.play(game.toGame(), row, column, type);
        game.setGame(played);
        game.persist();
        game(id);
    }

    public TemplateInstance game(@NotNull @RestPath Long id) {
        onlyHxRequest();
        final Optional<GameEntity> game = GameEntity.findByIdOptional(id);
        return game.isPresent() ? Templates.index$game(GLOBAL_DATA, new GameData(game.get())) : Templates.gameNotFound(GLOBAL_DATA, id);
    }

    @POST
    @Transactional
    public void newGame() {
        onlyHxRequest();
        GameEntity.deleteAll();
        final Game game = gameGenerator.generateGrid();
        final GameEntity gameEntity = GameEntity.fromGame(game);
        gameEntity.persist();
        game(gameEntity.id);
    }

    public record GameData(Long id, List<List<Cell>> grid, int score) {
        public GameData(GameEntity game) {
            this(game.id, game.toGame().asGrid(), game.score);
        }

    }

    public record GlobalData(List<String> quarkTypes) {}

}