package com.quantraksha.kite.api;

import com.quantraksha.kite.config.KiteProperties;
import com.quantraksha.kite.core.session.KiteSessionService;
import com.quantraksha.kite.core.session.KiteSessionService.Session;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class SessionController {

    public record Status(boolean loggedIn, String userId, String userName, String loginUrl) {
    }

    /** Either the bare request_token or the full redirect URL Kite sent the browser to. */
    public record LoginRequest(String requestToken) {
    }

    private final KiteSessionService sessions;
    private final KiteProperties props;

    public SessionController(KiteSessionService sessions, KiteProperties props) {
        this.sessions = sessions;
        this.props = props;
    }

    @GetMapping("/status")
    public Status status() {
        Session s = sessions.session();
        return s == null
                ? new Status(false, null, null, sessions.loginUrl())
                : new Status(true, s.userId(), s.userName(), sessions.loginUrl());
    }

    @PostMapping("/session")
    public Status login(@RequestBody LoginRequest body) {
        sessions.create(extractRequestToken(body.requestToken()));
        return status();
    }

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        sessions.logout();
    }

    /** Usable as the Kite app's Redirect URL: http://127.0.0.1:4100/api/callback */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("request_token") String requestToken) {
        sessions.create(requestToken);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(props.uiUrl())).build();
    }

    static String extractRequestToken(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("requestToken is required");
        }
        String value = input.trim();
        if (!value.contains("request_token=")) return value;
        String token = UriComponentsBuilder.fromUriString(value).build().getQueryParams().getFirst("request_token");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("No request_token in the pasted URL");
        }
        return token;
    }
}
