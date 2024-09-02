package vinnsla;

public class Vec2D {
    public double x, y;

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void baeta(Vec2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void draga(Vec2D other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public void sinnum(double value) {
        this.x *= value;
        this.y *= value;
    }

    public void deilt(double value) {
        this.x /= value;
        this.y /= value;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public void setMagnitude(double mag) {
        normalize();
        sinnum(mag);
    }

    public void limit(double max) {
        if (magnitude() > max) {
            setMagnitude(max);
        }
    }

    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            deilt(mag);
        }
    }

    public static Vec2D draga(Vec2D a, Vec2D b) {
        return new Vec2D(a.x - b.x, a.y - b.y);
    }

    public double fjarlaegd(Vec2D other) {
        return Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y));
    }


}
