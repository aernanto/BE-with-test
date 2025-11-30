package apap.ti._5.tour_package_2306165963_be.model.loyalty;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchased_coupons")
public class PurchasedCoupon {

    @Id
    @GeneratedValue(generator = "system-uuid")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Column(name = "purchased_date", nullable = false)
    private LocalDateTime purchasedDate;

    @Column(name = "used_date")
    private LocalDateTime usedDate;

    @PrePersist
    void onCreate() {
        purchasedDate = LocalDateTime.now();
    }
}