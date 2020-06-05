package cli.command.chaos_game;

import app.AppConfig;
import app.models.*;
import app.util.JobUtil;
import cli.CLIParser;
import cli.command.CLICommand;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class StartCommand implements CLICommand {

    private CLIParser parser;

    public StartCommand(CLIParser parser) {
        this.parser = parser;
    }

    @Override
    public String commandName() {
        return "start";
    }

    @Override
    public void execute(String args) {
        int serventCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
        int activeJobsCount = AppConfig.chordState.getActiveJobsCount();

        // todo: dodati ovo kad neko quittuje
        if (serventCount < 1 || serventCount < activeJobsCount + 1) {
            AppConfig.timestampedErrorPrint("There are not enough servents to execute jobs.");
            return;
        }

        Job job;
        if (args == null) {    // enter data for new job via CLI
            Scanner scanner = parser.getScanner();

            AppConfig.timestampedStandardPrint("Enter fractal job name: ");
            String jobName = scanner.nextLine().trim();
            try {
                AppConfig.timestampedStandardPrint("Enter number of points: ");
                int pointsCount = scanner.nextInt();
                scanner.nextLine();

                AppConfig.timestampedStandardPrint("Enter fractal proportion: ");
                double proportion = scanner.nextDouble();
                scanner.nextLine();

                AppConfig.timestampedStandardPrint("Enter image width and height separated by space: ");
                String[] imageDimensions = scanner.nextLine().trim().split(" ");
                int width = Integer.parseInt(imageDimensions[0]);
                int height = Integer.parseInt(imageDimensions[1]);

                List<Point> startPoints = new ArrayList<>();
                AppConfig.timestampedStandardPrint("Enter coordinates of points for fractal. " +
                        "Each point should be in new line with x and y coordinates separated by space: ");
                for (int i = 0; i < pointsCount; i++) {
                    String[] xy = scanner.nextLine().trim().split(" ");
                    startPoints.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
                }

                job = new Job(jobName, pointsCount, proportion, width, height, startPoints);
            } catch (NumberFormatException | InputMismatchException e) {
                AppConfig.timestampedErrorPrint("Problem reading data for the job. Exiting start job...");
                return;
            }
        } else {    // find job in your jobs loaded from config
            job = AppConfig.myServentInfo.getJobForName(args);
            if (job == null) {
                AppConfig.timestampedErrorPrint("Job with name \'" + args + "\" not found in the list of jobs.");
                return;
            }
        }

        if (AppConfig.chordState.getActiveJobsList().contains(job)) {
            AppConfig.timestampedErrorPrint("Job with name \'" + job.getName() + "\' already exist.");
            return;
        }

        // compute number of servents needed for each job
        AppConfig.chordState.addNewJob(job);
        JobUtil.executeJobScheduling(serventCount, JobScheduleType.JOB_ADDED);
    }
}
