package rest;

import game.Cell;
import game.GameGenerator;
import io.quarkiverse.renarde.htmx.HxController;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.security.RenardeSecurity;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import me.atrox.haikunator.Haikunator;
import model.BoardEntity;
import model.GameEntity;
import model.ScoreEntity;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.util.ArrayList;
import java.util.List;

import static game.GameGenerator.DEFAULT_COLUMNS;
import static game.GameGenerator.DEFAULT_MAX_CHARGE;
import static game.GameGenerator.DEFAULT_MIN_CHARGE;
import static game.GameGenerator.DEFAULT_ROWS;

@Path("/board")
@Blocking
public class BoardController extends HxController {
    GameGenerator gameGenerator = new GameGenerator();

    @Inject
    RenardeSecurity security;
    
    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance create(List<BoardEntity> boards,
                CreateBoardData board);

        public static native TemplateInstance create$content(CreateBoardData board);

        public static native TemplateInstance leaderboard(List<BoardEntity> boards, Long boardId, String boardName,
                List<ScoreData> leaderboard);

        public static native TemplateInstance leaderboard$content(Long boardId, String boardName, List<ScoreData> leaderboard);
    }

    @Authenticated
    @Path("{id}/leaderboard")
    public TemplateInstance leaderboard(@RestPath Long id) {
        final BoardEntity board = BoardEntity.findById(id);
        final List<ScoreEntity> scores = ScoreEntity.boardScores(board);
        final List<ScoreData> scoreData = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            final ScoreEntity score = scores.get(i);
            scoreData.add(new ScoreData(i + 1, score.user.userName, score.user.email, security.getUser().equals(score.user), score.score));
        }
        return isHxRequest() ? Templates.leaderboard$content(board.id, board.name, scoreData) :
                Templates.leaderboard(BoardEntity.listAll(), board.id, board.name, scoreData);
    }

    @Authenticated
    public TemplateInstance create() {
        if(!security.getUser().roles().contains("admin")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        final List<BoardEntity> boards = BoardEntity.listAll();
        String generatedName = new Haikunator().setTokenLength(0).haikunate();
        final CreateBoardData board = new CreateBoardData(generatedName, DEFAULT_ROWS,
                DEFAULT_COLUMNS, DEFAULT_MIN_CHARGE, DEFAULT_MAX_CHARGE);
        return isHxRequest() ? Templates.create$content(board) :
                Templates.create(boards, board);
    }

    @Authenticated
    @POST
    public Response save(@RestForm String name, @RestForm @Positive int rows, @RestForm @Positive int columns,
            @RestForm @Positive int minCharge, @RestForm @Positive int maxCharge) {
        onlyHxRequest();
        if(!security.getUser().roles().contains("admin")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        final List<Cell> cells = gameGenerator.generateCells(rows, columns, minCharge, maxCharge);
        String boardName = StringUtil.isNullOrEmpty(name) ? new Haikunator().setTokenLength(0).haikunate() : name;
        final BoardEntity board = BoardEntity.fromCells(boardName, cells, rows, columns);
        board.persist();
        return seeOther(Router.getURI(GameController::startGameFromBoard, board.id));
    }

    @Authenticated
    @DELETE
    @Transactional
    @Path("{boardId}")
    public Response delete(@RestPath Long boardId) {
        onlyHxRequest();
        if(!security.getUser().roles().contains("admin")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        GameEntity.deleteAll();
        ScoreEntity.deleteAll();
        BoardEntity.deleteById(boardId);
        hx(HxResponseHeader.LOCATION, Router.getURI(GameController::index).getPath());
        return seeOther(Router.getURI(GameController::index));
    }


    public record CreateBoardData(String generatedName, int defaultRows,
                                  int defaultColumns, int defaultMinCharge, int defaultMaxCharge) {
    }


    public record ScoreData(int rank, String userName, String userEmail, boolean isMe, int score) {}
}