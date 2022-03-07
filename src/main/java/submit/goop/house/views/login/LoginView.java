package submit.goop.house.views.login;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay {
    public LoginView() {
        setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Goop House Submissions");
        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);
        i18n.getForm().setForgotPassword("Login With Discord");
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

}

//@Route("login")
//@PageTitle("Login")
//public class LoginView extends VerticalLayout {
//
//    /**
//     * URL that Spring uses to connect to Google services
//     */
//    private static final String URL = "/oauth2/authorization/discord";
//
//    @Value("${spring.security.oauth2.client.registration.discord.client-id}")
//    private String clientkey;
//
//    public LoginView() {
//
//        setPadding(true);
//        setAlignItems(Alignment.CENTER);
//    }
//
//    @PostConstruct
//    public void initView() {
//
//        // Check that oauth keys are present
//        if (clientkey == null || clientkey.isEmpty() || clientkey.length() < 16) {
//            Paragraph text = new Paragraph("Could not find OAuth client key in application.properties. "
//                    + "Please double-check the key and refer to the README.md file for instructions.");
//            text.getStyle().set("padding-top", "100px");
//            add(text);
//
//        } else {
//
//            Anchor gplusLoginButton = new Anchor(URL, "Login with Discord");
//            gplusLoginButton.getStyle().set("margin-top", "100px");
//            gplusLoginButton.getElement().setAttribute("router-ignore", true);
//            add(gplusLoginButton);
//        }
//
//    }
//  }
