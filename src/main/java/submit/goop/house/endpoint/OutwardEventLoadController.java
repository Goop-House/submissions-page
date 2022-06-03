package submit.goop.house.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/events")
public class OutwardEventLoadController {

    @RequestMapping(value = "/token={token}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("token") String token) throws Exception {
        EventManagementController eventManagementController = new EventManagementController();
        eventManagementController.getEvents();
        return ResponseEntity.ok("OK");
    }
}
