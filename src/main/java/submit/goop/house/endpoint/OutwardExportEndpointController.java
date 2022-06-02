package submit.goop.house.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import submit.goop.house.data.util.Zipper;

@RestController
@RequestMapping("/api/v1/export")
public class OutwardExportEndpointController {

    public OutwardExportEndpointController() {
    }

    @RequestMapping(value = "/token={token}&type={type}&user={user}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("token") String token, @PathVariable("type") String type, @PathVariable("user") String user) {
        if (user.equals("undefined")) {
            if(type.equals("audio")) {
                String dir = "upload/audio/";
                String zipFile = "upload/export/audio.zip";

                Zipper zip = new Zipper();
                zip.compressDirectory(dir, zipFile);
                return ResponseEntity.ok(zipFile);
            }
        }
        return ResponseEntity.ok("");
    }
}
