package submit.goop.house.data.generator;

import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import submit.goop.house.data.Role;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.Submission;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.GoopUserRepository;
import submit.goop.house.data.service.SubmissionRepository;
import submit.goop.house.data.service.UserRepository;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
            SubmissionRepository submissionRepository, GoopUserRepository goopUserRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 3 User entities...");
            User user = new User();
            user.setName("TheLickIn13Alts#8075");
            user.setUsername("856445161059385364");
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://github.com/TheLickIn13Keys/profile-pictures/blob/main/kirbypfp.png?raw=true");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);


            User admin = new User();
            admin.setName("TheLickIn13Keys#7977");
            admin.setUsername("620845493957951498");
            admin.setHashedPassword(passwordEncoder.encode("admin"));
            admin.setProfilePictureUrl(
                    "https://github.com/TheLickIn13Keys/profile-pictures/blob/main/kirbypfp.png?raw=true");
            admin.setRoles(Stream.of(Role.USER, Role.ADMIN).collect(Collectors.toSet()));
            userRepository.save(admin);

            GoopUser goopUser = new GoopUser();
            goopUser.setDiscordID("620845493957951498");
            goopUser.setArtistName("TheLickIn13Keys");
            goopUser.setPronouns("he/him");
            goopUser.setEmail("bardiaanvari10@gmail.com");
            goopUser.setPhone("9253846745");
            goopUser.setSubmissions("3fc41c69-62c1-409c-a4a5-185baedfebfa,f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8,f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8");
            goopUser.setActiveSubmission(true);
            goopUserRepository.save(goopUser);

            Submission submission = new Submission();
            submission.setSubmissionID(UUID.fromString("3fc41c69-62c1-409c-a4a5-185baedfebfa"));
            submission.setMainArtist("TheLickIn13Keys");
            submission.setTitle("Joe Biden Mixtape");
            submission.setEvent("Goop Week 6");
            submissionRepository.save(submission);


//            logger.info("... generating 100 Submission entities...");
//            ExampleDataGenerator<Submission> submissionRepositoryGenerator = new ExampleDataGenerator<>(
//                    Submission.class, LocalDateTime.of(2022, 2, 2, 0, 0, 0));
//            submissionRepositoryGenerator.setData(Submission::setCoverArt, DataType.PROFILE_PICTURE_URL);
//            submissionRepositoryGenerator.setData(Submission::setAudioFileURL, DataType.PROFILE_PICTURE_URL);
//            submissionRepositoryGenerator.setData(Submission::setSubmissionID, DataType.UUID);
//            submissionRepositoryGenerator.setData(Submission::setMainArtist, DataType.FIRST_NAME);
//            submissionRepositoryGenerator.setData(Submission::setTitle, DataType.WORD);
//            submissionRepository.saveAll(submissionRepositoryGenerator.create(100, seed));
//
//            logger.info("... generating 100 Goop User entities...");
//            ExampleDataGenerator<GoopUser> goopUserRepositoryGenerator = new ExampleDataGenerator<>(GoopUser.class,
//                    LocalDateTime.of(2022, 2, 2, 0, 0, 0));
//            goopUserRepositoryGenerator.setData(GoopUser::setDiscordID, DataType.WORD);
//            goopUserRepositoryGenerator.setData(GoopUser::setArtistName, DataType.FIRST_NAME);
//            goopUserRepositoryGenerator.setData(GoopUser::setPronouns, DataType.WORD);
//            goopUserRepositoryGenerator.setData(GoopUser::setEmail, DataType.EMAIL);
//            goopUserRepositoryGenerator.setData(GoopUser::setPhone, DataType.PHONE_NUMBER);
//            goopUserRepositoryGenerator.setData(GoopUser::setSubmissions, DataType.TWO_WORDS);
//            goopUserRepositoryGenerator.setData(GoopUser::setActiveSubmission, DataType.BOOLEAN_50_50);
//            goopUserRepository.saveAll(goopUserRepositoryGenerator.create(100, seed));
//
//            logger.info("Generated demo data");
        };
    }

}