package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import game.Coords;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import game.Cell;
import game.Game;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import model.GameEntity.StoredCell;

@Entity
@Table(name = "board")
public class BoardEntity extends PanacheEntity {
    public String name;
    public int rows;
    public int columns;

    @JdbcTypeCode(SqlTypes.JSON)
    public List<StoredCell> cells;

    public static BoardEntity fromCells(String name, List<Cell> cells, int rows, int columns) {
        BoardEntity board = new BoardEntity();
        board.name = name;
        board.rows = rows;
        board.columns = columns;
        board.cells = cells.stream().map(c -> new StoredCell(c.type(), c.charge())).collect(Collectors.toList());
        return board;
    }

    public Game toGame() {
        List<Cell> gameCells = toGameCells(cells, List.of(), columns);
        return new Game(gameCells, rows, columns, 0);
    }

    static List<Cell> toGameCells(List<StoredCell> cells, List<Boolean> blasted, int columns) {
        List<Cell> gameCells = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            final Coords coords = Game.coords(i, columns);
            final StoredCell storedCell = cells.get(i);
            final Boolean b = !blasted.isEmpty() && blasted.get(i);
            gameCells.add(new Cell(coords, storedCell.type(), storedCell.charge(), b));
        }
        return gameCells;
    }

}