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
@RequestMapping("/api/v1/manage")
public class OutwardUserManagementEndpointController {

    UserService userService;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    public OutwardUserManagementEndpointController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping(value = "/token={token}&action={action}&user={user}&input={input}", produces = MediaType.APPLICATION_JSON_VALUE)
    //@JsonView(View.Public.class)
    public ResponseEntity<String> get(@PathVariable("token") String token, @PathVariable("action") String action, @PathVariable("user") String user, @PathVariable("input") String input) {
        User possibleUser = userService.findByUsername(user);
        if(action.equals("create")) {
            if(possibleUser != null) {
                return ResponseEntity.ok("User already exists");
            }
            else {
                possibleUser = new User();
            }
        }
        if(token.equals("ofghap8734yrpawy4prha4fkaiwyg4of7ya0w4fhoaw4hfah4974hpa9w4"))
            if (possibleUser != null) {
                try {
                    switch (action) {
                        case "remove":
                            userRepository.delete(possibleUser);
                            return ResponseEntity.ok("Successfully removed user");
                        case "create":
                            String[] realInput = input.split(",");
                            possibleUser.setUsername(user);
                            possibleUser.setHashedPassword(passwordEncoder.encode(realInput[0]));
                            possibleUser.setRoles(Collections.singleton(Role.USER));
                            possibleUser.setProfilePictureUrl("https://" + realInput[1].replace("-", "/"));
                            possibleUser.setName(realInput[2]
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
                            userRepository.save(possibleUser);
                            return ResponseEntity.ok("Successfully created user");
                        case "editUsername":
                            possibleUser.setUsername(input);
                            return ResponseEntity.ok("Successfully edited user");
                        case "editName":
                            possibleUser.setName(input);
                            return ResponseEntity.ok("Successfully edited user");
                        case "editPassword":
                            possibleUser.setHashedPassword(passwordEncoder.encode(input));
                            return ResponseEntity.ok("Successfully edited user");
                    }
                    return ResponseEntity.ok("Invalid action");

                } catch (Exception e) {
                    return ResponseEntity.ok("Failed");
                }
            } else {
                return ResponseEntity.ok("User does not exist");
            }
        else {
            return ResponseEntity.ok("Invalid auth");

        }
    }
}
