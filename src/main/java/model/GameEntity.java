package model;

import game.Cell;
import game.Game;
import game.QuarkType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity
public class GameEntity extends PanacheEntity {

    public int rows;
    public int columns;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<StoredCell> cells;

    public Integer activeCellIndex;

    public int activationCount;
    public int score;

    public Game toGame() {
        List<Cell> gameCells = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            final int[] coords = Game.coords(i, columns);
            final StoredCell storedCell = cells.get(i);
            gameCells.add(new Cell(coords[0], coords[1], storedCell.type, storedCell.charge));

        }
        return new Game(gameCells, rows, columns, score);
    }

    public void setGame(Game game) {
        this.rows = game.rows();
        this.columns = game.columns();
        this.cells = game.cells().stream().map(c -> new StoredCell(c.type(), c.charge())).collect(Collectors.toList());
        this.score = game.score();
    }

    public static GameEntity fromGame(Game game) {
        final GameEntity gameEntity = new GameEntity();
        gameEntity.setGame(game);
        return gameEntity;
    }

    public record StoredCell(QuarkType type, int charge) {
    }






}