package org.firstinspires.ftc.teamcode.common.util;

public class Pose {
    public double x, y, z;

    public Pose() {
        this(0, 0, 0);
    }

    /**
     * @param
     */
    public Pose(double y, double x, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Pose add(Pose p) {
        return new Pose(this.x + p.x, this.y + p.y, this.z + z);
    }

    public Pose subtract(Pose p) {
        return new Pose(this.x - p.x, this.y - p.y, this.z - p.z);
    }

    public Pose multiply(Pose p) {
        return new Pose(this.x * p.x, this.y * p.y, this.z * p.z);
    }

    public Pose divide(Pose p) {
        return new Pose(this.x / p.x, this.y / p.y, this.z / p.z);
    }

    public Vector2D toVector2D() {
        return new Vector2D(x, y, z);
    }

    public String toString() {
        return String.format("x: %.2f, y: %.2f, z: %.2f", x, y, z);
    }
}