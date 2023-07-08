package game;

public record Coords(int row, int column) {

    public static Coords parse(String coords) {
        final String[] split = coords.split(",");
        if(split.length != 2) {
            throw new IllegalArgumentException("Invalid coords: " + coords);
        }
        return new Coords(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
    public String toString() {
        return row + "," + column;
    }

    public Coords add(Coords coords) {
        return new Coords(row() + coords.row(), column() + coords.column());
    }
}
