package model;

import game.Cell;
import game.Game;
import game.QuarkType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class GameEntity extends PanacheEntity {

	@ManyToOne
	public BoardEntity board;

    public int rows;
    public int columns;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<StoredCell> cells;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<Boolean> blasted;
    public int score = 0;

    @CreationTimestamp
    public Timestamp started;

    public Timestamp completed = null;

    @ManyToOne
	public User user;

    public BoardEntity getBoard() {
        return board;
    }

    public static Optional<GameEntity> findRunningGame(User user, BoardEntity board) {
        return find("user = ?1 AND board = ?2 and completed is null", user, board).firstResultOptional();
    }

    public Game toGame() {
        final List<Cell> gameCells = BoardEntity.toGameCells(cells, blasted, columns);
        return new Game(gameCells, rows, columns, score);
    }

    public void setGame(Game game) {
        this.rows = game.rows();
        this.columns = game.columns();
        this.cells = new ArrayList<>(game.cells().size());
        this.blasted = new ArrayList<>(game.cells().size());
        for (int i = 0; i < game.cells().size(); i++) {
            final Cell c = game.cells().get(i);
            this.cells.add(new StoredCell(c.type(), c.charge()));
            this.blasted.add(c.blasted());
        }
        this.score = game.score();
    }

    public static GameEntity findOrCreateBoardGame(User user, BoardEntity board) {
        final Optional<GameEntity> runningGame = GameEntity.findRunningGame(user, board);
        return runningGame.orElseGet(() -> createBoardGame(user, board));
    }

    public static GameEntity createBoardGame(User user, BoardEntity board) {
        final GameEntity game = GameEntity.fromBoard(board);
        game.user = user;
        game.persist();
        return game;
    }

    public static GameEntity fromBoard(BoardEntity board) {
        final GameEntity gameEntity = new GameEntity();
        gameEntity.board = board;
        gameEntity.rows = board.rows;
        gameEntity.columns = board.columns;
        gameEntity.cells = new ArrayList<>(board.cells);
        gameEntity.blasted = List.of();
        return gameEntity;
    }

    public record StoredCell(QuarkType type, int charge) {
    }






}