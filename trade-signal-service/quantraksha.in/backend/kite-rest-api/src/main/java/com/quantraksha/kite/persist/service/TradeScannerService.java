package com.quantraksha.kite.persist.service;

import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.core.service.TradeCalculatorService;
import com.quantraksha.kite.core.session.KiteSessionService;
import com.quantraksha.kite.persist.entity.StrategyEntity;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.mapper.TradeCaptureToTradingMapper;
import com.quantraksha.kite.persist.repository.StrategyRepository;
import com.quantraksha.kite.persist.repository.TradeCaptureRepository;
import com.zerodhatech.models.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TradeScannerService {
    private static final Logger log = LoggerFactory.getLogger(TradeScannerService.class);
    public TradeCalculatorService tradeCalculatorService;
    public TradeCaptureToTradingMapper tradeCaptureToTradingMapper;
    public TradeCaptureRepository tradeCaptureRepository;
    public StrategyRepository strategyRepository;
    public KiteSessionService kite;

    public TradeScannerService(TradeCalculatorService tradeCalculatorService,
                               TradeCaptureToTradingMapper tradeCaptureToTradingMapper,
                               TradeCaptureRepository tradeCaptureRepository,
                               StrategyRepository strategyRepository,
                               KiteSessionService kite) {
      this.tradeCalculatorService = tradeCalculatorService;
      this.tradeCaptureToTradingMapper = tradeCaptureToTradingMapper;
      this.tradeCaptureRepository = tradeCaptureRepository;
      this.strategyRepository = strategyRepository;
      this.kite = kite;
    }

    public String scanAndSaveTradeSignal() {
        try {
            String instrumentToken = "146796039"; // CrudeOILM
            int days = 2; // Default value for days


            String instrumentTokenNifty = "15736578"; // NIFTY NOV FUT
            int daysNifty = 2; // Default value for days

            // For demonstration, we return a TradeEntryExit object with dummy values.
            TradeCapture tradeCaptureCrudeMFuture = tradeCalculatorService.getTradeCaptureForCrudeOilM(instrumentToken, days);

            // For demonstration, we return a TradeEntryExit object with dummy values.
            TradeCapture tradeCaptureNiftyFuture = tradeCalculatorService.getTradeCaptureForNiftyFutureM(instrumentTokenNifty, daysNifty);

            TradeCaptureEntity tradeCaptureEntityCrudeOilM = toEntity(tradeCaptureCrudeMFuture, "MCX");
            TradeCaptureEntity tradeCaptureEntityNiftyFutures = toEntity(tradeCaptureNiftyFuture, "NFO");

            if(tradeCaptureRepository.existsByEntryDateAndSecurityAndInstrumentId(
                            tradeCaptureEntityCrudeOilM.getEntryDate(),
                            tradeCaptureEntityCrudeOilM.getSecurity(),
                            tradeCaptureEntityCrudeOilM.getInstrumentId())) {
                log.info("Trade signal for CrudeOILM already exists. Skipping save.");
            } else {
                log.info("Trade signal not found for CrudeOILM. Saving new trade signal.");
                tradeCaptureRepository.save(tradeCaptureEntityCrudeOilM);
                log.info("Trade signal for CrudeOILM saved successfully.");
            }

            if(tradeCaptureRepository.existsByEntryDateAndSecurityAndInstrumentId(
                    tradeCaptureEntityNiftyFutures.getEntryDate(),
                    tradeCaptureEntityNiftyFutures.getSecurity(),
                    tradeCaptureEntityNiftyFutures.getInstrumentId())) {
                log.info("Trade signal for NiftyFut already exists. Skipping save.");
            } else {
                log.info("Trade signal not found for NiftyFut. Saving new trade signal.");
                tradeCaptureRepository.save(tradeCaptureEntityNiftyFutures);
                log.info("Trade signal for NiftyFut saved successfully.");
            }

        } catch (Exception e) {
            log.error("Error occurred while scanning and saving trade signals: {}", e.getMessage(), e);
            return "Error occurred while scanning and saving trade signals: " + e.getMessage();
        }

        // Here you can save the tradeCaptureEntity to your database using a repository or service
        return "Trade scan completed and signals saved for Crude Oil M and Nifty Futures.";

    }

    private TradeCaptureEntity toEntity(TradeCapture tradeCapture, String exchange) {
        return tradeCaptureToTradingMapper.mapToTradeCaptureEntity(tradeCapture,
                findOrCreateStrategy(tradeCapture.getStrategyName()),
                findInstrument(exchange, tradeCapture.getInstrumentToken()));
    }

    private StrategyEntity findOrCreateStrategy(String strategyName) {
        return strategyRepository.findByStrategyName(strategyName)
                .orElseGet(() -> strategyRepository.save(new StrategyEntity(strategyName, strategyName)));
    }

    /** Kite's instrument master is the source of the trading symbol and the current lot size. */
    private Instrument findInstrument(String exchange, String instrumentToken) {
        long token = Long.parseLong(instrumentToken);
        return kite.call(k -> k.getInstruments(exchange)).stream()
                .filter(i -> i.instrument_token == token)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Instrument " + instrumentToken + " not found on " + exchange + "; the contract may have expired"));
    }

}
