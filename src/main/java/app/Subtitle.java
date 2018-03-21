package app;


public class Subtitle implements Comparable<Subtitle>{
    private double startTime;
    private double endTime;
    private String ccData;
    private boolean started;
    private boolean ended;

    public Subtitle() {
        this.started = false;
        this.ended = false;
        this.ccData = "";
    }

    public String getCcData() {
        return ccData;
    }

    public void setCcData(String ccData) {
        this.ccData = ccData;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    @Override
    public int compareTo(Subtitle o) {
        if (this.startTime == o.startTime) {
            return 0;
        }
        return this.startTime < o.startTime ? -1 : 1;
    }
}
