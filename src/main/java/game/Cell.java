package game;

public record Cell(int row, int column, QuarkType type, int charge) {

    public Cell next() {
        return new Cell(row, column, type.next(), charge);
    }

    public String coordsString() {
        return row + ":" + column;
    }

}
