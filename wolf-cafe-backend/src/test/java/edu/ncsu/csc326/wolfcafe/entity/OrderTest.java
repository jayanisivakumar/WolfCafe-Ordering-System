package edu.ncsu.csc326.wolfcafe.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order entity.
 * Tests cover entity construction, getters/setters, and business logic.
 */
public class OrderTest {

    private Order order;
    private User customer;
    private Item item;
    private OrderItem orderItem;

    @BeforeEach
    public void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setUsername("testcustomer");

        item = new Item();
        item.setId(1L);
        item.setName("Coffee");
        item.setPrice(5.00);

        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setQuantity(2);
        orderItem.setPrice(5.00);
        orderItem.setItem(item);

        order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());
    }

    // ============ Tests for Order Entity ============

    @Test
    public void testOrderCreation() {
        assertNotNull(order);
        assertNotNull(order.getCustomer());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getItems());
    }

    @Test
    public void testOrderSettersAndGetters() {
        order.setId(1L);
        order.setSubtotal(10.00);
        order.setTax(0.20);
        order.setTip(1.50);
        order.setTotal(11.70);

        assertEquals(1L, order.getId());
        assertEquals(10.00, order.getSubtotal());
        assertEquals(0.20, order.getTax());
        assertEquals(1.50, order.getTip());
        assertEquals(11.70, order.getTotal());
    }

    @Test
    public void testOrderVersionField() {
        order.setVersion(1L);
        assertEquals(1L, order.getVersion());
    }

    @Test
    public void testOrderStatusTransitions() {
        assertEquals(OrderStatus.PENDING, order.getStatus());

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());

        order.setStatus(OrderStatus.PICKED_UP);
        assertEquals(OrderStatus.PICKED_UP, order.getStatus());

        order.setStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    public void testOrderTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setFulfilledAt(now.plusHours(1));
        order.setPickedUpAt(now.plusHours(2));
        order.setCancelledAt(now.plusHours(3));

        assertEquals(now, order.getCreatedAt());
        assertEquals(now.plusHours(1), order.getFulfilledAt());
        assertEquals(now.plusHours(2), order.getPickedUpAt());
        assertEquals(now.plusHours(3), order.getCancelledAt());
    }

    @Test
    public void testOrderItems() {
        order.getItems().add(orderItem);
        assertEquals(1, order.getItems().size());
        assertEquals(orderItem, order.getItems().get(0));
    }

    @Test
    public void testOrderItemsRelationship() {
        orderItem.setOrder(order);
        assertEquals(order, orderItem.getOrder());
    }

    @Test
    public void testOrderTotalCalculation() {
        order.setSubtotal(100.00);
        order.setTax(2.00);
        order.setTip(15.00);
        order.setTotal(117.00);

        double expectedTotal = 100.00 + 2.00 + 15.00;
        assertEquals(expectedTotal, order.getTotal());
    }

    @Test
    public void testOrderNoArgsConstructor() {
        Order newOrder = new Order();
        assertNotNull(newOrder);
        assertNull(newOrder.getId());
        assertNull(newOrder.getCustomer());
        assertFalse(newOrder.isGuestOrder());
    }

    @Test
    public void testOrderAllArgsConstructor() {
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        Order newOrder = new Order(
                1L,
                customer,
                false,
                null,
                OrderStatus.PENDING,
                items,
                10.00,
                0.20,
                1.50,
                11.70,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        assertEquals(1L, newOrder.getId());
        assertEquals(customer, newOrder.getCustomer());
        assertFalse(newOrder.isGuestOrder());
        assertNull(newOrder.getVersion());
        assertEquals(OrderStatus.PENDING, newOrder.getStatus());
        assertEquals(1, newOrder.getItems().size());
        assertEquals(10.00, newOrder.getSubtotal());
        assertEquals(0.20, newOrder.getTax());
        assertEquals(1.50, newOrder.getTip());
        assertEquals(11.70, newOrder.getTotal());
        assertNotNull(newOrder.getCreatedAt());
        assertNull(newOrder.getFulfilledAt());
        assertNull(newOrder.getPickedUpAt());
        assertNull(newOrder.getCancelledAt());
    }

    @Test
    public void testOrderAllArgsConstructor_GuestOrder() {
        Order newOrder = new Order(
                2L,
                null,
                true,
                null,
                OrderStatus.PENDING,
                new ArrayList<>(),
                5.00,
                0.10,
                0.00,
                5.10,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        assertEquals(2L, newOrder.getId());
        assertNull(newOrder.getCustomer());
        assertTrue(newOrder.isGuestOrder());
        assertEquals(OrderStatus.PENDING, newOrder.getStatus());
    }

    // ============ Tests for @PrePersist onCreate() ============

    @Test
    public void testOnCreate_BothNull() {
        // Both createdAt and status are null — onCreate sets both
        Order newOrder = new Order();
        newOrder.onCreate();
        assertNotNull(newOrder.getCreatedAt());
        assertEquals(OrderStatus.PENDING, newOrder.getStatus());
    }

    @Test
    public void testOnCreate_CreatedAtAlreadySet() {
        // createdAt is already set — should not be overwritten
        Order newOrder = new Order();
        LocalDateTime existing = LocalDateTime.of(2024, 1, 1, 12, 0);
        newOrder.setCreatedAt(existing);
        newOrder.onCreate();
        assertEquals(existing, newOrder.getCreatedAt());
        assertEquals(OrderStatus.PENDING, newOrder.getStatus());
    }

    @Test
    public void testOnCreate_StatusAlreadySet() {
        // status is already set — should not be overwritten
        Order newOrder = new Order();
        newOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
        newOrder.onCreate();
        assertNotNull(newOrder.getCreatedAt());
        assertEquals(OrderStatus.READY_FOR_PICKUP, newOrder.getStatus());
    }

    @Test
    public void testOnCreate_BothAlreadySet() {
        // Both are set — neither should be overwritten
        Order newOrder = new Order();
        LocalDateTime existing = LocalDateTime.of(2024, 6, 15, 9, 30);
        newOrder.setCreatedAt(existing);
        newOrder.setStatus(OrderStatus.CANCELLED);
        newOrder.onCreate();
        assertEquals(existing, newOrder.getCreatedAt());
        assertEquals(OrderStatus.CANCELLED, newOrder.getStatus());
    }

    // ============ Tests for guestOrder field ============

    @Test
    public void testGuestOrder_DefaultFalse() {
        Order newOrder = new Order();
        assertFalse(newOrder.isGuestOrder());
    }

    @Test
    public void testGuestOrder_SetTrue() {
        Order newOrder = new Order();
        newOrder.setGuestOrder(true);
        assertTrue(newOrder.isGuestOrder());
        assertNull(newOrder.getCustomer());
    }

    @Test
    public void testGuestOrder_SetFalse() {
        Order newOrder = new Order();
        newOrder.setGuestOrder(true);
        newOrder.setGuestOrder(false);
        assertFalse(newOrder.isGuestOrder());
    }

    @Test
    public void testGuestOrder() {
        Order guestOrder = new Order();
        assertFalse(guestOrder.isGuestOrder());
        guestOrder.setGuestOrder(true);
        assertTrue(guestOrder.isGuestOrder());
        assertNull(guestOrder.getCustomer());
    }

    // ============ Tests for OrderItem Entity ============

    @Test
    public void testOrderItemCreation() {
        assertNotNull(orderItem);
        assertEquals(2, orderItem.getQuantity());
        assertEquals(5.00, orderItem.getPrice());
    }

    @Test
    public void testOrderItemSettersAndGetters() {
        orderItem.setId(2L);
        orderItem.setQuantity(3);
        orderItem.setPrice(3.50);

        assertEquals(2L, orderItem.getId());
        assertEquals(3, orderItem.getQuantity());
        assertEquals(3.50, orderItem.getPrice());
    }

    @Test
    public void testOrderItemNoArgsConstructor() {
        OrderItem newItem = new OrderItem();
        assertNotNull(newItem);
        assertNull(newItem.getId());
    }

    @Test
    public void testOrderItemAllArgsConstructor() {
        OrderItem newItem = new OrderItem(
                2L,
                order,
                item,
                5,
                4.50
        );

        assertEquals(2L, newItem.getId());
        assertEquals(order, newItem.getOrder());
        assertEquals(item, newItem.getItem());
        assertEquals(5, newItem.getQuantity());
        assertEquals(4.50, newItem.getPrice());
    }

    @Test
    public void testOrderItemItemRelationship() {
        assertEquals("Coffee", orderItem.getItem().getName());
        assertEquals(5.00, orderItem.getItem().getPrice());
    }

    // ============ Tests for OrderStatus Enum ============

    @Test
    public void testOrderStatusEnum() {
        assertEquals(OrderStatus.PENDING, OrderStatus.PENDING);
        assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.READY_FOR_PICKUP);
        assertEquals(OrderStatus.PICKED_UP, OrderStatus.PICKED_UP);
        assertEquals(OrderStatus.CANCELLED, OrderStatus.CANCELLED);
    }

    @Test
    public void testOrderStatusValues() {
        OrderStatus[] statuses = OrderStatus.values();
        assertEquals(4, statuses.length);
        assertEquals("PENDING", statuses[0].toString());
    }

    @Test
    public void testOrderStatusOrdinal() {
        assertEquals(0, OrderStatus.PENDING.ordinal());
        assertEquals(1, OrderStatus.READY_FOR_PICKUP.ordinal());
        assertEquals(2, OrderStatus.PICKED_UP.ordinal());
        assertEquals(3, OrderStatus.CANCELLED.ordinal());
    }

    // ============ Tests for TipType Enum ============

    @Test
    public void testTipTypeEnum() {
        assertEquals(TipType.PERCENTAGE, TipType.PERCENTAGE);
        assertEquals(TipType.CUSTOM, TipType.CUSTOM);
        assertEquals(TipType.NONE, TipType.NONE);
    }

    @Test
    public void testTipTypeValues() {
        TipType[] tipTypes = TipType.values();
        assertEquals(3, tipTypes.length);
    }

    @Test
    public void testTipTypeToString() {
        assertEquals("PERCENTAGE", TipType.PERCENTAGE.toString());
        assertEquals("CUSTOM", TipType.CUSTOM.toString());
        assertEquals("NONE", TipType.NONE.toString());
    }

    // ============ Tests for Order Business Logic ============

    @Test
    public void testOrderMultipleItems() {
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setQuantity(1);
        item2.setPrice(3.00);

        order.getItems().add(orderItem);
        order.getItems().add(item2);

        assertEquals(2, order.getItems().size());
        assertEquals(13.00, order.getItems().get(0).getPrice() * order.getItems().get(0).getQuantity()
                + order.getItems().get(1).getPrice() * order.getItems().get(1).getQuantity());
    }

    @Test
    public void testOrderCustomerRelationship() {
        order.setCustomer(customer);
        assertEquals("testcustomer", order.getCustomer().getUsername());
    }
}