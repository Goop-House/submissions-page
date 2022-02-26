package submit.goop.house.views.chat;

import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.security.AuthenticatedUser;
import submit.goop.house.views.MainLayout;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@AnonymousAllowed
public class ChatView extends VerticalLayout {

    public ChatView(GoopUserService goopUserService) {
        addClassName("chat-view");
        setSpacing(false);
        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        goopUserService.get(auth.getName());
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), auth.getName(), null);

        // Tabs allow us to change chat rooms.
        Tabs tabs = new Tabs(new Tab("#general"), new Tab("#support"), new Tab("#casual"));
        tabs.setWidthFull();

        // `CollaborationMessageList` displays messages that are in a
        // Collaboration Engine topic. You should give in the user details of
        // the current user using the component, and a topic Id. Topic id can be
        // any freeform string. In this template, we have used the format
        // "chat/#general". Check
        // https://vaadin.com/docs/latest/ce/collaboration-message-list/#persisting-messages
        // for information on how to persisting are retrieving messages over
        // server restarts.
        CollaborationMessageList list = new CollaborationMessageList(userInfo, "chat/#general");
        list.setWidthFull();
        list.addClassNames("chat-view-message-list");

        // `CollaborationMessageInput is a textfield and button, to be able to
        // submit new messages. To avoid having to set the same info into both
        // the message list and message input, the input takes in the list as an
        // constructor argument to get the information from there.
        CollaborationMessageInput input = new CollaborationMessageInput(list);
        input.addClassNames("chat-view-message-input");
        input.setWidthFull();

        // Layouting
        add(tabs, list, input);
        setSizeFull();
        expand(list);

        // Change the topic id of the chat when a new tab is selected
        tabs.addSelectedChangeListener(event -> {
            String channelName = event.getSelectedTab().getLabel();
            list.setTopic("chat/" + channelName);
        });
    }

}