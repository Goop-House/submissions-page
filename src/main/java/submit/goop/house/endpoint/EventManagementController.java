package submit.goop.house.endpoint;


import org.json.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EventManagementController {

    private List<GoopEvent> eventList = new ArrayList<>();

    public EventManagementController() {
    }

    //"http://localhost:55554/api/v1/events/"
    public List<GoopEvent> getEvents() throws Exception {
        String jsonGoopEventArray = makeRequest(System.getenv("ENDPNT_URL"));
        JSONArray jsonArray = new JSONArray(jsonGoopEventArray);
        for (int i = 0; i < jsonArray.length(); i++) {
            GoopEvent goopEvent = new GoopEvent();
            goopEvent.setCoverURL(jsonArray.getJSONObject(i).getString("coverURL"));
            goopEvent.setName(jsonArray.getJSONObject(i).getString("name"));
            goopEvent.setStartTime(jsonArray.getJSONObject(i).getString("startTime"));
            goopEvent.setEndTime(jsonArray.getJSONObject(i).getString("endTime"));
            goopEvent.setActive(jsonArray.getJSONObject(i).getBoolean("isActive"));
            eventList.add(goopEvent);
        }

        return eventList;

    }

    private String makeRequest(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }


}
