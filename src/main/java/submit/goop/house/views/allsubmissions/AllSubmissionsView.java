package submit.goop.house.views.allsubmissions;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToUuidConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;
import submit.goop.house.data.entity.Submission;
import submit.goop.house.data.service.SubmissionService;
import submit.goop.house.views.MainLayout;

@PageTitle("All Submissions")
@Route(value = "allsubmissions/:submissionID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("admin")
public class AllSubmissionsView extends Div implements BeforeEnterObserver {

    private final String SUBMISSION_ID = "submissionID";
    private final String SUBMISSION_EDIT_ROUTE_TEMPLATE = "allsubmissions/%s/edit";

    private Grid<Submission> grid = new Grid<>(Submission.class, false);

    CollaborationAvatarGroup avatarGroup;

    private Upload coverArt;
    private Image coverArtPreview;
    private Upload audioFileURL;
    private Image audioFileURLPreview;
    private TextField submissionID;
    private TextField mainArtist;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<Submission> binder;

    private Submission submission;

    private SubmissionService submissionService;

    public AllSubmissionsView(@Autowired SubmissionService submissionService) {
        this.submissionService = submissionService;
        addClassNames("all-submissions-view", "flex", "flex-col", "h-full");

        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set("visibility", "hidden");

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<Submission> coverArtRenderer = LitRenderer.<Submission>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.coverArt} /></span>")
                .withProperty("coverArt", Submission::getCoverArt);
        grid.addColumn(coverArtRenderer).setHeader("Cover Art").setWidth("96px").setFlexGrow(0);

        LitRenderer<Submission> audioFileURLRenderer = LitRenderer.<Submission>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.audioFileURL} /></span>")
                .withProperty("audioFileURL", Submission::getAudioFileURL);
        grid.addColumn(audioFileURLRenderer).setHeader("Audio File URL").setWidth("96px").setFlexGrow(0);

        grid.addColumn("submissionID").setAutoWidth(true);
        grid.addColumn("mainArtist").setAutoWidth(true);
        grid.setItems(query -> submissionService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SUBMISSION_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AllSubmissionsView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(Submission.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(submissionID, String.class).withConverter(new StringToUuidConverter("Invalid UUID"))
                .bind("submissionID");

        binder.bindInstanceFields(this);

        attachImageUpload(coverArt, coverArtPreview);
        attachImageUpload(audioFileURL, audioFileURLPreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.submission == null) {
                    this.submission = new Submission();
                }
                binder.writeBean(this.submission);
                this.submission.setCoverArt(coverArtPreview.getSrc());
                this.submission.setAudioFileURL(audioFileURLPreview.getSrc());

                submissionService.update(this.submission);
                clearForm();
                refreshGrid();
                Notification.show("Submission details stored.");
                UI.getCurrent().navigate(AllSubmissionsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the submission details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> submissionId = event.getRouteParameters().get(SUBMISSION_ID).map(UUID::fromString);
        if (submissionId.isPresent()) {
            Optional<Submission> submissionFromBackend = submissionService.get(submissionId.get());
            if (submissionFromBackend.isPresent()) {
                populateForm(submissionFromBackend.get());
            } else {
                Notification.show(String.format("The requested submission was not found, ID = %d", submissionId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AllSubmissionsView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        Label coverArtLabel = new Label("Cover Art");
        coverArtPreview = new Image();
        coverArtPreview.setWidth("100%");
        coverArt = new Upload();
        coverArt.getStyle().set("box-sizing", "border-box");
        coverArt.getElement().appendChild(coverArtPreview.getElement());
        Label audioFileURLLabel = new Label("Audio File URL");
        audioFileURLPreview = new Image();
        audioFileURLPreview.setWidth("100%");
        audioFileURL = new Upload();
        audioFileURL.getStyle().set("box-sizing", "border-box");
        audioFileURL.getElement().appendChild(audioFileURLPreview.getElement());
        submissionID = new TextField("Submission ID");
        mainArtist = new TextField("Main Artist");
        Component[] fields = new Component[]{coverArtLabel, coverArt, audioFileURLLabel, audioFileURL, submissionID,
                mainArtist};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(avatarGroup, formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Submission value) {
        this.submission = value;
        String topic = null;
        if (this.submission != null && this.submission.getId() != null) {
            topic = "submission/" + this.submission.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.submission);
        avatarGroup.setTopic(topic);
        this.coverArtPreview.setVisible(value != null);
        if (value == null) {
            this.coverArtPreview.setSrc("");
        } else {
            this.coverArtPreview.setSrc(value.getCoverArt());
        }
        this.audioFileURLPreview.setVisible(value != null);
        if (value == null) {
            this.audioFileURLPreview.setSrc("");
        } else {
            this.audioFileURLPreview.setSrc(value.getAudioFileURL());
        }

    }
}