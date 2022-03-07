//package submit.goop.house.security;
//
//import com.vaadin.flow.server.HandlerHelper;
//import com.vaadin.flow.shared.ApplicationConstants;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.RequestEntity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
//import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
//import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
//import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.stream.Stream;
//
//import static submit.goop.house.security.OAuth2UserAgentUtils.withUserAgent;
//
///**
// * Configures Spring Security, doing the following:
// * <li>Bypass security checks for static resources,</li>
// * <li>Restrict access to the application, allowing only logged in users,</li>
// * <li>Set up the login form,</li>
// */
//@EnableWebSecurity
//@Configuration
//public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//
//    private static final String LOGIN_URL = "/login";
//    public static final String LOGOUT_URL = "/logout";
//    private static final String LOGOUT_SUCCESS_URL = "/";
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * Registers our UserDetailsService and the password encoder to be used on
//     * login attempts.
//     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // @formatter:off
//        http
//
//                // Allow all flow internal requests.
//                .authorizeRequests().requestMatchers(SecurityConfiguration::isFrameworkInternalRequest).permitAll()
//
//                // Restrict access to our application.
//                .and().authorizeRequests().anyRequest().authenticated()
//
//                // Not using Spring CSRF here to be able to use plain HTML for the login page
//                .and().csrf().disable()
//
//                // Configure logout
//                .logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)
//
//                // Configure the login page.
//                .and().oauth2Login().loginPage(LOGIN_URL).permitAll()
//
//                .and().oauth2Login().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient()).and().userInfoEndpoint().userService(userService1());
//
//        // @formatter:on
//    }
//
//    /**
//     * Allows access to static resources, bypassing Spring Security.
//     */
//    @Override
//    public void configure(WebSecurity web) {
//        web.ignoring().antMatchers(
//                // client-side JS code
//                "/VAADIN/**",
//
//                // the standard favicon URI
//                "/favicon.ico",
//
//                // web application manifest
//                "/manifest.webmanifest", "/sw.js", "/offline-page.html",
//
//                // icons and images
//                "/icons/**", "/images/**");
//    }
//
//    /**
//     * Tests if the request is an internal framework request. The test consists
//     * of checking if the request parameter is present and if its value is
//     * consistent with any of the request types know.
//     *
//     * @param request
//     *            {@link HttpServletRequest}
//     * @return true if is an internal framework request. False otherwise.
//     */
//    static boolean isFrameworkInternalRequest(HttpServletRequest request) {
//        final String parameterValue = request
//                .getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
//        return parameterValue != null
//                && Stream.of(HandlerHelper.RequestType.values()).anyMatch(
//                r -> r.getIdentifier().equals(parameterValue));
//    }
//
//
//    @Bean
//    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
//        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
//
//        client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter() {
//            @Override
//            public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest oauth2Request) {
//                return withUserAgent(super.convert(oauth2Request));
//            }
//        });
//
//        return client;
//    }
//
//    @Bean
//    public OAuth2UserService<OAuth2UserRequest, OAuth2User> userService1() {
//        DefaultOAuth2UserService service = new DefaultOAuth2UserService();
//
//        service.setRequestEntityConverter(new OAuth2UserRequestEntityConverter() {
//            @Override
//            public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
//                return withUserAgent(super.convert(userRequest));
//            }
//        });
//
//        return service;
//    }
//}