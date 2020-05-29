package app.models;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Job implements Serializable {

    private static final long serialVersionUID = 2542556708692562568L;
    private final String name;
    private final int pointsCount;
    private final double proportion;
    private final int width;
    private final int height;
    private final List<Point> points;

    public Job(String name, int pointsCount, double proportion, int width, int height, List<Point> points) {
        this.name = name;
        this.pointsCount = pointsCount;
        this.proportion = proportion;
        this.width = width;
        this.height = height;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public double getProportion() {
        return proportion;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                ", pointsCount=" + pointsCount +
                ", proportion=" + proportion +
                ", width=" + width +
                ", height=" + height +
                ", points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return name.equals(job.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
