package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order in the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    /** Order id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the customer who placed the order */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private User customer;
    
    /** Guest flag */
    @Column(name = "guest_order", nullable = false)
    private boolean guestOrder = false;
    
    /** Track version for simultaneous updates */
    @Version
    private Long version;

    /** Order status */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /** List of items in this order */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    /** Subtotal before tax and tip */
    @Column(nullable = false)
    private double subtotal;

    /** Sales tax amount */
    @Column(nullable = false)
    private double tax;

    /** Tip amount */
    @Column(nullable = false)
    private double tip;

    /** Total including subtotal, tax, and tip */
    @Column(nullable = false)
    private double total;

    /** Timestamp when the order was created */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the order was fulfilled (ready for pickup) */
    private LocalDateTime fulfilledAt;

    /** Timestamp when the customer picked up the order */
    private LocalDateTime pickedUpAt;

    /** Timestamp when the order was cancelled */
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
}
