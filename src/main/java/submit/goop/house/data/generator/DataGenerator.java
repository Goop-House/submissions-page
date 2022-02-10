package submit.goop.house.data.generator;

import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
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

            logger.info("... generating 2 User entities...");
            User user = new User();
            user.setName("John Normal");
            user.setUsername("user");
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);
            User admin = new User();
            admin.setName("Emma Powerful");
            admin.setUsername("admin");
            admin.setHashedPassword(passwordEncoder.encode("admin"));
            admin.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            admin.setRoles(Stream.of(Role.USER, Role.ADMIN).collect(Collectors.toSet()));
            userRepository.save(admin);
            logger.info("... generating 100 Submission entities...");
            ExampleDataGenerator<Submission> submissionRepositoryGenerator = new ExampleDataGenerator<>(
                    Submission.class, LocalDateTime.of(2022, 2, 2, 0, 0, 0));
            submissionRepositoryGenerator.setData(Submission::setCoverArt, DataType.PROFILE_PICTURE_URL);
            submissionRepositoryGenerator.setData(Submission::setAudioFileURL, DataType.PROFILE_PICTURE_URL);
            submissionRepositoryGenerator.setData(Submission::setSubmissionID, DataType.UUID);
            submissionRepositoryGenerator.setData(Submission::setMainArtist, DataType.FIRST_NAME);
            submissionRepositoryGenerator.setData(Submission::setTitle, DataType.WORD);
            submissionRepository.saveAll(submissionRepositoryGenerator.create(100, seed));

            logger.info("... generating 100 Goop User entities...");
            ExampleDataGenerator<GoopUser> goopUserRepositoryGenerator = new ExampleDataGenerator<>(GoopUser.class,
                    LocalDateTime.of(2022, 2, 2, 0, 0, 0));
            goopUserRepositoryGenerator.setData(GoopUser::setDiscordID, DataType.UUID);
            goopUserRepositoryGenerator.setData(GoopUser::setArtistName, DataType.FIRST_NAME);
            goopUserRepositoryGenerator.setData(GoopUser::setPronouns, DataType.WORD);
            goopUserRepositoryGenerator.setData(GoopUser::setEmail, DataType.EMAIL);
            goopUserRepositoryGenerator.setData(GoopUser::setPhone, DataType.PHONE_NUMBER);
            goopUserRepositoryGenerator.setData(GoopUser::setSubmissions, DataType.TWO_WORDS);
            goopUserRepositoryGenerator.setData(GoopUser::setActiveSubmission, DataType.BOOLEAN_50_50);
            goopUserRepository.saveAll(goopUserRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}