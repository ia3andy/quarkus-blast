package model;

import game.Cell;
import game.Game;
import game.QuarkType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity
public class GameEntity extends PanacheEntity {

    public int rows;
    public int columns;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<StoredCell> cells;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<Boolean> blasted;
    public int score = 0;

    public Date started;

    public Date completed;

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

    public static GameEntity fromBoard(BoardEntity board) {
        final GameEntity gameEntity = new GameEntity();
        gameEntity.rows = board.rows;
        gameEntity.columns = board.columns;
        gameEntity.cells = new ArrayList<>(board.cells);
        gameEntity.blasted = List.of();
        gameEntity.started = new Date();
        return gameEntity;
    }

    public record StoredCell(QuarkType type, int charge) {
    }






}