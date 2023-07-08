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

    private static final Coords[] DIRECTIONS = new Coords[] { new Coords(-1, 0), new Coords(1, 0), new Coords(0, -1),
            new Coords(0, 1) };

    public Cell cell(Coords coords) {
        int index = coords.row() * columns() + coords.column();
        return isValidCell(coords) ? cells().get(index) : null;
    }

    public List<List<Cell>> asGrid() {
        List<List<Cell>> table = new ArrayList<>();
        for (int i = 0; i < rows(); i++) {
            List<Cell> row = new ArrayList();
            for (int j = 0; j < columns(); j++) {
                row.add(cell(new Coords(i, j)));
            }
            table.add(row);
        }
        return table;
    }

    public static Coords coords(int index, int columns) {
        int row = index / columns;
        int column = index % columns;
        return new Coords(row, column);
    }

    public boolean isValidCell(Coords coords) {
        return coords.row() >= 0 && coords.row() < rows && coords.column() >= 0 && coords.column() < columns;
    }

    public boolean isCompleted() {
        return cells.stream().allMatch(Cell::blasted);
    }

    public Game blast(Set<Cell> reaction) {
        List<Cell> newCells = cells.stream()
                .map(c -> reaction.contains(c) ?
                        new Cell(c.coords(), c.type(), c.charge(), true) :
                        c)
                .collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score() + computePoints(reaction));
    }

    public boolean isAlone(Cell targetCell) {
        for (Coords direction : DIRECTIONS) {
            Coords newCoords = targetCell.coords().add(direction);
            Cell neighbor = cell(newCoords);
            if (neighbor != null && !neighbor.blasted()) {
                return false;
            }
        }
        return true;
    }

    public Set<Cell> detectReactionGroup(Coords coords) {
        Cell targetCell = cell(coords);
        QuarkType targetType = targetCell.type();

        Set<Cell> group = new HashSet<>();

        // Stack for iterative traversal
        Deque<Cell> stack = new ArrayDeque<>();
        stack.push(targetCell);

        while (!stack.isEmpty()) {
            Cell currentCell = stack.pop();
            for (Coords direction : DIRECTIONS) {
                Coords newCoords = currentCell.coords().add(direction);
                Cell neighbor = cell(newCoords);
                // Check if the new coordinates are within the cells boundaries
                // Check if the neighbor has the same type as the target cell
                if (neighbor != null
                        && !neighbor.blasted()
                        && neighbor.type() == targetType
                        && !group.contains(neighbor)) {
                    group.add(neighbor);
                    stack.push(neighbor);
                }
            }
        }
        return group;
    }

    public Game cleanCharge() {
        final List<Cell> newCells = cells.stream().map(c -> {
            if (c.blasted())
                return new Cell(c.coords(), c.type(), 0, true);
            return c;
        }).collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score());
    }

    public Game swapCells(Cell cell1, Cell cell2) {
        final List<Cell> newCells = cells.stream().map(c -> {
            if (cell1.equals(c))
                return new Cell(c.coords(), cell2.type(), cell2.charge(), cell2.blasted());
            else if (cell2.equals(c))
                return new Cell(c.coords(), cell1.type(), cell1.charge(), cell1.blasted());
            else if (c.blasted())
                return new Cell(c.coords(), c.type(), 0, true);
            return c;
        }).collect(Collectors.toList());
        return new Game(newCells, rows(), columns(), score());
    }

    public static Game play(Game game, Coords from, Coords to) {
        Game cleaned = game.cleanCharge();
        Cell fromCell = cleaned.cell(from);
        Cell toCell = cleaned.cell(to);
        if (fromCell == null || toCell == null) {
            throw new IllegalStateException("Invalid selected cells: " + from + "-" + toCell);
        }
        if(fromCell.equals(toCell)) {
            return cleaned.blast(Set.of(fromCell));
        }
        final Game updated = cleaned.swapCells(fromCell, toCell);
        final Set<Cell> reaction = new HashSet<>();
        reaction.addAll(updated.detectReactionGroup(from));
        reaction.addAll(updated.detectReactionGroup(to));
        return updated.blast(reaction);
    }

    public Set<Coords> findSwappableCells(final Coords from) {
        Cell fromCell = this.cell(from);
        if (fromCell == null || fromCell.blasted()) {
            throw new IllegalStateException("Invalid selected cell: " + from);
        }
        Set<Coords> cells = new HashSet<>();
        for (Coords direction : DIRECTIONS) {
            Coords newCoords = fromCell.coords().add(direction);
            Cell neighbor = cell(newCoords);
            if (neighbor != null && !neighbor.blasted()) {
                cells.add(newCoords);
            }
        }
        return cells;
    }

    private Set<Cell> detectLonely() {
        return cells.stream().filter(this::isAlone).collect(Collectors.toSet());
    }

    static int computePoints(Collection<Cell> reaction) {
        int size = reaction.size();
        int charge = reaction.stream().mapToInt(Cell::charge).sum();
        return charge * size;
    }

}
