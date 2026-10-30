package com.quantraksha.kite.api;

import com.quantraksha.kite.session.KiteSessionService;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.Margin;
import com.zerodhatech.models.Position;
import com.zerodhatech.models.Profile;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final KiteSessionService kite;

    public AccountController(KiteSessionService kite) {
        this.kite = kite;
    }

    @GetMapping("/profile")
    public Profile profile() {
        return kite.call(k -> k.getProfile());
    }

    @GetMapping("/margins")
    public Map<String, Margin> margins() {
        return kite.call(k -> k.getMargins());
    }

    /** segment is "equity" or "commodity" */
    @GetMapping("/margins/{segment}")
    public Margin margins(@PathVariable String segment) {
        return kite.call(k -> k.getMargins(segment));
    }

    /** {"net": [...], "day": [...]} */
    @GetMapping("/positions")
    public Map<String, List<Position>> positions() {
        return kite.call(k -> k.getPositions());
    }

    @GetMapping("/holdings")
    public List<Holding> holdings() {
        return kite.call(k -> k.getHoldings());
    }
}
