package submit.goop.house.endpoint;


public class GoopEvent {

    private String name;
    private String coverURL;
    private String startTime;
    private String endTime;
    private boolean isActive;

    public GoopEvent() {

    }

    public GoopEvent(String name, String coverURL, String startTime, String endTime, boolean isActive) {
        this.name = name;
        this.coverURL = coverURL;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
