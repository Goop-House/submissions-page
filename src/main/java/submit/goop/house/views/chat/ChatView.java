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

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.GoopUserService;
import submit.goop.house.data.service.UserService;
import submit.goop.house.security.AuthenticatedUser;
import submit.goop.house.views.MainLayout;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@AnonymousAllowed
public class ChatView extends VerticalLayout {

    public ChatView(GoopUserService goopUserService, UserService userService) {
        addClassName("chat-view");
        setSpacing(false);
        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        Logger logger = LoggerFactory.getLogger(getClass());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GoopUser> possibleChatter = goopUserService.findByDiscordID(auth.getName());
        String chatterName = "";
        GoopUser chatter = null;
        User authUser = userService.findByUsername(auth.getName());
        if (possibleChatter.size() == 0) {
            if(auth.getName().equals("anonymousUser")) {
                chatterName = "Anonymous";
            }
            else {
                chatterName = authUser.getName();
            }
        }
        else {
            chatterName = possibleChatter.get(0).getArtistName();
            chatter = possibleChatter.get(0);
        }
        logger.info("Chatter: " + chatterName);
        UserInfo userInfo = new UserInfo(chatter != null ? chatter.getId().toString() : UUID.randomUUID().toString(), chatterName, auth.getName().equals("anonymousUser") ? "https://www.amongusavatarcreator.com/assets/img/characters/600/green.png" : authUser.getProfilePictureUrl());

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