package submit.goop.house.security;


import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;

class OAuth2UserAgentUtils {
    static RequestEntity<?> withUserAgent(RequestEntity<?> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        headers.add(HttpHeaders.USER_AGENT, DISCORD_BOT_USER_AGENT);

        return new RequestEntity<>(request.getBody(), headers, request.getMethod(), request.getUrl());
    }

    private static final String DISCORD_BOT_USER_AGENT = "DiscordBot (https://github.com/Goop-House/submissions-page/)";
}