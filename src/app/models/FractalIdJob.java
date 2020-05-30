package app.models;

import java.io.Serializable;
import java.util.Objects;

public class FractalIdJob implements Serializable {

    private static final long serialVersionUID = -2752752347614204986L;

    private String fractalId;
    private String jobName;

    public FractalIdJob(String fractalId, String jobName) {
        this.fractalId = fractalId;
        this.jobName = jobName;
    }

    public String getFractalId() {
        return fractalId;
    }

    public String getJobName() {
        return jobName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FractalIdJob that = (FractalIdJob) o;
        return Objects.equals(fractalId, that.fractalId) &&
                Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fractalId, jobName);
    }

    @Override
    public String toString() {
        return "FractalIdJob{" +
                "fractalId='" + fractalId + '\'' +
                ", jobName='" + jobName + '\'' +
                '}';
    }
}
