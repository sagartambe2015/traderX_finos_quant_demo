package com.quantraksha.kite.api;

import com.quantraksha.kite.persist.dto.TradeSignalResponse;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.service.TradePositionService;
import com.quantraksha.kite.persist.service.TradeScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TradePositionController {
    private static final Logger log = LoggerFactory.getLogger(TradePositionController.class);

    public TradePositionService tradePositionService;
    public TradePositionController(TradePositionService tradePositionService) {
        this.tradePositionService = tradePositionService;
    }

    @GetMapping("/getAllTradesAndPosition")
    public List<TradeSignalResponse> showTradesAndPosition () {
        return  tradePositionService.getAllTradePosition();
    }




}
