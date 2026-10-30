package com.quantraksha.kite.api;

import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.service.LevelCalculatorService;
import com.quantraksha.kite.core.session.KiteSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * N-day breakout levels for the commodity strategy: HH is the highest high and LL the lowest low of
 * the last N completed daily candles. /api/levels/{instrumentToken}?days=2 gives 2D HH / 2D LL.
 * Today's candle is still forming, so it is left out.
 */
@RestController
@RequestMapping("/api")
public class LevelsController {

    private final KiteSessionService kite;

    private LevelCalculatorService levelCalculatorService;

    public LevelsController(KiteSessionService kite) {
        this.kite = kite;
    }

    @GetMapping("/levels/{instrumentToken}")
    public Levels levels(@PathVariable String instrumentToken, @RequestParam(defaultValue = "2") int days) {
       return  levelCalculatorService.getLevels(instrumentToken, days);
    }
}
