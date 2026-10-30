package com.quantraksha.kite.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quantraksha.kite.core.session.KiteSessionService;
import com.quantraksha.kite.persist.service.TradeScannerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The HTTP route only; TradeScannerServiceIT covers the scan itself. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class TradeScannerControllerIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private KiteSessionService kite;

    @MockitoBean
    private TradeScannerService scanner;

    @Test
    void tradeScanReturnsTheScanResult() throws Exception {
        when(scanner.scanAndSaveTradeSignal()).thenReturn("scan result");

        mvc.perform(get("/api/trade-scan"))
                .andExpect(status().isOk())
                .andExpect(content().string("scan result"));
    }
}
