package game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        return cells.stream().allMatch(Cell::blasted);
    }

    public Game blast(Set<Cell> reaction, Set<Cell> lonely) {
        List<Cell> newCells = cells.stream()
                .map(c -> reaction.contains(c) || lonely.contains(c) ?
                        new Cell(c.row(), c.column(), c.type(), c.charge(), true) :
                        c)
                .collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score() + computePoints(reaction, lonely));
    }

    public boolean isAlone(Cell targetCell) {
        for (int[] direction : DIRECTIONS) {
            int newRow = targetCell.row() + direction[0];
            int newColumn = targetCell.column() + direction[1];
            Cell neighbor = cell(newRow, newColumn);
            if (neighbor != null && !neighbor.blasted()) {
                return false;
            }
        }
        return true;
    }

    public Set<Cell> detectReactionGroup(int row, int column) {
        Cell targetCell = cell(row, column);
        QuarkType targetType = targetCell.type();

        Set<Cell> group = new HashSet<>();

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
                    if (!neighbor.blasted() && neighbor.type() == targetType && !group.contains(neighbor)) {
                        group.add(neighbor);
                        stack.push(neighbor);
                    }
                }
            }
        }

        return group;
    }

    public Game cleanCharge() {
        final List<Cell> newCells = cells.stream().map(c -> {
            if (c.blasted())
                return new Cell(c.row(), c.column(), c.type(), 0, true);
            return c;
        }).collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score());
    }

    public Game swapCells(Cell cell1, Cell cell2) {
        final List<Cell> newCells = cells.stream().map(c -> {
            if (cell1.equals(c))
                return new Cell(c.row(), c.column(), cell2.type(), cell2.charge(), cell2.blasted());
            else if (cell2.equals(c))
                return new Cell(c.row(), c.column(), cell1.type(), cell1.charge(), cell1.blasted());
            else if (c.blasted())
                return new Cell(c.row(), c.column(), c.type(), 0, true);
            return c;
        }).collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score());
    }

    public static Game play(Game game, int row, int column) {
        Game cleaned = game.cleanCharge();
        Cell selectedCell = cleaned.cell(row, column);
        if (selectedCell == null) {
            throw new IllegalStateException("Invalid selected cell: " + row + ":" + column);
        }
        final Optional<Cell> swap = cleaned.findCellToSwap(selectedCell);
        if(swap.isEmpty()) {
            return cleaned.blast(Set.of(selectedCell), Set.of());
        }
        final Game updated = cleaned.swapCells(selectedCell, swap.get());
        final Set<Cell> reaction = new HashSet<>();
        reaction.addAll(updated.detectReactionGroup(row, column));
        reaction.addAll(updated.detectReactionGroup(swap.get().row(), swap.get().column()));
        final Set<Cell> lonely = updated.detectLonely();
        return updated.blast(reaction, lonely);
    }

    private Optional<Cell> findCellToSwap(final Cell selectedCell) {
        Optional<Cell> current = Optional.empty();
        for (int[] direction : DIRECTIONS) {
            int newRow = selectedCell.row() + direction[0];
            int newColumn = selectedCell.column() + direction[1];
            Cell neighbor = cell(newRow, newColumn);
            if (neighbor != null
                    && !neighbor.blasted()
                    && neighbor.charge() < selectedCell.charge()
                    && (current.isEmpty() || neighbor.charge() < current.get().charge())) {
                current = Optional.of(neighbor);
            }
        }
        return current;
    }

    private Set<Cell> detectLonely() {
        return cells.stream().filter(this::isAlone).collect(Collectors.toSet());
    }

    static int computePoints(Collection<Cell> reaction, Set<Cell> lonely) {
        int size = reaction.size();
        int charge = reaction.stream().mapToInt(Cell::charge).sum();
        int lonelyCharge = lonely.stream().mapToInt(Cell::charge).sum();
        return charge * size + lonelyCharge;
    }

}
