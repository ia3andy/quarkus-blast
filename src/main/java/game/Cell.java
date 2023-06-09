package game;

public record Cell(int row, int column, QuarkType type, int charge, boolean blasted) {

    public String coordsString() {
        return row + ":" + column;
    }

}
