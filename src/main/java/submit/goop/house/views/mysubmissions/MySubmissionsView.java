package submit.goop.house.views.mysubmissions;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.security.RolesAllowed;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.Submission;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.SubmissionService;
import submit.goop.house.endpoint.SubmissionsEndpoint;
import submit.goop.house.security.AuthenticatedUser;
import submit.goop.house.views.MainLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@PageTitle("My Submissions")
@Route(value = "my-submissions", layout = MainLayout.class)
@RolesAllowed("user")
@Tag("my-submissions-view")
@JsModule("./views/mysubmissions/my-submissions-view.ts")
public class MySubmissionsView extends LitTemplate implements HasComponents, HasStyle {

    @Id
    private Select<String> sortBy;

    public MySubmissionsView(GoopUserService goopUserService, SubmissionService submissionService) {
        addClassNames("my-submissions-view", "flex", "flex-col", "h-full");
        sortBy.setItems("Popularity", "Newest first", "Oldest first");
        sortBy.setValue("Popularity");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GoopUser> possibleGoopUser = goopUserService.findByDiscordID(auth.getName());
        GoopUser goopUser = possibleGoopUser.get(0);
        ArrayList<String> submissionIDs = new ArrayList<String>(Arrays.asList(goopUser.getSubmissions().split(",")));
        ArrayList<Submission> submissions = new ArrayList<Submission>();
        SubmissionsEndpoint submissionsEndpoint = new SubmissionsEndpoint();
        for (String submissionID : submissionIDs){
            try {
                UUID subID = UUID.fromString(submissionID);
                submissions.add(submissionService.findBySubmissionID(subID).get(0));
            } catch (Exception e){
                e.printStackTrace();
                Notification.show("An error occurred while loading som of your submissions.", 5, Notification.Position.TOP_CENTER);
            }

        }
        for(Submission submission : submissions) {
            try {
                String songName = submission.getTitle() == null ? "Unknown" : submission.getTitle();
                String event = submission.getEvent() == null ? "Unknown" : submission.getEvent();
                //String imageURL = submission.getCoverArt() ==  null ? submissionsEndpoint.getEventArt(event) : submission.getCoverArt();
                String imageURL = submissionsEndpoint.getEventArt(event);
                String artistName = submission.getMainArtist() == null ? "Unknown" : submission.getMainArtist();

                add(new MySubmissionsViewCard(songName, event, imageURL, artistName));
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("An error occurred while loading som of your submissions.", 5, Notification.Position.TOP_CENTER);
            }
        }
    }
}