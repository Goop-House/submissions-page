package submit.goop.house.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import submit.goop.house.data.util.Zipper;

import java.time.LocalTime;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/export")
public class OutwardExportEndpointController {

    public OutwardExportEndpointController() {
    }

    @RequestMapping(value = "/token={token}&type={type}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("token") String token, @PathVariable("type") String type) {
        if(token.equals(System.getenv("ADMIN_TOKEN"))) {
            if (type.equals("audio")) {
                String dir = "src/main/resources/META-INF/resources/uploads/export/";
                String zipFile = "audio-" + LocalTime.now() + ".zip";

                Zipper zip = new Zipper();
                zip.compressDirectory(dir, zipFile);
                return ResponseEntity.ok(zipFile);
            }
            else if(type.equals("art")) {
                String dir = "src/main/resources/META-INF/resources/uploads/export/";
                String zipFile = "art-" + LocalTime.now() + ".zip";

                Zipper zip = new Zipper();
                zip.compressDirectory(dir, zipFile);
                return ResponseEntity.ok(zipFile);
            }
            else {
                return ResponseEntity.badRequest().body("Invalid type");
            }

        }
        else {
            return ResponseEntity.ok("Invalid token");
        }
    }
}
