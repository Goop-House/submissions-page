package submit.goop.house.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import submit.goop.house.data.entity.User;
import submit.goop.house.data.service.UserService;
import submit.goop.house.security.AuthenticatedUser;
import submit.goop.house.views.about.AboutView;
import submit.goop.house.views.allsubmissions.AllSubmissionsView;
import submit.goop.house.views.allusers.AllUsersView;
import submit.goop.house.views.chat.ChatView;
import submit.goop.house.views.myinfo.MyInfoView;
import submit.goop.house.views.mysubmissions.MySubmissionsView;
import submit.goop.house.views.submit.SubmitView;
import submit.goop.house.views.users.UsersView;

/**
 * The main view is a top-level placeholder for other views.
 */
@PageTitle("Main")
public class MainLayout extends AppLayout {

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("menu-item-link");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("menu-item-text");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font size and margin
                addClassNames("me-s", "text-l");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    private UserService userService;
    private H1 viewTitle;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.userService = userService;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());


    }

    private Component createHeaderContent() {
//        Header header = new Header();
//        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "flex-col", "w-full");
//
//        Div layout = new Div();
//        layout.addClassNames("flex", "h-xl", "items-center", "px-l");
//
//        H1 appName = new H1("Goop House Submissions");
//        appName.addClassNames("my-0", "me-auto", "text-l");
//        layout.add(appName);
//
//        Optional<User> maybeUser = authenticatedUser.get();
//        if (maybeUser.isPresent()) {
//            User user = maybeUser.get();
//
//            Avatar avatar = new Avatar(user.getName(), user.getProfilePictureUrl());
//            avatar.addClassNames("me-xs");
//
//            ContextMenu userMenu = new ContextMenu(avatar);
//            userMenu.setOpenOnClick(true);
//            userMenu.addItem("Logout", e -> {
//                authenticatedUser.logout();
//            });
//
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            User pos = userService.findByUsername(auth.getName());
//            Span name = new Span(pos.getName());
//            name.addClassNames("font-medium", "text-s", "text-secondary");
//
//            Span role = new Span(pos.getRoles().toString());
//
//            layout.add(avatar, name);
//        } else {
//            Anchor loginLink = new Anchor("login", "Sign in");
//            layout.add(loginLink);
//        }
//
//        Nav nav = new Nav();
//        nav.addClassNames("flex", "gap-s", "overflow-auto", "px-m");
//
//        // Wrap the links in a list; improves accessibility
//        UnorderedList list = new UnorderedList();
//        list.addClassNames("flex", "list-none", "m-0", "p-0");
//        nav.add(list);
//
//        for (MenuItemInfo menuItem : createMenuItems()) {
//            if (accessChecker.hasAccess(menuItem.getView())) {
//                list.add(menuItem);
//            }
//
//        }
//
//        header.add(layout, nav);
//        return header;
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames("view-toggle");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames("view-title");

        Header header = new Header(toggle, viewTitle);
        header.addClassNames("view-header");
        return header;
    }

    private Component createDrawerContent() {
        H2 appName = new H2("Goop House Submissions");
        appName.addClassNames("app-name");

        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName,
                createNavigation(), createFooter());
        section.addClassNames("drawer-section");
        return section;
    }
    private Nav createNavigation() {
        Nav nav = new Nav();
        nav.addClassNames("menu-item-container");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("navigation-list");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }
        return nav;
    }
    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("About", "la la-broadcast-tower", AboutView.class), //

//                new MenuItemInfo("Chat", "la la-comments", ChatView.class), //

                new MenuItemInfo("All Submissions", "la la-mail-bulk", AllSubmissionsView.class), //

                new MenuItemInfo("All Users", "la la-user", AllUsersView.class), //

                //new MenuItemInfo("Users", "la la-user-astronaut", UsersView.class), //

                new MenuItemInfo("My Info", "la la-user", MyInfoView.class), //

                new MenuItemInfo("My Submissions", "la la-th-list", MySubmissionsView.class), //

                new MenuItemInfo("Active Submissions", "la la-upload", SubmitView.class), //

        };
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("footer");

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName(), user.getProfilePictureUrl());
            avatar.addClassNames("me-xs");

            ContextMenu userMenu = new ContextMenu(avatar);
            userMenu.setOpenOnClick(true);
            userMenu.addItem("Logout", e -> {
                authenticatedUser.logout();
            });

            Span name = new Span(user.getName());
            name.addClassNames("font-medium", "text-s", "text-secondary");

            layout.add(avatar, name);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

}
