package gemshop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "gemstone")
public class Gemstone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "stone_type")
    private String stoneType;

    @Column(precision = 10, scale = 2)
    private BigDecimal carat;   // <— changed from Double to BigDecimal

    private String quality;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    public Gemstone() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStoneType() { return stoneType; }
    public void setStoneType(String stoneType) { this.stoneType = stoneType; }

    public BigDecimal getCarat() { return carat; }
    public void setCarat(BigDecimal carat) { this.carat = carat; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
}
