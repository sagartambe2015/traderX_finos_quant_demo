package com.quantraksha.kite.api;


import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.core.service.LevelCalculatorService;
import com.quantraksha.kite.core.service.TradeCalculatorService;
import com.quantraksha.kite.core.service.TradeCaptureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TradeCaptureController {
    private static final Logger log = LoggerFactory.getLogger(TradeCaptureController.class);

    public TradeCalculatorService tradeCalculatorService;

    public TradeCaptureController(TradeCalculatorService tradeCaptureService) {

        this.tradeCalculatorService = tradeCaptureService;

    }


    @GetMapping("/calculateTrade/mcx/{instrumentToken}")
    public TradeCapture calculateEntryForCrudeOilM (@PathVariable String instrumentToken, @RequestParam(defaultValue = "2") int days) {



        //InstrumentID: 146796039 : CrudeOILM
        //InstrumentID: 15736578   : NIFTY NOV FUT
        // For demonstration, we return a TradeEntryExit object with dummy values.
        return tradeCalculatorService.getTradeCaptureForCrudeOilM(instrumentToken, days);
    }

    @GetMapping("/calculateTrade/nse/{instrumentToken}")
    public TradeCapture calculateEntryForNiftyFuture (@PathVariable String instrumentToken, @RequestParam(defaultValue = "2") int days) {


        //InstrumentID: 146796039 : CrudeOILM
        //InstrumentID: 15736578   : NIFTY NOV FUT
        // For demonstration, we return a TradeEntryExit object with dummy values.
        return  tradeCalculatorService.getTradeCaptureForNiftyFutureM(instrumentToken, days);
    }
}
