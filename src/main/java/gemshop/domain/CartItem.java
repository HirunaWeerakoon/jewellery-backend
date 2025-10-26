package gemshop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cart_item", columnNames = {"cart_id", "gemstone_id"})
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gemstone_id", nullable = false)
    private Gemstone gemstone;

    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice; // snapshot from gemstone.basePrice

    public CartItem() {}

    public Long getId() { return id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Gemstone getGemstone() { return gemstone; }
    public void setGemstone(Gemstone gemstone) { this.gemstone = gemstone; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
