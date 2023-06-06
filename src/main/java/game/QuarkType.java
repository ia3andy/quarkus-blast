package game;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QuarkType {
    UP("U"),
    DOWN("D"),
    CHARM("C"),
    STRANGE("S"),
    TOP("T"),
    BOTTOM("B");

    public static final List<QuarkType> TYPES = Stream.of(values()).collect(Collectors.toList());

    private final String key;

    QuarkType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }



    public QuarkType next() {
        int currentIndex = TYPES.indexOf(this);
        int nextIndex = (currentIndex + 1) % TYPES.size();
        return TYPES.get(nextIndex);
    }

    @Override public String toString() {
        return super.toString().toLowerCase();
    }
}
