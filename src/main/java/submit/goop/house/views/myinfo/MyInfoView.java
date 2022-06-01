package submit.goop.house.views.myinfo;

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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.security.RolesAllowed;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.SamplePerson;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.SamplePersonService;
import submit.goop.house.data.service.UserService;
import submit.goop.house.security.AuthenticatedUser;
import submit.goop.house.views.MainLayout;

import java.util.List;

@PageTitle("My Info")
@Route(value = "my-info", layout = MainLayout.class)
@RolesAllowed("user")
@Uses(Icon.class)
public class MyInfoView extends Div {

    private TextField discordID = new TextField("Discord ID");
    private TextField artistName = new TextField("Artist Name");
    private TextField pronouns = new TextField("Pronouns");
    private EmailField email = new EmailField("Email address");
    private PhoneNumberField phone = new PhoneNumberField("Phone number");
    private TextField submissions = new TextField("Submission IDs");

    //private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private Binder<GoopUser> binder = new Binder(GoopUser.class);

    private GoopUser goopUser;

    public MyInfoView(GoopUserService goopUserService) {
        addClassName("my-info-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        binder.bindInstanceFields(this);


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<GoopUser> possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
        if (possibleGoopUser.size() >= 1) {
            binder.setBean(possibleGoopUser.get(0));
        }
        else {
            clearForm();
            Notification.show("No user found, please contact a mod");
        }



        //cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> {
            goopUserService.update(binder.getBean());
            Notification.show("Artist details saved.");
            //clearForm();
        });
    }

    private void clearForm() {
        binder.setBean(new GoopUser());
    }

    private Component createTitle() {
        return new H3("Artist information");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        email.setErrorMessage("Please enter a valid email address");
        discordID.setReadOnly(true);
        submissions.setReadOnly(true);
        formLayout.add(discordID, submissions, artistName, pronouns, phone, email);
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

    private static class PhoneNumberField extends CustomField<String> {
        private ComboBox<String> countryCode = new ComboBox<>();
        private TextField number = new TextField();

        public PhoneNumberField(String label) {
            setLabel(label);
            countryCode.setWidth("120px");
            countryCode.setPlaceholder("Country");
            countryCode.setPattern("\\+\\d*");
            countryCode.setPreventInvalidInput(true);
            countryCode.setItems("+1","+44", "+52", "+91", "+86", "+964", "+353", "+44", "+972", "+39", "+225");
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
