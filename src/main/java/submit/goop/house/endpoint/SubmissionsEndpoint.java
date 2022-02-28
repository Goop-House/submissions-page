package submit.goop.house.endpoint;

import java.util.ArrayList;
import java.util.HashMap;

public class SubmissionsEndpoint {
    private String currentDefaultCoverArtURL;
    private HashMap<String, String> eventArtURLs = new HashMap<>();

    public SubmissionsEndpoint() {
        this.currentDefaultCoverArtURL = "https://pbs.twimg.com/media/FKiwPD-VIAAOQkp?format=jpg&name=medium";
        this.eventArtURLs.put("Goop Week 6", "https://pbs.twimg.com/media/FKiwPD-VIAAOQkp?format=jpg&name=medium");
    }

    public String getCurrentDefaultCoverArt() {
        return currentDefaultCoverArtURL;
    }

    public String getEventArt(String eventName) {
        return eventArtURLs.getOrDefault(eventName, currentDefaultCoverArtURL);
    }
}
