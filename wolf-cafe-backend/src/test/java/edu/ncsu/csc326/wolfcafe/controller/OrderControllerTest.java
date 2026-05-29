package edu.ncsu.csc326.wolfcafe.controller;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemRequestDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderPlaceDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderController REST endpoints.
 * Tests cover all order-related endpoints with various scenarios.
 */
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderDto testOrder;
    private OrderPlaceDto testOrderPlaceDto;

    @BeforeEach
    public void setUp() {
        testOrder = new OrderDto();
        testOrder.setId(1L);
        testOrder.setCustomerName("testcustomer");
        testOrder.setSubtotal(10.00);
        testOrder.setTax(0.20);
        testOrder.setTip(1.50);
        testOrder.setTotal(11.70);
        testOrder.setStatus("PENDING");
        testOrder.setItems(new ArrayList<>());

        testOrderPlaceDto = new OrderPlaceDto();
        testOrderPlaceDto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 2)));
        testOrderPlaceDto.setTipType("PERCENTAGE");
        testOrderPlaceDto.setTipValue(15);
    }

    // ============ Tests for placeOrder ============

    @Test
    public void testPlaceOrder_Success() {
        when(orderService.placeOrder(any(OrderPlaceDto.class))).thenReturn(testOrder);

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertEquals(1L, returnedOrder.getId());
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_EmptyOrder() {
        when(orderService.placeOrder(any(OrderPlaceDto.class)))
                .thenThrow(new IllegalArgumentException("Order must contain at least one item"));

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Order must contain at least one item", body.get("error"));
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_NegativeTip() {
        when(orderService.placeOrder(any(OrderPlaceDto.class)))
                .thenThrow(new IllegalArgumentException("Tip amount cannot be negative"));

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_InsufficientInventory() {
        when(orderService.placeOrder(any(OrderPlaceDto.class)))
                .thenThrow(new IllegalArgumentException("Insufficient inventory for item: Coffee"));

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_GuestSuccess() {
        OrderDto guestOrder = new OrderDto();
        guestOrder.setId(2L);
        guestOrder.setCustomerName("Guest");
        guestOrder.setSubtotal(10.00);
        guestOrder.setTax(0.20);
        guestOrder.setTip(0.00);
        guestOrder.setTotal(10.20);
        guestOrder.setStatus("PENDING");
        guestOrder.setMessage("Order placed successfully");
        when(orderService.placeOrder(any(OrderPlaceDto.class))).thenReturn(guestOrder);

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertNotNull(returnedOrder);
        assertEquals("Guest", returnedOrder.getCustomerName());
        assertEquals("Order placed successfully", returnedOrder.getMessage());
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_StaffBlocked() {
        when(orderService.placeOrder(any(OrderPlaceDto.class)))
                .thenThrow(new IllegalArgumentException("Only customers or guests can place orders"));

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Only customers or guests can place orders", body.get("error"));
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    @Test
    public void testPlaceOrder_AdminBlocked() {
        when(orderService.placeOrder(any(OrderPlaceDto.class)))
                .thenThrow(new IllegalArgumentException("Only customers or guests can place orders"));

        ResponseEntity<?> response = orderController.placeOrder(testOrderPlaceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Only customers or guests can place orders", body.get("error"));
        verify(orderService, times(1)).placeOrder(any(OrderPlaceDto.class));
    }

    // ============ Tests for getPendingOrders ============

    @Test
    public void testGetPendingOrders_Success() {
        List<OrderDto> pendingOrders = Arrays.asList(testOrder);
        when(orderService.getPendingOrders()).thenReturn(pendingOrders);

        ResponseEntity<List<OrderDto>> response = orderController.getPendingOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(orderService, times(1)).getPendingOrders();
    }

    @Test
    public void testGetPendingOrders_Empty() {
        when(orderService.getPendingOrders()).thenReturn(new ArrayList<>());

        ResponseEntity<List<OrderDto>> response = orderController.getPendingOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(orderService, times(1)).getPendingOrders();
    }

    // ============ Tests for getOrder ============

    @Test
    public void testGetOrder_Success() {
        when(orderService.getOrder(1L)).thenReturn(testOrder);

        ResponseEntity<?> response = orderController.getOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertEquals(1L, returnedOrder.getId());
        verify(orderService, times(1)).getOrder(1L);
    }

    @Test
    public void testGetOrder_NotFound() {
        when(orderService.getOrder(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        ResponseEntity<?> response = orderController.getOrder(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderService, times(1)).getOrder(99L);
    }

    // ============ Tests for getMyOrders ============

    @Test
    public void testGetMyOrders_Success() {
        List<OrderDto> customerOrders = Arrays.asList(testOrder);
        when(orderService.getMyOrders()).thenReturn(customerOrders);

        ResponseEntity<List<OrderDto>> response = orderController.getMyOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(orderService, times(1)).getMyOrders();
    }

    @Test
    public void testGetMyOrders_Empty() {
        when(orderService.getMyOrders()).thenReturn(new ArrayList<>());

        ResponseEntity<List<OrderDto>> response = orderController.getMyOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(orderService, times(1)).getMyOrders();
    }

    // ============ Tests for fulfillOrder ============

    @Test
    public void testFulfillOrder_Success() {
        testOrder.setStatus("READY_FOR_PICKUP");
        testOrder.setMessage("Order fulfilled successfully");
        when(orderService.fulfillOrder(1L)).thenReturn(testOrder);

        ResponseEntity<?> response = orderController.fulfillOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertEquals("READY_FOR_PICKUP", returnedOrder.getStatus());
        assertEquals("Order fulfilled successfully", returnedOrder.getMessage());
        verify(orderService, times(1)).fulfillOrder(1L);
    }

    @Test
    public void testFulfillOrder_AlreadyFulfilled() {
        OrderDto dto = new OrderDto();
        dto.setId(1L);
        dto.setStatus("READY_FOR_PICKUP");
        dto.setMessage("Order already fulfilled");
        when(orderService.fulfillOrder(1L)).thenReturn(dto);

        ResponseEntity<?> response = orderController.fulfillOrder(1L);

        // Returns 200 with informational message — not an error
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderDto responseBody = (OrderDto) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Order already fulfilled", responseBody.getMessage());
        verify(orderService, times(1)).fulfillOrder(1L);
    }

    @Test
    public void testFulfillOrder_NotFound() {
        when(orderService.fulfillOrder(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        ResponseEntity<?> response = orderController.fulfillOrder(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Order not found", body.get("error"));
        verify(orderService, times(1)).fulfillOrder(99L);
    }

    // ============ Tests for cancelOrder ============

    @Test
    public void testCancelOrder_Success_PendingOrder() {
        OrderDto cancelledOrder = new OrderDto();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus("CANCELLED");
        cancelledOrder.setMessage("Order cancelled successfully");
        when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

        ResponseEntity<?> response = orderController.cancelOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertEquals("CANCELLED", returnedOrder.getStatus());
        assertEquals("Order cancelled successfully", returnedOrder.getMessage());
        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    public void testCancelOrder_Success_ReadyForPickupOrder() {
        // Staff can also cancel an order that is READY_FOR_PICKUP
        OrderDto cancelledOrder = new OrderDto();
        cancelledOrder.setId(2L);
        cancelledOrder.setStatus("CANCELLED");
        cancelledOrder.setMessage("Order cancelled successfully");
        when(orderService.cancelOrder(2L)).thenReturn(cancelledOrder);

        ResponseEntity<?> response = orderController.cancelOrder(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertNotNull(returnedOrder);
        assertEquals("CANCELLED", returnedOrder.getStatus());
        verify(orderService, times(1)).cancelOrder(2L);
    }

    @Test
    public void testCancelOrder_NotFound() {
        when(orderService.cancelOrder(99L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        ResponseEntity<?> response = orderController.cancelOrder(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Order not found", body.get("error"));
        verify(orderService, times(1)).cancelOrder(99L);
    }

    @Test
    public void testCancelOrder_AlreadyPickedUp() {
        // Cannot cancel an order that has already been picked up
        when(orderService.cancelOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "Order cannot be cancelled in its current state: PICKED_UP"));

        ResponseEntity<?> response = orderController.cancelOrder(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("cannot be cancelled"));
        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    public void testCancelOrder_AlreadyCancelled() {
        // Cannot cancel an already-cancelled order
        when(orderService.cancelOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "Order cannot be cancelled in its current state: CANCELLED"));

        ResponseEntity<?> response = orderController.cancelOrder(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("cannot be cancelled"));
        verify(orderService, times(1)).cancelOrder(1L);
    }

    // ============ Tests for pickupOrder ============

    @Test
    public void testPickupOrder_Success() {
        testOrder.setStatus("PICKED_UP");
        testOrder.setMessage("Order picked up successfully");
        when(orderService.pickupOrder(1L)).thenReturn(testOrder);

        ResponseEntity<?> response = orderController.pickupOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        OrderDto returnedOrder = (OrderDto) response.getBody();
        assertEquals("PICKED_UP", returnedOrder.getStatus());
        assertEquals("Order picked up successfully", returnedOrder.getMessage());
        verify(orderService, times(1)).pickupOrder(1L);
    }

    @Test
    public void testPickupOrder_NotReady() {
        // Order is still PENDING — cannot be picked up
        when(orderService.pickupOrder(1L))
                .thenThrow(new IllegalArgumentException("Order is not ready for pickup"));

        ResponseEntity<?> response = orderController.pickupOrder(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Order is not ready for pickup", body.get("error"));
        verify(orderService, times(1)).pickupOrder(1L);
    }

    @Test
    public void testPickupOrder_NotFound() {
        when(orderService.pickupOrder(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        ResponseEntity<?> response = orderController.pickupOrder(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderService, times(1)).pickupOrder(99L);
    }

    @Test
    public void testPickupOrder_WrongCustomer() {
        when(orderService.pickupOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "You are not authorized to pick up this order"));

        ResponseEntity<?> response = orderController.pickupOrder(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("You are not authorized to pick up this order", body.get("error"));
        verify(orderService, times(1)).pickupOrder(1L);
    }

    @Test
    public void testPickupOrder_GuestOrderRejected() {
        when(orderService.pickupOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "Guest orders cannot be picked up through this endpoint"));

        ResponseEntity<?> response = orderController.pickupOrder(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("Guest orders"));
        verify(orderService, times(1)).pickupOrder(1L);
    }
    
 // ============ Tests for getAllOrders ============

    @Test
    public void testGetAllOrders_Success() {
        OrderDto fulfilledOrder = new OrderDto();
        fulfilledOrder.setId(2L);
        fulfilledOrder.setCustomerName("testcustomer");
        fulfilledOrder.setStatus("READY_FOR_PICKUP");

        OrderDto cancelledOrder = new OrderDto();
        cancelledOrder.setId(3L);
        cancelledOrder.setCustomerName("othercustomer");
        cancelledOrder.setStatus("CANCELLED");

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrder, fulfilledOrder, cancelledOrder));

        ResponseEntity<List<OrderDto>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    public void testGetAllOrders_Empty() {
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());

        ResponseEntity<List<OrderDto>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    public void testGetAllOrders_ContainsAllStatuses() {
        OrderDto pending = new OrderDto();
        pending.setId(1L);
        pending.setStatus("PENDING");

        OrderDto ready = new OrderDto();
        ready.setId(2L);
        ready.setStatus("READY_FOR_PICKUP");

        OrderDto pickedUp = new OrderDto();
        pickedUp.setId(3L);
        pickedUp.setStatus("PICKED_UP");

        OrderDto cancelled = new OrderDto();
        cancelled.setId(4L);
        cancelled.setStatus("CANCELLED");

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(pending, ready, pickedUp, cancelled));

        ResponseEntity<List<OrderDto>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<OrderDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(4, body.size());
        List<String> statuses = body.stream()
                .map(OrderDto::getStatus)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(statuses.contains("PENDING"));
        assertTrue(statuses.contains("READY_FOR_PICKUP"));
        assertTrue(statuses.contains("PICKED_UP"));
        assertTrue(statuses.contains("CANCELLED"));
        verify(orderService, times(1)).getAllOrders();
    }
}
