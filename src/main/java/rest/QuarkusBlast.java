package rest;

import game.Cell;
import game.Game;
import game.GameGenerator;
import game.QuarkType;
import io.quarkiverse.renarde.htmx.HxController;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateGlobal;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.common.annotation.Blocking;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import me.atrox.haikunator.Haikunator;
import model.BoardEntity;
import model.GameEntity;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static game.GameGenerator.DEFAULT_COLUMNS;
import static game.GameGenerator.DEFAULT_MAX_CHARGE;
import static game.GameGenerator.DEFAULT_MIN_CHARGE;
import static game.GameGenerator.DEFAULT_ROWS;

@Path("/")
@Blocking
public class QuarkusBlast extends HxController {
    GameGenerator gameGenerator = new GameGenerator();

    @TemplateGlobal
    public static class Globals {
        public static final List<String> QUARK_TYPES = QuarkType.TYPES.stream().map(Enum::toString).collect(Collectors.toList());

        public static List<String> quarkTypes(){
        	return QUARK_TYPES;
        }
    }
    
    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance index(GameData game, List<BoardEntity> boards);

        public static native TemplateInstance boardsNav(List<BoardEntity> boards);

        public static native TemplateInstance index$game(GameData game);

        public static native TemplateInstance gameNotFound(Long id);

        public static native TemplateInstance createNewBoard(List<BoardEntity> boards,
                CreateBoardData board);

        public static native TemplateInstance createNewBoard$content(CreateBoardData board);
    }

    @Transactional
    @Path("/")
    public TemplateInstance index() {
        final List<BoardEntity> boards = BoardEntity.listAll();
        final BoardEntity board = BoardEntity.findAll().firstResult();
        return Templates.index(new GameData(createBoardGame(board)), boards);
    }

    public TemplateInstance boardsNav() {
        onlyHxRequest();
        final List<BoardEntity> boards = BoardEntity.listAll();
        return Templates.boardsNav(boards);
    }

    public TemplateInstance createNewBoard() {
        final List<BoardEntity> boards = BoardEntity.listAll();
        String generatedName = new Haikunator().haikunate();
        final CreateBoardData board = new CreateBoardData(generatedName, DEFAULT_ROWS,
                DEFAULT_COLUMNS, DEFAULT_MIN_CHARGE, DEFAULT_MAX_CHARGE);
        return isHxRequest() ? Templates.createNewBoard$content(board) :
                Templates.createNewBoard(boards, board);
    }

    @POST
    public void play(@NotNull @RestPath Long id, @NotNull @RestPath int row, @NotNull @RestPath int column,
            @NotNull @RestForm("type")
            QuarkType type) {
        onlyHxRequest();
        final GameEntity game = GameEntity.findById(id);
        notFoundIfNull(game);
        final Game played = Game.play(game.toGame(), row, column, type);
        game.setGame(played);
        if (played.isCompleted()) {
            game.completed = new Date();
        }
        game.persist();
        game(id);
    }

    @POST
    public void startGame(@NotNull @RestPath Long id) {
        onlyHxRequest();
        game(createBoardGame(id).id);
    }

    private GameEntity createBoardGame(Long id) {
        final BoardEntity board = BoardEntity.findById(id);
        notFoundIfNull(board);
        return createBoardGame(board);
    }

    private static GameEntity createBoardGame(BoardEntity board) {
        final GameEntity game = GameEntity.fromBoard(board);
        game.persist();
        return game;
    }

    public TemplateInstance game(@NotNull @RestPath Long id) {
        onlyHxRequest();
        hx(HxResponseHeader.PUSH, "/");
        hx(HxResponseHeader.TRIGGER, "refreshBoards");
        final Optional<GameEntity> game = GameEntity.findByIdOptional(id);
        return game.isPresent() ?
                Templates.index$game(new GameData(game.get())) :
                Templates.gameNotFound(id);
    }

    @POST
    public void newBoard(@RestForm String name, @RestForm @Positive int rows, @RestForm @Positive int columns,
            @RestForm @Positive int minCharge, @RestForm @Positive int maxCharge) {
        onlyHxRequest();
        GameEntity.deleteAll();
        final List<Cell> cells = gameGenerator.generateCells(rows, columns, minCharge, maxCharge);
        String boardName = StringUtil.isNullOrEmpty(name) ? new Haikunator().haikunate() : name;
        final BoardEntity board = BoardEntity.fromCells(boardName, cells, rows, columns);
        board.persist();
        final GameEntity game = createBoardGame(board);
        game(game.id);
    }

    public record GameData(Long id, List<List<Cell>> grid, int score, Date completed) {
        public GameData(GameEntity game) {
            this(game.id, game.toGame().asGrid(), game.score, game.completed);
        }

    }

    public record CreateBoardData(String generatedName, int defaultRows,
                                  int defaultColumns, int defaultMinCharge, int defaultMaxCharge) {
    }

}