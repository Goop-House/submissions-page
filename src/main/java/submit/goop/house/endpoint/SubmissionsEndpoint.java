package submit.goop.house.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubmissionsEndpoint {
    private String currentDefaultCoverArtURL;
    private List<GoopEvent> eventList;

    public SubmissionsEndpoint() {
        EventManagementController controller = new EventManagementController();
        this.currentDefaultCoverArtURL = "https://cdn.discordapp.com/attachments/834541919568527361/874767069772660736/goop_house_logo_for_disc.png";
    }

    public String getEventArt(String eventName) throws Exception {
        this.eventList = new EventManagementController().getEvents();
        for (GoopEvent event : this.eventList) {
            if (event.getName().equals(eventName)) {
                return event.getCoverURL();
            }
        }
        return this.currentDefaultCoverArtURL;
    }
}
