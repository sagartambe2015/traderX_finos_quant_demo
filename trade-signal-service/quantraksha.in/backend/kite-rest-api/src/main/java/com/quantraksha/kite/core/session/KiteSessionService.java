package com.quantraksha.kite.core.session;

import com.quantraksha.kite.api.KiteApiException;
import com.quantraksha.kite.config.KiteProperties;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.InputException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.PermissionException;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.TokenException;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Owns the single KiteConnect client and the daily access token. The token is a plain string in
 * {@code kite.token-file}, the same format the Node relay and test.ts use, so they can share one login.
 */
@Service
public class KiteSessionService {

    private static final Logger log = LoggerFactory.getLogger(KiteSessionService.class);

    @FunctionalInterface
    public interface KiteCall<T> {
        T call(KiteConnect kite) throws KiteException, IOException, JSONException;
    }

    public record Session(String userId, String userName) {
    }

    private final KiteProperties props;
    private final KiteConnect kite;
    private final Path tokenFile;
    private volatile Session session;

    public KiteSessionService(KiteProperties props) {
        this.props = props;
        this.kite = new KiteConnect(props.apiKey());
        this.tokenFile = Path.of(props.tokenFile()).toAbsolutePath();
    }

    /** Reuse a saved token if Kite still accepts it; a rejected token is deleted. */
    @PostConstruct
    void restore() {
        if (!Files.exists(tokenFile)) return;
        try {
            String saved = Files.readString(tokenFile, StandardCharsets.UTF_8).trim();
            if (saved.isEmpty()) return;
            kite.setAccessToken(saved);
            Profile profile = kite.getProfile();
            // The Java Profile model drops user_id, so it stays null until the next login
            session = new Session(null, profile.userName);
            log.info("Restored Kite session for {} from {}", profile.userName, tokenFile);
        } catch (TokenException e) {
            log.info("Saved access token in {} is expired or invalid", tokenFile);
            clear();
        } catch (KiteException e) {
            log.warn("Could not validate saved token: {}", e.message);
        } catch (IOException | JSONException e) {
            log.warn("Could not validate saved token: {}", e.toString());
        }
    }

    public Session session() {
        return session;
    }

    public String loginUrl() {
        return kite.getLoginURL();
    }

    /** Exchange a single-use request_token for the day's access token and persist it. */
    public synchronized Session create(String requestToken) {
        User user = invoke(k -> k.generateSession(requestToken, props.apiSecret()));
        kite.setAccessToken(user.accessToken);
        kite.setPublicToken(user.publicToken);
        try {
            Files.writeString(tokenFile, user.accessToken, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Logged in, but could not save the token to {}: {}", tokenFile, e.toString());
        }
        session = new Session(user.userId, user.userName);
        return session;
    }

    /** Invalidates the token at Kite too, which also logs out anything sharing the token file. */
    public synchronized void logout() {
        if (session != null) {
            try {
                invoke(KiteConnect::invalidateAccessToken);
            } catch (KiteApiException e) {
                log.warn("Kite logout failed, clearing locally anyway: {}", e.getMessage());
            }
        }
        clear();
    }

    /** Run a Kite call that needs a logged-in session. */
    public <T> T call(KiteCall<T> call) {
        if (session == null) {
            throw new KiteApiException(HttpStatus.UNAUTHORIZED, "NotLoggedIn", "No Kite session. Log in first.");
        }
        return invoke(call);
    }

    private <T> T invoke(KiteCall<T> call) {
        try {
            return call.call(kite);
        } catch (TokenException e) {
            // Kite access tokens die at the end of the trading day
            clear();
            throw new KiteApiException(HttpStatus.UNAUTHORIZED, "TokenException", e.message);
        } catch (InputException e) {
            throw new KiteApiException(HttpStatus.BAD_REQUEST, "InputException", e.message);
        } catch (PermissionException e) {
            throw new KiteApiException(HttpStatus.FORBIDDEN, "PermissionException", e.message);
        } catch (KiteException e) {
            throw new KiteApiException(HttpStatus.BAD_GATEWAY, e.getClass().getSimpleName(), e.message);
        } catch (IOException | JSONException e) {
            throw new KiteApiException(HttpStatus.BAD_GATEWAY, "NetworkException", e.toString());
        }
    }

    private void clear() {
        String rejected = kite.getAccessToken();
        session = null;
        kite.setAccessToken(null);
        deleteTokenFile(rejected);
    }

    /** The Node relay writes this file too, so keep it if it now holds a newer token than the one rejected. */
    private void deleteTokenFile(String rejected) {
        try {
            if (Files.exists(tokenFile) && Files.readString(tokenFile, StandardCharsets.UTF_8).trim().equals(rejected)) {
                Files.delete(tokenFile);
            }
        } catch (IOException e) {
            log.warn("Could not delete {}: {}", tokenFile, e.toString());
        }
    }
}
