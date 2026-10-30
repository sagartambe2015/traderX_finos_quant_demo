package com.quantraksha.kite.api;


import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;
import com.quantraksha.kite.service.LevelCalculatorService;
import com.quantraksha.kite.service.TradeCaptureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TradeCaptureController {
    private static final Logger log = LoggerFactory.getLogger(TradeCaptureController.class);


    private final LevelCalculatorService levelCalculatorService;

    private  final TradeCaptureService tradeCaptureService;

    public TradeCaptureController(LevelCalculatorService levelCalculatorService, TradeCaptureService tradeCaptureService) {

        this.levelCalculatorService = levelCalculatorService;
        this.tradeCaptureService = tradeCaptureService;
    }


    @GetMapping("/calculateTrade/mcx/{instrumentToken}")
    public TradeCapture calculateEntryForCrudeOilM (@PathVariable String instrumentToken, @RequestParam(defaultValue = "2") int days) {
        Levels levels =  levelCalculatorService.getLevels(instrumentToken, days);
        log.info("Levels Instrument:={}, Highest High:={} and Lowest Low:={} ", levels.instrumentToken() , levels.hh() , levels.ll());
        TradeCapture tradeCapture = tradeCaptureService.getTradeCaptureForCrudeOilMFuture(levels);
        log.info("Trade Capture Entity: {}", tradeCapture);

        //InstrumentID: 146796039 : CrudeOILM
        //InstrumentID: 15736578   : NIFTY NOV FUT
        // For demonstration, we return a TradeEntryExit object with dummy values.
        return tradeCapture;
    }

    @GetMapping("/calculateTrade/nse/{instrumentToken}")
    public TradeCapture calculateEntryForNiftyFuture (@PathVariable String instrumentToken, @RequestParam(defaultValue = "2") int days) {
        Levels levels =  levelCalculatorService.getLevels(instrumentToken, days);
        log.info("Levels Instrument:={}, Highest High:={} and Lowest Low:={} ", levels.instrumentToken() , levels.hh() , levels.ll());
        TradeCapture tradeCapture = tradeCaptureService.getTradeCaptureForNiftyFuture(levels);
        log.info("Trade Capture Entity: {}", tradeCapture);

        //InstrumentID: 146796039 : CrudeOILM
        //InstrumentID: 15736578   : NIFTY NOV FUT
        // For demonstration, we return a TradeEntryExit object with dummy values.
        return tradeCapture;
    }
}
