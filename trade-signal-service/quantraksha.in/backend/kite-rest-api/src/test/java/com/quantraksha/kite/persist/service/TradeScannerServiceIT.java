package com.quantraksha.kite.persist.service;

import static com.quantraksha.kite.support.KiteFixtures.candle;
import static com.quantraksha.kite.support.KiteFixtures.history;
import static com.quantraksha.kite.support.KiteFixtures.todayIst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.quantraksha.kite.core.session.KiteSessionService;
import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import com.quantraksha.kite.persist.repository.StrategyRepository;
import com.quantraksha.kite.persist.repository.TradeCaptureRepository;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** A scan saved through the real mapper and repositories to H2, with only the Kite session mocked. */
@SpringBootTest
@ActiveProfiles("it")
class TradeScannerServiceIT {

    private static final String SUCCESS = "Trade scan completed and signals saved for Crude Oil M and Nifty Futures.";

    @Autowired
    private TradeScannerService scanner;

    @Autowired
    private TradeCaptureRepository captures;

    @Autowired
    private StrategyRepository strategies;

    @MockitoBean
    private KiteSessionService kite;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = todayIst();
    }

    // Scans commit, and H2 is shared with the other ITs, which expect empty tables
    @AfterEach
    void tearDown() {
        captures.deleteAll();
        strategies.deleteAll();
    }

    private static Instrument instrument(long token, String symbol, int lotSize) {
        Instrument i = new Instrument();
        i.instrument_token = token;
        i.tradingsymbol = symbol;
        i.lot_size = lotSize;
        return i;
    }

    private HistoricalData twoDays(double hh, double ll) {
        return history(
                candle(today.minusDays(2), ll + 10, hh - 5, ll, hh - 10),
                candle(today.minusDays(1), hh - 10, hh, ll + 5, hh - 5));
    }

    // Order of Kite calls in a scan: crude levels, nifty levels, MCX instruments, NFO instruments
    private void kiteReturnsLevelsAndInstruments(List<Instrument> nfo) {
        when(kite.<Object>call(any())).thenReturn(
                twoDays(6010, 5905),
                twoDays(24010, 23805),
                List.of(instrument(1, "OTHER", 1), instrument(146796039, "CRUDEOILM25NOVFUT", 10)),
                nfo);
    }

    @Test
    void savesACompleteCaptureForEachInstrument() {
        kiteReturnsLevelsAndInstruments(List.of(instrument(15736578, "NIFTY25NOVFUT", 75)));

        assertThat(scanner.scanAndSaveTradeSignal()).isEqualTo(SUCCESS);

        assertThat(captures.findAll()).hasSize(2);
        TradeCaptureEntity nifty = captures.findBySecurityAndEntryDate("NIFTY25NOVFUT", today.minusDays(1)).get(0);
        assertThat(nifty.getCaptureId()).isNotBlank();
        assertThat(nifty.getInstrumentId()).isEqualTo("15736578");
        assertThat(nifty.getSide()).isEqualTo(TradeSide.LONG);
        assertThat(nifty.getTargetQuantity()).isEqualByComparingTo("75");
        assertThat(nifty.getCalcEntry()).isEqualByComparingTo("24041");
        assertThat(nifty.getEntryTime()).isNotNull();
        assertThat(nifty.getGoNoDecision()).isEqualTo(GoNoDecision.PENDING);
        assertThat(captures.findByStrategyEntity_StrategyId("Nifty-FUTURE-HH-LL")).hasSize(1);
        assertThat(captures.findByStrategyEntity_StrategyId("CRUDEOIL-M-FUTURE-HH-LL")).hasSize(1);
    }

    @Test
    void savesTheLotSizeFromTheInstrumentMaster() {
        kiteReturnsLevelsAndInstruments(List.of(instrument(15736578, "NIFTY25NOVFUT", 75)));

        scanner.scanAndSaveTradeSignal();

        LocalDate entryDate = today.minusDays(1);
        assertThat(captures.findBySecurityAndEntryDate("NIFTY25NOVFUT", entryDate).get(0).getLotSize()).isEqualTo(75);
        assertThat(captures.findBySecurityAndEntryDate("CRUDEOILM25NOVFUT", entryDate).get(0).getLotSize()).isEqualTo(10);
    }

    @Test
    void secondScanSkipsExistingCapturesAndReusesTheStrategies() {
        List<Instrument> nfo = List.of(instrument(15736578, "NIFTY25NOVFUT", 75));
        kiteReturnsLevelsAndInstruments(nfo);
        scanner.scanAndSaveTradeSignal();
        List<String> firstScanIds = captures.findAll().stream().map(TradeCaptureEntity::getCaptureId).toList();
        kiteReturnsLevelsAndInstruments(nfo);

        assertThat(scanner.scanAndSaveTradeSignal()).isEqualTo(SUCCESS);

        assertThat(strategies.findAll()).hasSize(2);
        assertThat(captures.findAll())
                .extracting(TradeCaptureEntity::getCaptureId)
                .containsExactlyInAnyOrderElementsOf(firstScanIds);
    }

    @Test
    void expiredContractIsReportedAndNothingIsSaved() {
        kiteReturnsLevelsAndInstruments(List.of());

        assertThat(scanner.scanAndSaveTradeSignal()).contains("Instrument 15736578 not found on NFO");

        assertThat(captures.findAll()).isEmpty();
    }
}
