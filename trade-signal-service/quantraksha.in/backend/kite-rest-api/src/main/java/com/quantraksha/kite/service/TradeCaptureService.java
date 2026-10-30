package com.quantraksha.kite.service;

import com.quantraksha.kite.core.CrudeOilMTradeCalculator;
import com.quantraksha.kite.core.ITradeCalculator;
import com.quantraksha.kite.core.NiftyTradeCalculator;
import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;
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

}
