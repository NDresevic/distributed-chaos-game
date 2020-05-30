package app.models;

import app.AppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobExecution implements Runnable {

    private final String name;
    private final double proportion;
    private final int width;
    private final int height;
    private final List<Point> startingPoints;
    private List<Point> computedPoints;

    private boolean working;

    public JobExecution(String name, double proportion, int width, int height, List<Point> startingPoints) {
        this.name = name;
        this.proportion = proportion;
        this.width = width;
        this.height = height;
        this.startingPoints = startingPoints;
        this.computedPoints = new ArrayList<>();
        this.working = true;
    }

    @Override
    public void run() {
        AppConfig.timestampedStandardPrint("Computing points...");
        while (working) {
            computedPoints.add(computeNewPoint());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Point> getComputedPoints() { return computedPoints; }

    public boolean isWorking() { return working; }

    private Point getRandomStartPoint() {
        Random r = new Random();
        int index = r.nextInt(startingPoints.size());
        return startingPoints.get(index);
    }

    private Point getRandomPoint() {
        Random r = new Random();
        int x = r.nextInt(width + 1);
        int y = r.nextInt(height + 1);
        return new Point(x, y);
    }

    private Point computeNewPoint() {
        if (computedPoints.isEmpty()) {
            return getRandomPoint();
        }

        Point lastPoint = computedPoints.get(computedPoints.size() - 1);
        Point randomPoint = getRandomStartPoint();
        int newX = (int) (randomPoint.getX() + proportion * (lastPoint.getX() - randomPoint.getX()));
        int newY = (int) (randomPoint.getY() + proportion * (lastPoint.getY() - randomPoint.getY()));
        return new Point(newX, newY);
    }
}
