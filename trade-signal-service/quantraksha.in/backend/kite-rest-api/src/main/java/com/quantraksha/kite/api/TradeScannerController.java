package com.quantraksha.kite.api;

import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.core.service.TradeCalculatorService;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.mapper.TradeCaptureToTradingMapper;
import com.quantraksha.kite.persist.service.TradeScannerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TradeScannerController {

    private TradeScannerService tradeScannerService;
    public TradeScannerController(TradeScannerService tradeScannerService) {
       this.tradeScannerService = tradeScannerService;
    }

    @GetMapping("/trade-scan")
    public String trade_scan () {
        return  tradeScannerService.scanAndSaveTradeSignal();
    }
}
