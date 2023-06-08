package game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Game(List<Cell> cells, int rows, int columns, int score) {

    private static final int[][] DIRECTIONS = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    public Cell cell(int row, int column) {
        int index = row * columns() + column;
        return isValidCell(row, column) ? cells().get(index) : null;
    }

    public List<List<Cell>> asGrid() {
        List<List<Cell>> table = new ArrayList<>();
        for (int i = 0; i < rows(); i++) {
            List<Cell> row = new ArrayList();
            for (int j = 0; j < columns(); j++) {
                row.add(cell(i, j));
            }
            table.add(row);
        }
        return table;
    }

    public static int[] coords(int index, int columns) {
        int row = index / columns;
        int column = index % columns;
        return new int[] { row, column };
    }

    public boolean isValidCell(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    public boolean isCompleted() {
        return cells.stream().map(Cell::type).allMatch(Objects::isNull);
    }
    public Game blast(Set<Cell> blast) {
        List<Cell> newCells = cells.stream()
                .map(c -> blast.contains(c) ? new Cell(c.row(), c.column(), null, c.charge()) : c)
                .collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score() + computePoints(blast));
    }

    public boolean isAlone(Cell targetCell) {
        for (int[] direction : DIRECTIONS) {
            int newRow = targetCell.row() + direction[0];
            int newColumn = targetCell.column() + direction[1];
            if (isValidCell(newRow, newColumn)) {
                return false;
            }
        }
        return true;
    }

    public Set<Cell> detectReactionGroup(int row, int column) {
        Cell targetCell = cell(row, column);
        QuarkType targetType = targetCell.type();

        Set<Cell> group = new HashSet<>();
        group.add(targetCell);

        // Stack for iterative traversal
        Deque<Cell> stack = new ArrayDeque<>();
        stack.push(targetCell);

        while (!stack.isEmpty()) {
            Cell currentCell = stack.pop();

            for (int[] direction : DIRECTIONS) {
                int newRow = currentCell.row() + direction[0];
                int newColumn = currentCell.column() + direction[1];

                // Check if the new coordinates are within the cells boundaries
                if (isValidCell(newRow, newColumn)) {
                    Cell neighbor = cell(newRow, newColumn);

                    // Check if the neighbor has the same type as the target cell
                    if (neighbor.type() == targetType && !group.contains(neighbor)) {
                        group.add(neighbor);
                        stack.push(neighbor);
                    }
                }
            }
        }

        return group;
    }

    public Game changeCellType(Cell selectedCell, QuarkType newType) {
        final List<Cell> newCells = cells.stream().map(c -> {
            if (selectedCell.equals(c))
                return new Cell(c.row(), c.column(), newType, c.charge());
            else if (c.type() == null)
                return new Cell(selectedCell.row(), selectedCell.column(), c.type(), 0);
            return c;
        }).collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score());
    }

    public static Game play(Game game, int row, int column, QuarkType type) {
        Cell selectedCell = game.cell(row, column);
        if (selectedCell == null) {
            throw new IllegalStateException("Invalid selected cell: " + row + ":" + column);
        }
        if (game.isAlone(selectedCell)) {
            return game.blast(Set.of(selectedCell));
        }
        final Game updated = game.changeCellType(selectedCell, type);
        final Set<Cell> reaction = updated.detectReactionGroup(row, column);
        final Set<Cell> lonely = updated.detectLonely();
        final Set<Cell> blast = new HashSet<>();
        blast.addAll(reaction);
        blast.addAll(lonely);
        return updated.blast(reaction);
    }

    private Set<Cell> detectLonely() {
        return cells.stream().filter(this::isAlone).collect(Collectors.toSet());
    }

    static int computePoints(Collection<Cell> group) {
        int size = group.size();
        int charge = group.stream().mapToInt(Cell::charge).sum();
        return charge * size;
    }

}
