package com.quantraksha.kite.core.service;

import com.quantraksha.kite.core.*;
import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TradeCaptureService {

    private static final Logger log = LoggerFactory.getLogger(TradeCaptureService.class);

    public TradeCapture getTradeCaptureForCrudeOilMFuture(Levels levels) {

        ITradeCalculator tradeCalculator = new CrudeOilMTradeCalculator();
        return  tradeCalculator.calculateTrade(levels);
    }


    public TradeCapture getTradeCaptureForNiftyFuture(Levels levels) {

        ITradeCalculator tradeCalculator = new NiftyTradeCalculator();
        return  tradeCalculator.calculateTrade(levels);
    }

    public TradeCapture getTradeCaptureForBankNiftyFuture(Levels levels) {

        ITradeCalculator tradeCalculator = new BankNiftyTradeCalculator();
        return  tradeCalculator.calculateTrade(levels);
    }

    public TradeCapture getTradeCaptureForGOLDMFuture(Levels levels) {

        ITradeCalculator tradeCalculator = new GOLDMTradeCalculator();
        return  tradeCalculator.calculateTrade(levels);
    }

}
