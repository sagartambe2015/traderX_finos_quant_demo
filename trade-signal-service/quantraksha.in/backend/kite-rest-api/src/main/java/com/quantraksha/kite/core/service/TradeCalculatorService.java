package com.quantraksha.kite.core.service;

import com.quantraksha.kite.core.*;
import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TradeCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(TradeCalculatorService.class);
    LevelCalculatorService levelCalculatorService;

    public TradeCalculatorService(LevelCalculatorService levelCalculatorService) {
        this.levelCalculatorService = levelCalculatorService;
    }

    public static ITradeCalculator getTradeCalculator(String strategyName) {
        ITradeCalculator iTradeCalculator = null;

        if(strategyName == null || strategyName.isEmpty()) {
            throw new IllegalArgumentException("Instrument cannot be null or empty");
        } else if (!strategyName.equals(StrategyName.CRUDEOILM_FUT)
                && !strategyName.equals(StrategyName.NIFTY_FUT)
                && !strategyName.equals(StrategyName.BANKNIFTY_FUT)
                && !strategyName.equals(StrategyName.GOLDM_FUT)
                && !strategyName.equals(StrategyName.SILVERM_FUT)) {
            log.error("Invalid strategy name: {}", strategyName);
            throw new IllegalArgumentException("Invalid instrument: " + strategyName);
        } else if (StrategyName.CRUDEOILM_FUT.equals(strategyName)) {
            iTradeCalculator = new CrudeOilMTradeCalculator();
        } else if (StrategyName.NIFTY_FUT.equals(strategyName)) {
            iTradeCalculator = new NiftyTradeCalculator();
        } else if (StrategyName.BANKNIFTY_FUT.equals(strategyName)) {
            iTradeCalculator = new BankNiftyTradeCalculator();
        } else if (StrategyName.GOLDM_FUT.equals(strategyName)) {
            iTradeCalculator = new GOLDMTradeCalculator();
        } else if (StrategyName.SILVERM_FUT.equals(strategyName)) {
            iTradeCalculator = new SILVERMTradeCalculator();
        }

        return iTradeCalculator;
    }

    public TradeCapture getTradeCaptureForCrudeOilM(String instrumentToken, int days) {
        Levels levels = levelCalculatorService.getLevels(instrumentToken, days);
        log.info("Levels for Crude Oil M: {}", levels);
        ITradeCalculator tradeCalculator = getTradeCalculator(StrategyName.CRUDEOILM_FUT);
        TradeCapture tradeCapture = tradeCalculator.calculateTrade(levels);
        log.info("Trade Capture for Crude Oil M: {}", tradeCapture);
        return tradeCapture;
    }

    public TradeCapture getTradeCaptureForNiftyFutureM(String instrumentToken, int days) {
        Levels levels = levelCalculatorService.getLevels(instrumentToken, days);
        log.info("Levels for Nifty Future: {}", levels);
        ITradeCalculator tradeCalculator = getTradeCalculator(StrategyName.NIFTY_FUT);
        TradeCapture tradeCapture = tradeCalculator.calculateTrade(levels);
        log.info("Trade Capture for Nifty Future: {}", tradeCapture);
        return tradeCapture;

    }

    public TradeCapture getTradeCaptureForBankNiftyFuture(String instrumentToken, int days) {
        Levels levels = levelCalculatorService.getLevels(instrumentToken, days);
        ITradeCalculator tradeCalculator = getTradeCalculator(StrategyName.BANKNIFTY_FUT);
        log.info("Levels for BankNifty Future: {}", levels);
        TradeCapture tradeCapture = tradeCalculator.calculateTrade(levels);
        log.info("Trade Capture for BankNifty Future: {}", tradeCapture);
        return tradeCapture;
    }

    public TradeCapture getTradeCaptureForGOLDMFuture(String instrumentToken, int days) {
        Levels levels = levelCalculatorService.getLevels(instrumentToken, days);
        ITradeCalculator tradeCalculator = getTradeCalculator(StrategyName.GOLDM_FUT);
        log.info("Levels for GOLDM Future: {}", levels);
        TradeCapture tradeCapture = tradeCalculator.calculateTrade(levels);
        log.info("Trade Capture for GOLDM Future: {}", tradeCapture);
        return tradeCapture;
    }


    public TradeCapture getTradeCaptureForSILVERMMFuture(String instrumentToken, int days) {
        Levels levels = levelCalculatorService.getLevels(instrumentToken, days);
        ITradeCalculator tradeCalculator = getTradeCalculator(StrategyName.SILVERM_FUT);
        log.info("Levels for SILVERM Future: {}", levels);
        TradeCapture tradeCapture = tradeCalculator.calculateTrade(levels);
        log.info("Trade Capture for SILVERM Future: {}", tradeCapture);
        return tradeCalculator.calculateTrade(levels);
    }


}
