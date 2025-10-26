package gemshop.web;

import gemshop.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService svc;

    public CartController(CartService svc) {
        this.svc = svc;
    }

    // GET /api/cart?userId=1
    @GetMapping
    public Map<String, Object> view(@RequestParam Long userId) {
        return svc.viewCart(userId);
    }

    // POST /api/cart/items?userId=1&gemstoneId=2&qty=3
    @PostMapping("/items")
    public Map<String, Object> add(@RequestParam Long userId,
                                   @RequestParam Long gemstoneId,
                                   @RequestParam int qty) {
        svc.addItem(userId, gemstoneId, qty);
        return svc.viewCart(userId);
    }

    // PUT /api/cart/items/{itemId}?userId=1&qty=5
    @PutMapping("/items/{itemId}")
    public Map<String, Object> update(@PathVariable Long itemId,
                                      @RequestParam Long userId,
                                      @RequestParam int qty) {
        svc.updateItem(userId, itemId, qty);
        return svc.viewCart(userId);
    }

    // DELETE /api/cart/items/{itemId}?userId=1
    @DeleteMapping("/items/{itemId}")
    public Map<String, Object> remove(@PathVariable Long itemId,
                                      @RequestParam Long userId) {
        svc.removeItem(userId, itemId);
        return svc.viewCart(userId);
    }

    // POST /api/cart/checkout?userId=1
    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestParam Long userId) {
        return svc.checkout(userId);
    }
}
