package submit.goop.house.endpoint;

import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import submit.goop.house.data.util.Zipper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/export")
public class OutwardExportEndpointController {


    public OutwardExportEndpointController() {

    }

    @RequestMapping(value = "/token={token}&type={type}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("token") String token, @PathVariable("type") String type) throws IOException {
        if(token.equals(System.getenv("TOKEN"))) {
            if (type.equals("audio")) {
                String tempDir = "src/main/resources/META-INF/resources/uploads/audio/temp/";
                String zipFile = "src/main/resources/META-INF/resources/uploads/export/" + "audio-" + LocalTime.now() + ".zip";
                Zipper zip = new Zipper();

                Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/audio/temp/"));
                List<File> files = zip.getFiles(new File("src/main/resources/META-INF/resources/uploads/audio/"));
                for (File file : files) {
                    FileUtils.copyFile(file, new File("src/main/resources/META-INF/resources/uploads/audio/temp/" + file.getName()));
                }

                zip.compressDirectory(tempDir, zipFile);
                
                FileUtils.deleteDirectory(new File(tempDir));

                return ResponseEntity.ok(zipFile);
            }
            else if(type.equals("art")) {
                String tempDir = "src/main/resources/META-INF/resources/uploads/art/temp/";
                String zipFile = "src/main/resources/META-INF/resources/uploads/export/" + "art-" + LocalTime.now() + ".zip";
                Zipper zip = new Zipper();

                Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/art/temp/"));
                List<File> files = zip.getFiles(new File("src/main/resources/META-INF/resources/uploads/art/"));
                for (File file : files) {
                    FileUtils.copyFile(file, new File("src/main/resources/META-INF/resources/uploads/art/temp/" + file.getName()));
                }

                zip.compressDirectory(tempDir, zipFile);

                FileUtils.deleteDirectory(new File(tempDir));

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
