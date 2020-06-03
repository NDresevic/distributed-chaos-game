package app.util;

import app.models.Point;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {

    private static boolean insidePolygon(Point target, List<Point> polygon) {
        boolean result = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).getY() > target.getY()) != (polygon.get(j).getY() > target.getY()) &&
                    (target.getX() <
                            (polygon.get(j).getX() - polygon.get(i).getX()) *
                                    (target.getY() - polygon.get(i).getY()) /
                                    (polygon.get(j).getY() - polygon.get(i).getY()) +
                                    polygon.get(i).getX())) {
                result = !result;

            }
        }
        return result;
    }

    public static List<Point> getPointsInsidePolygon(List<Point> polygon, List<Point> targetPoints) {
        List<Point> result = new ArrayList<>();
        for (Point myPoint: targetPoints) {
            if (insidePolygon(myPoint, polygon)) {
                result.add(myPoint);
            }
        }
        return result;
    }
}
