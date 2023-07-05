package rest;

import game.Cell;
import game.GameGenerator;
import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import me.atrox.haikunator.Haikunator;
import model.BoardEntity;
import model.GameEntity;
import org.jboss.resteasy.reactive.RestForm;

import java.util.List;

import static game.GameGenerator.DEFAULT_COLUMNS;
import static game.GameGenerator.DEFAULT_MAX_CHARGE;
import static game.GameGenerator.DEFAULT_MIN_CHARGE;
import static game.GameGenerator.DEFAULT_ROWS;
import static model.GameEntity.findOrCreateBoardGame;

@Path("/board")
@Blocking
public class BoardController extends HxController {
    GameGenerator gameGenerator = new GameGenerator();

    
    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance createNewBoard(List<BoardEntity> boards,
                CreateBoardData board);

        public static native TemplateInstance createNewBoard$content(CreateBoardData board);
    }

    @Authenticated
    @Path("create")

    public TemplateInstance createNewBoard() {
        onlyHxRequest();
        final List<BoardEntity> boards = BoardEntity.listAll();
        String generatedName = new Haikunator().setTokenLength(0).haikunate();
        final CreateBoardData board = new CreateBoardData(generatedName, DEFAULT_ROWS,
                DEFAULT_COLUMNS, DEFAULT_MIN_CHARGE, DEFAULT_MAX_CHARGE);
        return isHxRequest() ? Templates.createNewBoard$content(board) :
                Templates.createNewBoard(boards, board);
    }

    @Authenticated
    @POST
    @Path("save")
    public Response saveBoard(@RestForm String name, @RestForm @Positive int rows, @RestForm @Positive int columns,
            @RestForm @Positive int minCharge, @RestForm @Positive int maxCharge) {
        onlyHxRequest();
        GameEntity.deleteAll();
        final List<Cell> cells = gameGenerator.generateCells(rows, columns, minCharge, maxCharge);
        String boardName = StringUtil.isNullOrEmpty(name) ? new Haikunator().setTokenLength(0).haikunate() : name;
        final BoardEntity board = BoardEntity.fromCells(boardName, cells, rows, columns);
        board.persist();
        return seeOther(Router.getURI(GameController::startGameFromBoard, board.id));
    }


    public record CreateBoardData(String generatedName, int defaultRows,
                                  int defaultColumns, int defaultMinCharge, int defaultMaxCharge) {
    }

}