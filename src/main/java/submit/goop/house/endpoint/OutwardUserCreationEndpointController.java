package submit.goop.house.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import submit.goop.house.data.Role;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.UserRepository;
import submit.goop.house.data.service.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/endpoint")
public class OutwardUserCreationEndpointController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    public OutwardUserCreationEndpointController(PasswordEncoder passwordEncoder, UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @RequestMapping(value = "/discordID={id}&password={pass}&discordTag={tag}&discordPicture={pic}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("id") String id, @PathVariable("tag") String tag, @PathVariable("pic") String pic, @PathVariable("pass") String pass) {
        User possibleUser = userService.findByUsername(id);
        if (possibleUser == null) {
            try {
                User user = new User();
                user.setName(tag
                        .replace("^space^", " ")
                        .replace("^hash^", "#")
                        .replace("^lt^", "<")
                        .replace("^gt^", ">")
                        .replace("^amp^", "&")
                        .replace("^pipe^", "|")
                        .replace("^slash^", "/")
                        .replace("^backslash^", "\\")
                        .replace("^question^", "?")
                        .replace("^star^", "*")
                        .replace("^quote^", "\"")
                        .replace("^apos^", "'")
                        .replace("^backtick^", "`")
                        .replace("^dollar^", "$")
                        .replace("^at^", "@")
                        .replace("^exclamation^", "!")
                );
                user.setUsername(id);
                user.setHashedPassword(passwordEncoder.encode(pass
                        .replace("^space^", " ")
                        .replace("^hash^", "#")
                        .replace("^lt^", "<")
                        .replace("^gt^", ">")
                        .replace("^amp^", "&")
                        .replace("^pipe^", "|")
                        .replace("^slash^", "/")
                        .replace("^backslash^", "\\")
                        .replace("^question^", "?")
                        .replace("^star^", "*")
                        .replace("^quote^", "\"")
                        .replace("^apos^", "'")
                        .replace("^backtick^", "`")
                        .replace("^dollar^", "$")
                        .replace("^at^", "@")
                        .replace("^exclamation^", "!")));
                user.setProfilePictureUrl("https://" + pic.replace("-", "/"));
                user.setRoles(Collections.singleton(Role.USER));
                userRepository.save(user);
                return ResponseEntity.ok("Successfully created user");
            } catch (Exception e) {
                return ResponseEntity.ok("Failed to create user");
            }
        } else {
            return ResponseEntity.ok("User already exists");
        }
    }
}
