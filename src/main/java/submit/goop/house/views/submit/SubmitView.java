package submit.goop.house.views.submit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Lob;

import io.swagger.models.auth.In;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestAttribute;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.Submission;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.SubmissionService;
import submit.goop.house.data.service.UserService;
import submit.goop.house.views.MainLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@PageTitle("Manage Active Submission or new Submission")
@Route(value = "submit", layout = MainLayout.class)
@RolesAllowed("user")
@Uses(Icon.class)
public class SubmitView extends Div {

    private TextField mainArtist = new TextField("Artist Name");
    private TextField title = new TextField("Title");
    private TextField coverArt = new TextField("Cover Art");
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

    private GoopUser authGoopUser;
    private SubmissionService submissionService;


    public SubmitView(SubmissionService submissionService, UserService userService, GoopUserService goopUserService) {
        addClassName("submit-view");

        add(createTitle());
        add(createFormLayout());
        //add(createUploadLayout());
        add(createButtonLayout());

        binder.bindInstanceFields(this);


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GoopUser> possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
//        this.authGoopUser = possibleGoopUser.get(0);
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
            submissionService.update(binder.getBean());
            Notification.show("Your submission has been successfully saved");
            //clearForm();
        });

        audioUpload.addSucceededListener(e -> {
            String fileName = submissionID + " || " + e.getFileName();
            Submission submission = binder.getBean();
            //submission.setAudioFileURL("/uploads/audio/" + fileName);
            submission.setSubmissionID(this.submissionID);

            audioFileURL.setValue("/uploads/audio/" + fileName);
            InputStream inputStream = audioMemoryBuffer.getInputStream();
            try {
                saveAudioFileToDisk(inputStream, fileName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } );
        artUpload.addSucceededListener(e -> {
            String fileName = submissionID.toString() + " || " + e.getFileName();
            coverArt.setValue("/uploads/art/" + fileName);
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
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        artUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    private void setFormData(Submission submission) {
        Notification.show("You already have an active submission, so it has been loaded into the form.");
        binder.setBean(submission);
    }

    private String saveAudioFileToDisk(InputStream inputStream, String fileName) throws IOException {
        File file = new File("/Users/bardia/IdeaProjects/goop-house-submissions/uploads/audio/", fileName);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file.getAbsolutePath();
    }
    private String saveArtFileToDisk(InputStream inputStream, String fileName) throws IOException {
        File file = new File("/Users/bardia/IdeaProjects/goop-house-submissions/uploads/art/", fileName);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file.getAbsolutePath();
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
        //audioUpload.setAcceptedFileTypes("audio/mp3", "audio/mpeg", "audio/wav", "audio/x-wav", "audio/ogg", "audio/webm");
        //artUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/gif", "image/tiff", "image/svg+xml", "image/webp");
        //submissionID.setReadOnly(true);
        //submissionID.setValue(UUID.randomUUID().toString());
        formLayout.add(mainArtist, title, audioFileURL, coverArt, audioUpload, artUpload);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        //buttonLayout.add(cancel);
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
