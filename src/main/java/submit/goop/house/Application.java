package submit.goop.house;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import com.vaadin.flow.server.AppShellSettings;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "goophousesubmissions", variant = Lumo.DARK)
@PWA(name = "Goop House Submissions", shortName = "Goop House Submissions", offlineResources = {"images/logo.png"}, iconPath = "images/icons/logo.png")
@Push
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addMetaTag("og:title", "Goop House Submissions");
        settings.addMetaTag("og:description", "The official submissions site for Goop House");
        settings.addMetaTag("og:image", "https://cdn.discordapp.com/attachments/834541919568527361/874767069772660736/goop_house_logo_for_disc.png");
        settings.addMetaTag("og:url", "https://submit.goop.house");
        settings.addMetaTag("og:type", "website");
        //settings.addLink("shortcut icon", "images/icons/logo.png");
        settings.addFavIcon("icon","images/icons/logo.png", "48x48");
    }

}
