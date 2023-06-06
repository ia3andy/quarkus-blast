package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameGenerator {
    private final int rows;
    private final int columns;
    private final int minCharge;
    private final int maxCharge;
    private final Random random;

    public GameGenerator(int rows, int columns, int minCharge, int maxCharge) {
        this.rows = rows;
        this.columns = columns;
        this.minCharge = minCharge;
        this.maxCharge = maxCharge;
        this.random = new Random();
    }

    public Game generateGrid() {
        List<Cell> cells = new ArrayList<>(rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                QuarkType randomQuark = getRandomQuarkType();
                cells.add(new Cell(row, column, randomQuark, random.nextInt(minCharge, maxCharge)));
            }
        }
        return new Game(cells, rows, columns, 0);
    }

    private QuarkType getRandomQuarkType() {
        int randomIndex = random.nextInt(QuarkType.values().length);
        return QuarkType.values()[randomIndex];
    }
}