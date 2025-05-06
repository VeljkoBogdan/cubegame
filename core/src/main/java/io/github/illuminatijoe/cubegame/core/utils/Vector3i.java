package io.github.illuminatijoe.cubegame.core.utils;

import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public class Vector3i {
    public int x;
    public int y;
    public int z;

    public Vector3i() {
        this(0, 0, 0);
    }

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Copy constructor
    public Vector3i(Vector3i other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    // Arithmetic operations
    public Vector3i add(int x, int y, int z) {
        return new Vector3i(this.x + x, this.y + y, this.z + z);
    }

    public Vector3i add(Vector3i other) {
        return new Vector3i(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3i sub(Vector3i other) {
        return new Vector3i(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3i mul(int scalar) {
        return new Vector3i(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3i div(int scalar) {
        return new Vector3i(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    // Length (magnitude) and distance
    public double len() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double dst(Vector3i other) {
        int dx = other.x - this.x;
        int dy = other.y - this.y;
        int dz = other.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Utility
    public Vector3i cpy() {
        return new Vector3i(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3i)) return false;
        Vector3i that = (Vector3i) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3i(" + x + ", " + y + ", " + z + ")";
    }

    public Vector3i scl(int scale) {
        return new Vector3i(x * scale, y * scale, z * scale);
    }
}

