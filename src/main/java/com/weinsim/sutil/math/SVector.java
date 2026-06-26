package com.weinsim.sutil.math;

public class SVector {

    public double x, y, z;

    public SVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public SVector(SVector other) {
        this();
        set(other);
    }

    public SVector(double x, double y, double z) {
        this();
        set(x, y, z);
    }

    public SVector(double x, double y) {
        this();
        set(x, y, 0);
    }

    public SVector(double[] values) {
        this();
        set(values);
    }

    public SVector set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    public SVector set(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;

        return this;
    }

    public SVector set(double[] values) {
        x = values[0];
        y = values[1];
        z = values[2];

        return this;
    }

    public SVector set(SVector other) {
        x = other.x;
        y = other.y;
        z = other.z;

        return this;
    }

    public SVector copy() {
        return new SVector(this);
    }

    @Override
    public String toString() {
        return String.format("[ %.3f, %.3f, %.3f ]", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof SVector v)
            return x == v.x && y == v.y && z == v.z;
        return false;
    }

    public SVector add(SVector v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public SVector sub(SVector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public SVector scale(double f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public SVector div(double f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public SVector mult(SVector v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        return this;
    }

    public double dot(SVector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double magSq() {
        return x * x + y * y + z * z;
    }

    public double mag() {
        return Math.sqrt(magSq());
    }


    public double distSq(SVector other) {
        return copy().sub(other).magSq();
    }

    public double dist(SVector other) {
        return Math.sqrt(distSq(other));
    }

    public SVector normalize() {
        return div(mag());
    }

    public SVector cross(SVector v) {
        double newX = y * v.z - z * v.y;
        double newY = z * v.x - x * v.z;
        z = x * v.y - y * v.x;
        x = newX;
        y = newY;
        return this;
    }

    public SVector project(SVector v) {
        return v.copy().scale(dot(v) / v.magSq());
    }

    /**
     * @param axis
     * @param angle needs to be normalized
     * @return
     */
    public SVector rotate(SVector axis, double angle) {
        var parallel = axis.copy().scale(dot(axis));
        // this = perp
        sub(parallel);
        var w = axis.copy().cross(this);
        scale(Math.cos(angle));
        w.scale(Math.sin(angle));
        add(w);
        add(parallel);
        return this;
    }

    public SVector lerp(SVector target, double t) {
        scale(1 - t);
        add(target.copy().scale(t));
        return this;
    }
}
