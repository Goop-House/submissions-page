package submit.goop.house.views.about;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.UserService;
import submit.goop.house.views.MainLayout;

import java.util.List;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class AboutView extends VerticalLayout {

    public AboutView(GoopUserService goopUserService, UserService userService) {
        setSpacing(false);

        Image img = new Image("images/empty-plant.png", "placeholder plant");
        img.setWidth("200px");
        add(img);

        add(new H2("This place intentionally left empty"));
        add(new Paragraph("It’s a place where you can grow your own UI 🤗"));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");

        Dialog dialog = new Dialog();
        dialog.getElement().setAttribute("aria-label", "Create new employee");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(true);

        VerticalLayout dialogLayout = createDialogLayout(dialog);
        dialog.add(dialogLayout);

        Button button = new Button("Show dialog", e -> dialog.open());
        button.setVisible(false);
        add(dialog, button);

        PendingJavaScriptResult loadingFinished = UI.getCurrent().getPage().executeJs(""
                + "function delay() {\n"
                + "  return new Promise(function(resolve) { \n"
                + "    setTimeout(resolve, 50)\n"
                + "  });\n"
                + "}\n"
                + "async function waitForLoadingToFinish() {\n"
                + "  while(true) {\n"
                + "    let progressElement = document.getElementsByClassName('v-loading-indicator');\n"
                + "    if (progressElement[0].style.display == 'none') {\n"
                + "      return true;\n"
                + "    } else {\n"
                + "      await delay();\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "return waitForLoadingToFinish();");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        loadingFinished.then(Boolean.class, (res) -> {
            if(!auth.getName().equals("anonymousUser")) {
                List<GoopUser> possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
                int count = 0;
                while(possibleGoopUser.size() == 0 && count < 10) {
                        button.click();
                        possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
                        count++;
                }
            }
        });
    }

    private static VerticalLayout createDialogLayout(Dialog dialog) {
        H2 headline = new H2("Create new employee");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        TextField firstNameField = new TextField("First name");
        TextField lastNameField = new TextField("Last name");
        VerticalLayout fieldLayout = new VerticalLayout(firstNameField,
                lastNameField);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button saveButton = new Button("Save", e -> dialog.close());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonLayout
                .setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(headline, fieldLayout,
                buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return dialogLayout;
    }

}
