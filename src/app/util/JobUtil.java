package app.util;

import app.models.FractalIdJob;
import app.models.Job;
import app.models.Point;

import java.util.*;

public class JobUtil {

    public static Map<Job, Integer> computeServentCountForJobs(int serventCount, List<Job> jobs) {
        Map<Job, Integer> result = new HashMap<>();
        int jobCount = jobs.size();
        for (int i = 0; i < jobCount; i++) {
            int assignedServentCount = serventCount / jobCount;
            if (i < serventCount % jobCount) {
                assignedServentCount++;
            }
            result.put(jobs.get(i), assignedServentCount);
        }
        return result;
    }

    public static int computeJobServentsCount(int serventCount, int pointsCount) {
        int result = 1;
        int x = 0;
        while (true) {
            int possibleNodeCount = 1 + x * (pointsCount - 1);
            if (possibleNodeCount > serventCount) {
                break;
            }
            result = possibleNodeCount;
            x++;
        }
        return result;
    }

    public static List<String> computeFractalIds(int serventCount, int pointsCount) {
        List<String> fractalIds = new ArrayList<>();
        int length = 0;
        String base = "";

        // if only one node is executing the job, no need to split fractalIds
        if (serventCount == 1) {
            fractalIds.add("0");
            return fractalIds;
        }

        while (serventCount > 0) {
            if (length >= 1) {
                boolean hasLength = false;
                for (String fractalId: fractalIds) {
                    if (fractalId.length() == length) {
                        base = fractalId;
                        fractalIds.remove(fractalId);
                        hasLength = true;
                        break;
                    }
                }
                if (!hasLength) {
                    length++;
                    continue;
                }

                serventCount++;
            }

            for (int i = 0; i < pointsCount; i++) {
                fractalIds.add(base + i);
            }
            if (length == 0) {
                length++;
            }
            serventCount -= pointsCount;
        }
        Collections.sort(fractalIds);
        return fractalIds;
    }

    public static List<Point> computeRegionPoints(List<Point> jobPoints, int i, double proportion) {
        List<Point> regionPoints = new ArrayList<>();
        Point referentPoint = jobPoints.get(i);
        for (int j = 0; j < jobPoints.size(); j++) {
            if (i == j) {
                regionPoints.add(referentPoint);
                continue;
            }

            Point other = jobPoints.get(j);
            int newX = (int) (referentPoint.getX() + proportion * (other.getX() - referentPoint.getX()));
            int newY = (int) (referentPoint.getY() + proportion * (other.getY() - referentPoint.getY()));
            Point newPoint = new Point(newX, newY);

            regionPoints.add(newPoint);
        }
        return regionPoints;
    }

    public static Map<FractalIdJob, FractalIdJob> mapFractalsForJob(List<String> oldFractals, List<String> newFractals,
                                                                    String jobName) {
        Map<FractalIdJob, FractalIdJob> result = new HashMap<>();
        for (String oldOne: oldFractals) {
            for (String newOne: newFractals) {
                if (oldOne.startsWith(newOne)) {
                    result.put(new FractalIdJob(oldOne, jobName), new FractalIdJob(newOne, jobName));
                }
            }
        }
        return result;
    }

    public static List<String> fractalsForJob(Map<Integer, FractalIdJob> serventJobs, String jobName) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, FractalIdJob> entry: serventJobs.entrySet()) {
            if (entry.getValue().getJobName().equals(jobName)) {
                result.add(entry.getValue().getFractalId());
            }
        }
        Collections.sort(result);
        return result;
    }
}
