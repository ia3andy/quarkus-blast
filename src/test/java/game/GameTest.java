package game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class GameTest {


    @Test
    public void testCellRetrieval() {
        // Create a grid with a flat list of cells
        List<Cell> cells = Arrays.asList(
                new Cell(0, 0, QuarkType.UP, 1, false),
                new Cell(0, 1, QuarkType.CHARM, 2, false),
                new Cell(1, 0, QuarkType.STRANGE, 5, false),
                new Cell(1, 1, QuarkType.DOWN, 10, false)
        );
        int rows = 2;
        int columns = 2;

        Game grid = new Game(cells, rows, columns, 0);

        // Test valid cell retrieval
        Cell cell = grid.cell(0, 0);
        Assertions.assertEquals(new Cell(0, 0, QuarkType.UP, 1, false), cell);

        cell = grid.cell(1, 1);
        Assertions.assertEquals(new Cell(1, 1, QuarkType.DOWN, 10, false), cell);

        // Test invalid cell retrieval
        Cell invalidCell = grid.cell(2, 2); // Out of bounds
        Assertions.assertNull(invalidCell);

        invalidCell = grid.cell(-1, 0); // Out of bounds
        Assertions.assertNull(invalidCell);
    }


}
