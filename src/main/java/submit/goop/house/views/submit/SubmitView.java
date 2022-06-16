package submit.goop.house.views.submit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.Submission;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.SubmissionService;
import submit.goop.house.data.service.UserService;
import submit.goop.house.data.util.SimpleTimer;
import submit.goop.house.endpoint.GoopEvent;
import submit.goop.house.endpoint.SubmissionsEndpoint;
import submit.goop.house.data.service.GoopUserRepository;
import submit.goop.house.views.MainLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@PageTitle("Active Submissions")
@Route(value = "submit", layout = MainLayout.class)
@RolesAllowed("user")
@Uses(Icon.class)
public class SubmitView extends Div {

    private TextField mainArtist = new TextField("Artist Name");
    private TextField title = new TextField("Title");
    private TextField coverArt = new TextField("Cover Art (Optional)");
    private TextField audioFileURL = new TextField("Audio File URL");
    //private TextField submissionID = new TextField("Submission ID");
    private UUID submissionID;

    //private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private Binder<Submission> binder = new Binder(Submission.class);

    private MemoryBuffer audioMemoryBuffer = new MemoryBuffer();
    private MemoryBuffer artMemoryBuffer = new MemoryBuffer();

    private Upload audioUpload = new Upload(audioMemoryBuffer);
    private Upload artUpload = new Upload(artMemoryBuffer);
    //private H4 audioUploadLabel = new H4("Audio File");
    //private H4 artUploadLabel = new H4("Optional Artwork");

    private String audioFileName;
    private String artFileName;
    private GoopUser authGoopUser;
    private SubmissionService submissionService;
    private User authUser;

    private SimpleTimer clock = new SimpleTimer();
    private Span time = new Span();
    private TextField event = new TextField("Event");


    public SubmitView(SubmissionService submissionService, UserService userService, GoopUserService goopUserService, GoopUserRepository goopUserRepository) {
        addClassName("submit-view");

        add(createTitle());
        add(createFormLayout());
        //add(createUploadLayout());
        try {
            add(createButtonLayout());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        binder.bindInstanceFields(this);


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GoopUser> possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
        this.authUser = userService.findByUsername(auth.getName());
        this.authGoopUser = possibleGoopUser.get(0);
//        this.submissionService = submissionService;

        if(possibleGoopUser.get(0).isActiveSubmission()) {
            UUID subID = UUID.fromString(possibleGoopUser.get(0).getSubmissions().split(",")[0]);
            this.submissionID = subID;
            setFormData(submissionService.findBySubmissionID(subID).get(0));
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info("Main Artist: " + submissionService.findBySubmissionID(subID).get(0).getMainArtist());
        }
        else {
            this.submissionID = UUID.randomUUID();
            Notification.show("You have no active submission, so a new one has been created.");
            clearForm();
        }

        //cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> {
            authGoopUser.setActiveSubmission(true);
            if(authGoopUser.getSubmissions() == null){
                authGoopUser.setSubmissions(submissionID.toString());
            }
            else{
                if(authGoopUser.getSubmissions().equals("")){
                    authGoopUser.setSubmissions(submissionID.toString());
                }
                else{
                    if(!authGoopUser.getSubmissions().contains(submissionID.toString())){
                        authGoopUser.setSubmissions(authGoopUser.getSubmissions() + "," + submissionID.toString());
                    }
                }
            }
            goopUserRepository.save(authGoopUser);
            boolean changedAudio = false;
            try {
                if(new File(audioFileName).exists()) {
                    File savedAudioFile = new File(audioFileName);
                    try {
                        Files.move(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/" + savedAudioFile.getName()), Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + savedAudioFile.getName().split("\\.")[savedAudioFile.getName().split("\\.").length - 1]));
                        audioFileURL.setValue("/uploads/audio/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + savedAudioFile.getName().split("\\.")[savedAudioFile.getName().split("\\.").length - 1]);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    changedAudio = true;
                }
            }
            catch (Exception ex) {
                System.out.println("No new audio file");
            }


            boolean changedArt = false;
            try {
                if (new File(artFileName).exists()) {
                    File savedArtFile = new File(artFileName);
                    try {
                        Files.move(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/" + savedArtFile.getName()), Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + savedArtFile.getName().split("\\.")[savedArtFile.getName().split("\\.").length - 1]));
                        coverArt.setValue("/uploads/art/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + savedArtFile.getName().split("\\.")[savedArtFile.getName().split("\\.").length - 1]);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    changedArt = true;
                }
            }
            catch (Exception ex) {
                System.out.println("No new art file");
            }

            if(!(changedAudio && changedArt)) {
                if(changedAudio) {
                    if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()))) {
                        try (Stream<Path> filePathStream = Files.walk(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()))){
                            filePathStream
                                    .filter(Files::isRegularFile)
                                    .forEach(path -> {
                                        try {
                                            Files.move(path, Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]));

                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    })
                            ;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                else if(changedArt) {
                    if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()))) {
                        try (Stream<Path> filePathStream = Files.walk(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()))){
                            filePathStream
                                    .filter(Files::isRegularFile)
                                    .forEach(path -> {
                                        try {
                                            Files.move(path, Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]));

                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    })
                            ;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                else{
                    if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()))) {
                        try (Stream<Path> filePathStream = Files.walk(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()))){
                            filePathStream
                                    .filter(Files::isRegularFile)
                                    .forEach(path -> {
                                        try {
                                            Files.move(path, Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]));
                                            audioFileURL.setValue("/uploads/audio/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    })
                            ;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()))) {
                        try (Stream<Path> filePathStream = Files.walk(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()))){
                            filePathStream
                                    .filter(Files::isRegularFile)
                                    .forEach(path -> {
                                        try {
                                            Files.move(path, Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]));
                                            coverArt.setValue("/uploads/art/" + authUser.getUsername() + "/" + mainArtist.getValue() + " - " + title.getValue() + "." + path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length - 1]);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    })
                            ;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                }
            }
            submissionService.update(binder.getBean());
            Notification.show("Your submission has been successfully saved");
            //clearForm();
        });

        audioUpload.addSucceededListener(e -> {
            String fileName = submissionID.toString() + "." + e.getFileName().split("\\.")[e.getFileName().split("\\.").length - 1];
            Submission submission = binder.getBean();
            //submission.setAudioFileURL("/uploads/audio/" + fileName);
            submission.setSubmissionID(this.submissionID);
            this.audioFileName = "src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/" + fileName;
            audioFileURL.setValue("/uploads/art/" + authUser.getUsername() + "/" + fileName);
            InputStream inputStream = audioMemoryBuffer.getInputStream();
            try {
                saveAudioFileToDisk(inputStream, fileName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } );
        artUpload.addSucceededListener(e -> {
            String fileName = submissionID.toString() + "." + e.getFileName().split("\\.")[e.getFileName().split("\\.").length - 1];
            this.artFileName = "src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/" + fileName;
            coverArt.setValue("/uploads/art/" + authUser.getUsername() + "/" + fileName);
            InputStream inputStream = artMemoryBuffer.getInputStream();
            try {
                saveArtFileToDisk(inputStream, fileName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } );

        audioUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    2000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        artUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    2000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        SubmissionsEndpoint submissionsEndpoint = new SubmissionsEndpoint();
        GoopEvent goopEvent = null;
        try {
            goopEvent = submissionsEndpoint.getActiveEvent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(goopEvent != null) {
            event.setValue(goopEvent.getName());
        }
        else {
            event.setValue("No Active Event");
        }

        clock.start();
    }

    private void setFormData(Submission submission) {
        Notification.show("You already have an active submission, so it has been loaded into the form.");
        binder.setBean(submission);
    }

    private String saveAudioFileToDisk(InputStream inputStream, String fileName) throws IOException {
        if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()))) {

            FileUtils.deleteDirectory(new File("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()));
            Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()));

            File file = new File("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/", fileName);
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        }
        else {
            Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername()));
            File file = new File("src/main/resources/META-INF/resources/uploads/audio/" + authUser.getUsername() + "/", fileName);
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        }
    }
    private String saveArtFileToDisk(InputStream inputStream, String fileName) throws IOException {
        if(Files.exists(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()))) {

            FileUtils.deleteDirectory(new File("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()));
            Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()));

            File file = new File("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/", fileName);
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        }
        else {
            Files.createDirectory(Paths.get("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername()));
            File file = new File("src/main/resources/META-INF/resources/uploads/art/" + authUser.getUsername() + "/", fileName);
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        }
    }

    private void clearForm() {
        binder.setBean(new Submission());
    }

    private Component createTitle() {
        return new H3("Your Submission");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();

        //email.setErrorMessage("Please enter a valid email address");
        mainArtist.setRequiredIndicatorVisible(true);
        mainArtist.setErrorMessage("This field is required");
        mainArtist.setRequired(true);
        title.setRequiredIndicatorVisible(true);
        title.setErrorMessage("This field is required");
        title.setRequired(true);
        audioFileURL.setRequiredIndicatorVisible(true);
        audioFileURL.setRequired(true);
        audioFileURL.setErrorMessage("This field is required");
        audioFileURL.setReadOnly(true);
        coverArt.setReadOnly(true);

        audioUpload.setDropAllowed(true);
        artUpload.setDropAllowed(true);
        audioUpload.setAcceptedFileTypes("audio/mp3", "audio/mpeg", "audio/wav", "audio/x-wav", "audio/ogg", "audio/webm");
        artUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/gif", "image/tiff", "image/svg+xml", "image/webp");
        event.setRequired(true);
        event.setRequiredIndicatorVisible(true);
        event.setErrorMessage("This field is required");
        event.setReadOnly(true);
        formLayout.add(mainArtist, title, audioFileURL, coverArt, audioUpload, artUpload, event);
        //submissionID.setReadOnly(true);
        //submissionID.setValue(UUID.randomUUID().toString());
        //formLayout.add(mainArtist, title, audioFileURL, coverArt, audioUpload, artUpload);
        return formLayout;
    }

    private Component createButtonLayout() throws Exception {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Label label = new Label("No active event.");
        label.addClassName("no-event-label");
        SubmissionsEndpoint submissionsEndpoint = new SubmissionsEndpoint();
        GoopEvent goopEvent = submissionsEndpoint.getActiveEvent();
        if(goopEvent != null) {
            String endTime = goopEvent.getEndTime();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(endTime.split("-")[2]), Integer.parseInt(endTime.split("-")[1])-1, Integer.parseInt(endTime.split("-")[0]), Integer.parseInt(endTime.split("-")[3]), Integer.parseInt(endTime.split("-")[4]), Integer.parseInt(endTime.split("-")[5]));
            if (!calendar.getTime().after(new Date())) {
                save.setEnabled(false);
                label.setText("Time's up! Contact a mod if you have a valid reason you could not submit.");
                buttonLayout.add(save);
                buttonLayout.add(label);
            }
            else {
                clock.setFractions(false);
                clock.setDays(true);
                clock.setHours(true);
                clock.setCountEvent(goopEvent.getName());
                clock.setStartTime((calendar.getTimeInMillis()/1000)-(Calendar.getInstance().getTimeInMillis()/1000));
                clock.addClassName("clock-style");
                buttonLayout.add(save);
                buttonLayout.add(clock);
                //clock.setFormat("%d days, %h hours, %m minutes and %s seconds until your submission is due!");
            }
        }
        else {
            save.setEnabled(false);
            buttonLayout.add(save);
            buttonLayout.add(label);
        }


        return buttonLayout;
    }

    private Component createUploadLayout(){
        HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.addClassName("upload-layout");
        audioUpload.setDropAllowed(true);
        artUpload.setDropAllowed(true);
        //audioUpload.setAcceptedFileTypes("audio/mp3", "audio/mpeg", "audio/wav", "audio/x-wav", "audio/ogg", "audio/webm");
        //artUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/gif", "image/tiff", "image/svg+xml", "image/webp");
        uploadLayout.add(/*audioUploadLabel,*/ audioUpload);
        uploadLayout.add(/*artUploadLabel,*/ artUpload);
        return uploadLayout;
    }

    private static class PhoneNumberField extends CustomField<String> {
        private ComboBox<String> countryCode = new ComboBox<>();
        private TextField number = new TextField();

        public PhoneNumberField(String label) {
            setLabel(label);
            countryCode.setWidth("120px");
            countryCode.setPlaceholder("Country");
            countryCode.setPattern("\\+\\d*");
            countryCode.setPreventInvalidInput(true);
            countryCode.setItems("+1", "+91", "+62", "+98", "+964", "+353", "+44", "+972", "+39", "+225");
            countryCode.addCustomValueSetListener(e -> countryCode.setValue(e.getDetail()));
            number.setPattern("\\d*");
            number.setPreventInvalidInput(true);
            HorizontalLayout layout = new HorizontalLayout(countryCode, number);
            layout.setFlexGrow(1.0, number);
            add(layout);
        }

        @Override
        protected String generateModelValue() {
            if (countryCode.getValue() != null && number.getValue() != null) {
                String s = countryCode.getValue() + " " + number.getValue();
                return s;
            }
            return "";
        }

        @Override
        protected void setPresentationValue(String phoneNumber) {
            String[] parts = phoneNumber != null ? phoneNumber.split(" ", 2) : new String[0];
            if (parts.length == 1) {
                countryCode.clear();
                number.setValue(parts[0]);
            } else if (parts.length == 2) {
                countryCode.setValue(parts[0]);
                number.setValue(parts[1]);
            } else {
                countryCode.clear();
                number.clear();
            }
        }
    }

}
