package com.quantraksha.kite.core.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quantraksha.kite.api.KiteApiException;
import com.quantraksha.kite.config.KiteProperties;
import com.quantraksha.kite.core.session.KiteSessionService.Session;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.TokenException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class KiteSessionServiceTest {

    @TempDir
    Path dir;

    private Path tokenFile;
    private KiteSessionService service;

    @BeforeEach
    void setUp() {
        tokenFile = dir.resolve("access_token.txt");
        service = new KiteSessionService(new KiteProperties("key", "secret", tokenFile.toString(), "", List.of()));
        ReflectionTestUtils.setField(service, "session", new Session("id", "name"));
    }

    private void rejectToken(String token) {
        assertThatThrownBy(() -> service.call(kite -> {
            kite.setAccessToken(token);
            throw new TokenException("expired", 403);
        })).isInstanceOf(KiteApiException.class);
    }

    @Test
    void rejectedTokenIsDeletedFromTheFile() throws IOException {
        Files.writeString(tokenFile, "old\n");

        rejectToken("old");

        assertThat(tokenFile).doesNotExist();
        assertThat(service.session()).isNull();
    }

    @Test
    void newerTokenSavedByTheRelayIsKept() throws IOException {
        Files.writeString(tokenFile, "new");

        rejectToken("old");

        assertThat(tokenFile).hasContent("new");
        assertThat(service.session()).isNull();
    }
}
