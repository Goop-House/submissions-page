package submit.goop.house.views.allusers;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToUuidConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.views.MainLayout;

@PageTitle("All Users")
@Route(value = "allusers/:goopUserID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("admin")
@Uses(Icon.class)
public class AllUsersView extends Div implements BeforeEnterObserver {

    private final String GOOPUSER_ID = "goopUserID";
    private final String GOOPUSER_EDIT_ROUTE_TEMPLATE = "allusers/%s/edit";

    private Grid<GoopUser> grid = new Grid<>(GoopUser.class, false);

    CollaborationAvatarGroup avatarGroup;

    private TextField discordID;
    private TextField artistName;
    private TextField pronouns;
    private TextField email;
    private TextField phone;
    private TextField submissions;
    private Checkbox activeSubmission;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<GoopUser> binder;

    private GoopUser goopUser;

    private GoopUserService goopUserService;

    public AllUsersView(@Autowired GoopUserService goopUserService) {
        this.goopUserService = goopUserService;
        addClassNames("all-users-view", "flex", "flex-col", "h-full");

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
        grid.addColumn("discordID").setAutoWidth(true);
        grid.addColumn("artistName").setAutoWidth(true);
        grid.addColumn("pronouns").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("submissions").setAutoWidth(true);
        LitRenderer<GoopUser> activeSubmissionRenderer = LitRenderer.<GoopUser>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", activeSubmission -> activeSubmission.isActiveSubmission() ? "check" : "minus")
                .withProperty("color",
                        activeSubmission -> activeSubmission.isActiveSubmission()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(activeSubmissionRenderer).setHeader("Active Submission").setAutoWidth(true);

        grid.setItems(query -> goopUserService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(GOOPUSER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AllUsersView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(GoopUser.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(discordID, String.class).withConverter(new StringToUuidConverter("Invalid UUID"))
                .bind("discordID");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.goopUser == null) {
                    this.goopUser = new GoopUser();
                }
                binder.writeBean(this.goopUser);

                goopUserService.update(this.goopUser);
                clearForm();
                refreshGrid();
                Notification.show("GoopUser details stored.");
                UI.getCurrent().navigate(AllUsersView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the goopUser details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> goopUserId = event.getRouteParameters().get(GOOPUSER_ID).map(UUID::fromString);
        if (goopUserId.isPresent()) {
            Optional<GoopUser> goopUserFromBackend = goopUserService.get(goopUserId.get());
            if (goopUserFromBackend.isPresent()) {
                populateForm(goopUserFromBackend.get());
            } else {
                Notification.show(String.format("The requested goopUser was not found, ID = %d", goopUserId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AllUsersView.class);
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
        discordID = new TextField("Discord ID");
        artistName = new TextField("Artist Name");
        pronouns = new TextField("Pronouns");
        email = new TextField("Email");
        phone = new TextField("Phone");
        submissions = new TextField("Submissions");
        activeSubmission = new Checkbox("Active Submission");
        activeSubmission.getStyle().set("padding-top", "var(--lumo-space-m)");
        Component[] fields = new Component[]{discordID, artistName, pronouns, email, phone, submissions,
                activeSubmission};

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

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(GoopUser value) {
        this.goopUser = value;
        String topic = null;
        if (this.goopUser != null && this.goopUser.getId() != null) {
            topic = "goopUser/" + this.goopUser.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.goopUser);
        avatarGroup.setTopic(topic);

    }
}