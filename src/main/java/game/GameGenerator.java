package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameGenerator {

    public static final int DEFAULT_ROWS = 5;
    public static final int DEFAULT_COLUMNS = 6;
    public static final int DEFAULT_MIN_CHARGE = 1;
    public static final int DEFAULT_MAX_CHARGE = 1000;
    private final Random random;

    public GameGenerator() {
        this.random = new Random();
    }

    public List<Cell> generateCells(int rows, int columns, int minCharge, int maxCharge) {
        List<Cell> cells = new ArrayList<>(rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                QuarkType randomQuark = getRandomQuarkType();
                cells.add(new Cell(row, column, randomQuark, random.nextInt(minCharge, maxCharge), false));
            }
        }
        return cells;
    }

    private QuarkType getRandomQuarkType() {
        int randomIndex = random.nextInt(QuarkType.values().length);
        return QuarkType.values()[randomIndex];
    }
}