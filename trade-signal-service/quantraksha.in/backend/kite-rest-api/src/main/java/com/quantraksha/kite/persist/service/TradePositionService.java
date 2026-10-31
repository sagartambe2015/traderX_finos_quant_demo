package com.quantraksha.kite.persist.service;

import com.quantraksha.kite.persist.dto.TradeSignalResponse;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.repository.TradeCaptureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradePositionService {

    private static final Logger log = LoggerFactory.getLogger(TradePositionService.class);

    protected TradeCaptureRepository tradeCaptureRepository;
    public TradePositionService(TradeCaptureRepository tradeCaptureRepository) {
        this.tradeCaptureRepository = tradeCaptureRepository;

    }


    public List<TradeSignalResponse> getAllTradePosition() {
        return tradeCaptureRepository.findAll().stream().map(TradeSignalResponse::from).toList();
    }



}
