package com.quantraksha.kite.api;

import com.quantraksha.kite.session.KiteSessionService;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Trade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only for now: today's orders and trades. */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final KiteSessionService kite;

    public OrderController(KiteSessionService kite) {
        this.kite = kite;
    }

    @GetMapping("/orders")
    public List<Order> orders() {
        return kite.call(k -> k.getOrders());
    }

    @GetMapping("/orders/{orderId}/history")
    public List<Order> orderHistory(@PathVariable String orderId) {
        return kite.call(k -> k.getOrderHistory(orderId));
    }

    @GetMapping("/trades")
    public List<Trade> trades() {
        return kite.call(k -> k.getTrades());
    }
}
