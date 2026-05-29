package edu.ncsu.csc326.wolfcafe.service.impl;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemRequestDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderPlaceDto;
import edu.ncsu.csc326.wolfcafe.entity.*;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.*;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;
import edu.ncsu.csc326.wolfcafe.service.util.InventoryUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the OrderService interface.
 * Handles placing orders, viewing pending orders, and fulfilling/picking up orders.
 */
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private InventoryRepository inventoryRepository;
    private TaxRateService taxRateService;

    /**
     * Rounds a monetary value to 2 decimal places.
     * @param value the value to round
     * @return the rounded value
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Returns the username of the currently authenticated user.
     * @return current username or "anonymous"
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }

    /**
     * Returns the current authenticated user.
     * @return the User entity
     * @throws ResourceNotFoundException if user not found
     */
    private User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Converts an Order entity to an OrderDto.
     * @param order the order entity
     * @return the order DTO
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCustomerName(order.isGuestOrder() ? "Guest" : order.getCustomer().getUsername());
        dto.setSubtotal(roundToTwoDecimals(order.getSubtotal()));
        dto.setTax(roundToTwoDecimals(order.getTax()));
        dto.setTip(roundToTwoDecimals(order.getTip()));
        dto.setTotal(roundToTwoDecimals(order.getTotal()));
        dto.setStatus(order.getStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setFulfilledAt(order.getFulfilledAt());
        dto.setPickedUpAt(order.getPickedUpAt());
        dto.setCancelledAt(order.getCancelledAt());

        // Convert order items
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(oi -> new OrderItemDto(
                        oi.getItem().getId(),
                        oi.getItem().getName(),
                        oi.getQuantity(),
                        roundToTwoDecimals(oi.getPrice())
                ))
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    /**
     * Places a new order for the authenticated customer.
     * Validates items, calculates totals, and saves the order. Inventory is NOT
     * deducted here — deduction happens at fulfillment time.
     *
     * @param orderPlaceDto the order details
     * @return the created order
     * @throws IllegalArgumentException if order is empty or tip is invalid
     * @throws ResourceNotFoundException if item not found
     */
    @Override
    @Transactional
    public OrderDto placeOrder(OrderPlaceDto orderPlaceDto) {
        // Determine if this is an authenticated customer or anonymous guest
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isGuest = auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser");

        User customer = null;
        if (!isGuest) {
            // Block staff and admin from placing orders
            boolean isCustomer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
            if (!isCustomer) {
                throw new IllegalArgumentException("Only customers or guests can place orders");
            }
            customer = getCurrentUser();
        }

        // Validate order has at least one item
        if (orderPlaceDto.getItems() == null || orderPlaceDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Validate tip
        double tipAmount = 0;
        if (orderPlaceDto.getTipType() != null && !orderPlaceDto.getTipType().equalsIgnoreCase("NONE")) {
            if (orderPlaceDto.getTipValue() < 0) {
                throw new IllegalArgumentException("Tip amount cannot be negative");
            }
            tipAmount = orderPlaceDto.getTipValue();
        }

        // Create order and items list
        Order order = new Order();
        if (!isGuest) {
            order.setCustomer(customer);
        }
        order.setGuestOrder(isGuest);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        double subtotal = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // Process each item in the order
        for (OrderItemRequestDto itemRequest : orderPlaceDto.getItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(item.getPrice());

            orderItems.add(orderItem);
            subtotal += item.getPrice() * itemRequest.getQuantity();
        }

        subtotal = roundToTwoDecimals(subtotal);

        double taxRate = taxRateService.getTaxRate().getRate() / 100.0;
        double tax = roundToTwoDecimals(subtotal * taxRate);

        if ("PERCENTAGE".equalsIgnoreCase(orderPlaceDto.getTipType())) {
            tipAmount = roundToTwoDecimals(subtotal * (tipAmount / 100.0));
        } else if ("CUSTOM".equalsIgnoreCase(orderPlaceDto.getTipType())) {
            tipAmount = roundToTwoDecimals(orderPlaceDto.getTipValue());
        } else {
            tipAmount = 0;
        }

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTip(tipAmount);
        order.setTotal(roundToTwoDecimals(subtotal + tax + tipAmount));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Log differently for guest vs authenticated customer
        if (isGuest) {
            logger.info("[anonymous, GUEST, PLACE_ORDER, {}]", savedOrder.getId());
        } else {
            logger.info("[{}, CUSTOMER, PLACE_ORDER, {}]", getCurrentUsername(), savedOrder.getId());
        }

        OrderDto resultDto = convertToDto(savedOrder);
        resultDto.setMessage("Order placed successfully");
        return resultDto;
    }

    /**
     * Gets all pending orders for staff fulfillment.
     * @return list of pending orders
     */
    @Override
    public List<OrderDto> getPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        return pendingOrders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets an order by ID.
     * @param id the order id
     * @return the order
     * @throws ResourceNotFoundException if order not found
     */
    @Override
    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return convertToDto(order);
    }

    /**
     * Gets all orders for the authenticated customer.
     * @return list of customer's orders
     */
    @Override
    public List<OrderDto> getMyOrders() {
        User customer = getCurrentUser();
        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks an order as fulfilled (ready for pickup).
     * Checks inventory for sufficient ingredients, deducts them, and updates the order status.
     * @param orderId the order id
     * @return the updated order
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalArgumentException if order is not pending or inventory is insufficient
     */
    @Override
    @Transactional
    public OrderDto fulfillOrder(Long orderId) {
    	try {
    		Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (order.getStatus() != OrderStatus.PENDING) {
                OrderDto dto = convertToDto(order);
                dto.setMessage("Order already fulfilled");
                return dto;
            }

            // Check and deduct inventory at fulfillment time
            List<Inventory> inventories = inventoryRepository.findAll();
            if (inventories.isEmpty()) {
                throw new IllegalArgumentException("No inventory exists. Cannot fulfill order without inventory.");
            }
            Inventory inventory = inventories.get(0);
            if (!InventoryUtils.hasEnoughForOrderItems(inventory, order.getItems())) {
                throw new IllegalArgumentException("Insufficient inventory to fulfill this order");
            }
            InventoryUtils.deductOrderItemsFromInventory(inventory, order.getItems());
            inventoryRepository.save(inventory);

            order.setStatus(OrderStatus.READY_FOR_PICKUP);
            order.setFulfilledAt(LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);

            String username = getCurrentUsername();
            logger.info("[{}, STAFF, FULFILL_ORDER, {}]", username, orderId);

            OrderDto resultDto = convertToDto(savedOrder);
            resultDto.setMessage("Order fulfilled successfully");
            return resultDto;
    	} catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
    		// checks version errors for concurrent fulfillments
    		Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    		
    		OrderDto dto = convertToDto(order);
    		dto.setMessage("Order already fulfilled");
    		return dto;
    	}
         
    }

    /**
     * Marks an order as picked up by the customer.
     * @param orderId the order id
     * @return the updated order
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalArgumentException if order is not ready for pickup
     */
    @Override
    @Transactional
    public OrderDto pickupOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Guest orders have no customer account and cannot be picked up via this endpoint
        if (order.isGuestOrder() || order.getCustomer() == null) {
            throw new IllegalArgumentException("Guest orders cannot be picked up through this endpoint");
        }

        // Verify the authenticated customer owns this order
        User currentUser = getCurrentUser();
        if (!order.getCustomer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You are not authorized to pick up this order");
        }

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalArgumentException("Order is not ready for pickup");
        }

        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        logger.info("[{}, CUSTOMER, PICKUP_ORDER, {}]", currentUser.getUsername(), orderId);

        OrderDto resultDto = convertToDto(savedOrder);
        resultDto.setMessage("Order picked up successfully");
        return resultDto;
    }
    
    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.PICKED_UP || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        // If the order was already fulfilled (READY_FOR_PICKUP), inventory was deducted
        // at fulfillment time — restore those ingredients back into inventory.
        if (order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            List<Inventory> inventories = inventoryRepository.findAll();
            if (!inventories.isEmpty()) {
                Inventory inventory = inventories.get(0);
                InventoryUtils.restoreOrderItemsToInventory(inventory, order.getItems());
                inventoryRepository.save(inventory);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        String username = getCurrentUsername();
        logger.info("[{}, STAFF, CANCEL_ORDER, {}]", username, orderId);

        OrderDto resultDto = convertToDto(savedOrder);
        resultDto.setMessage("Order cancelled successfully");
        return resultDto;
    }
    
    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}