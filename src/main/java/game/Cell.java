package game;

public record Cell(Coords coords, QuarkType type, int charge, boolean blasted) {

    public Cell(int row, int column, QuarkType type, int charge, boolean blasted) {
        this(new Coords(row, column), type, charge, blasted);
    }

}
