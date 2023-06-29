package model;

import game.Cell;
import game.Game;
import game.QuarkType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity
public class GameEntity extends PanacheEntity {

    public Long boardId;

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

    public static Optional<GameEntity> findRunningGame(Long boardId) {
        return find("boardId = ?1 and completed is null", boardId).firstResultOptional();
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

    public static GameEntity findOrCreateBoardGame(BoardEntity board) {
        final Optional<GameEntity> runningGame = GameEntity.findRunningGame(board.id);
        return runningGame.orElseGet(() -> createBoardGame(board));
    }

    public static GameEntity createBoardGame(BoardEntity board) {
        final GameEntity game = GameEntity.fromBoard(board);
        game.persist();
        return game;
    }

    public static GameEntity fromBoard(BoardEntity board) {
        final GameEntity gameEntity = new GameEntity();
        gameEntity.boardId = board.id;
        gameEntity.rows = board.rows;
        gameEntity.columns = board.columns;
        gameEntity.cells = new ArrayList<>(board.cells);
        gameEntity.blasted = List.of();
        return gameEntity;
    }

    public record StoredCell(QuarkType type, int charge) {
    }






}