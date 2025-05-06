package io.github.illuminatijoe.cubegame.core;

public enum Direction {
    NORTH(0, 0, -1, 2, true),   // -Z
    SOUTH(0, 0, 1, 2, false),   // +Z
    WEST(-1, 0, 0, 0, true),    // -X
    EAST(1, 0, 0, 0, false),    // +X
    DOWN(0, -1, 0, 1, true),    // -Y
    UP(0, 1, 0, 1, false);      // +Y

    public final int dx, dy, dz;
    public final int axis;      // 0 = X, 1 = Y, 2 = Z
    public final boolean negative;

    Direction(int dx, int dy, int dz, int axis, boolean negative) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.axis = axis;
        this.negative = negative;
    }
}


