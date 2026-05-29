package edu.ncsu.csc326.wolfcafe.service.impl;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemRequestDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderPlaceDto;
import edu.ncsu.csc326.wolfcafe.entity.*;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private TaxRateService taxRateService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customer;
    private User otherCustomer;
    private Item item1;
    private Item item2;
    private Inventory inventory;
    private Order order;

    @BeforeEach
    public void setUp() {
        // Default: authenticated customer context
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testcustomer",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Primary test customer
        customer = new User();
        customer.setId(1L);
        customer.setUsername("testcustomer");
        customer.setEmail("customer@test.com");
        customer.setName("Test Customer");

        // A second customer — used to verify ownership enforcement
        otherCustomer = new User();
        otherCustomer.setId(2L);
        otherCustomer.setUsername("othercustomer");
        otherCustomer.setEmail("other@test.com");
        otherCustomer.setName("Other Customer");

        lenient().doReturn(Optional.of(customer)).when(userRepository).findByUsername("testcustomer");
        lenient().doReturn(Optional.of(otherCustomer)).when(userRepository).findByUsername("othercustomer");

        // item1 has one ingredient: COFFEE (requires 2 units per item)
        Ingredient coffeeIngredient = new Ingredient();
        coffeeIngredient.setName("COFFEE");
        coffeeIngredient.setAmount(2);

        item1 = new Item();
        item1.setId(1L);
        item1.setName("Coffee");
        item1.setPrice(5.00);
        item1.setDescription("Hot coffee");
        item1.setIngredients(Arrays.asList(coffeeIngredient));

        // item2 has one ingredient: PASTRY (requires 1 unit per item)
        Ingredient pastryIngredient = new Ingredient();
        pastryIngredient.setName("PASTRY");
        pastryIngredient.setAmount(1);

        item2 = new Item();
        item2.setId(2L);
        item2.setName("Pastry");
        item2.setPrice(3.00);
        item2.setDescription("Fresh pastry");
        item2.setIngredients(Arrays.asList(pastryIngredient));

        // Inventory has plenty of COFFEE and PASTRY
        inventory = new Inventory();
        inventory.setId(1L);
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("COFFEE", 50);
        ingredients.put("PASTRY", 30);
        inventory.setIngredients(ingredients);

        // Default order owned by `customer`, contains one Coffee item (quantity 1)
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setItem(item1);
        orderItem1.setQuantity(1);
        orderItem1.setPrice(5.00);

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setGuestOrder(false);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(5.00);
        order.setTax(0.10);
        order.setTip(0.75);
        order.setTotal(5.85);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>(Arrays.asList(orderItem1)));

        lenient().when(orderRepository.save(any(Order.class))).thenReturn(order);
        lenient().when(inventoryRepository.findAll()).thenReturn(Arrays.asList(inventory));
        lenient().when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
    }

    // ──────────────────────────────────────────────
    // Security context helpers
    // ──────────────────────────────────────────────

    private void setGuestSecurityContext() {
        SecurityContext ctx = mock(SecurityContext.class);
        AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
                "guest-key", "anonymousUser",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        lenient().when(ctx.getAuthentication()).thenReturn(anon);
        SecurityContextHolder.setContext(ctx);
    }

    private void setStaffSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "staffuser", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private void setAdminSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "adminuser", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    /** Sets a security context for a customer other than the order owner. */
    private void setOtherCustomerSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "othercustomer", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ──────────────────────────────────────────────
    // placeOrder
    // ──────────────────────────────────────────────

    @Test
    public void testPlaceOrder_Success() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 2)));
        dto.setTipType("PERCENTAGE");
        dto.setTipValue(15);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.placeOrder(dto);

        assertNotNull(result);
        assertEquals(5.00, result.getSubtotal());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(itemRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        // Inventory must NOT be touched at order placement
        verify(inventoryRepository, never()).findAll();
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testPlaceOrder_EmptyOrder() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Order must contain at least one item", ex.getMessage());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_NullItems() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Order must contain at least one item", ex.getMessage());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_NegativeTip() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("CUSTOM");
        dto.setTipValue(-5.00);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Tip amount cannot be negative", ex.getMessage());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_ItemNotFound() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(99L, 1)));
        dto.setTipType("NONE");

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Item not found", ex.getMessage());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_CustomTip() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("CUSTOM");
        dto.setTipValue(2.50);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        assertNotNull(orderService.placeOrder(dto));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_PercentageTip() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 2)));
        dto.setTipType("PERCENTAGE");
        dto.setTipValue(20);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        assertNotNull(orderService.placeOrder(dto));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_NoTip() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("NONE");
        dto.setTipValue(0);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        assertNotNull(orderService.placeOrder(dto));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_MultipleItems() {
        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(
                new OrderItemRequestDto(1L, 1),
                new OrderItemRequestDto(2L, 2)));
        dto.setTipType("PERCENTAGE");
        dto.setTipValue(15);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = captor.getValue();
            o.setId(1L);
            return o;
        });

        OrderDto result = orderService.placeOrder(dto);

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        verify(orderRepository, times(1)).save(any(Order.class));
        // Inventory still must NOT be touched at order placement
        verify(inventoryRepository, never()).findAll();
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testPlaceOrder_StaffBlocked() {
        setStaffSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("NONE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Only customers or guests can place orders", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(itemRepository, never()).findById(any());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_AdminBlocked() {
        setAdminSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("NONE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Only customers or guests can place orders", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    // ──────────────────────────────────────────────
    // placeOrder — guest flows
    // ──────────────────────────────────────────────

    @Test
    public void testPlaceOrder_GuestSuccess() {
        setGuestSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("NONE");
        dto.setTipValue(0);

        Order guestOrder = new Order();
        guestOrder.setId(2L);
        guestOrder.setGuestOrder(true);
        guestOrder.setCustomer(null);
        guestOrder.setStatus(OrderStatus.PENDING);
        guestOrder.setSubtotal(5.00);
        guestOrder.setTax(0.10);
        guestOrder.setTip(0.00);
        guestOrder.setTotal(5.10);
        guestOrder.setCreatedAt(LocalDateTime.now());
        guestOrder.setItems(new ArrayList<>());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(guestOrder);

        OrderDto result = orderService.placeOrder(dto);

        assertNotNull(result);
        assertEquals("Guest", result.getCustomerName());
        assertEquals("Order placed successfully", result.getMessage());
        verify(userRepository, never()).findByUsername(anyString());
        verify(orderRepository, times(1)).save(any(Order.class));
        // Inventory must NOT be touched at order placement
        verify(inventoryRepository, never()).findAll();
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testPlaceOrder_GuestWithPercentageTip() {
        setGuestSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 2)));
        dto.setTipType("PERCENTAGE");
        dto.setTipValue(15);

        Order guestOrder = new Order();
        guestOrder.setId(3L);
        guestOrder.setGuestOrder(true);
        guestOrder.setCustomer(null);
        guestOrder.setStatus(OrderStatus.PENDING);
        guestOrder.setSubtotal(10.00);
        guestOrder.setTax(0.20);
        guestOrder.setTip(1.50);
        guestOrder.setTotal(11.70);
        guestOrder.setCreatedAt(LocalDateTime.now());
        guestOrder.setItems(new ArrayList<>());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));
        when(orderRepository.save(any(Order.class))).thenReturn(guestOrder);

        OrderDto result = orderService.placeOrder(dto);

        assertNotNull(result);
        assertEquals("Guest", result.getCustomerName());
        verify(userRepository, never()).findByUsername(anyString());
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_GuestEmptyOrder() {
        setGuestSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Order must contain at least one item", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_GuestNegativeTip() {
        setGuestSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("CUSTOM");
        dto.setTipValue(-3.00);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(dto));
        assertEquals("Tip amount cannot be negative", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testPlaceOrder_GuestOrderNotLinkedToUser() {
        setGuestSecurityContext();

        OrderPlaceDto dto = new OrderPlaceDto();
        dto.setItems(Arrays.asList(new OrderItemRequestDto(1L, 1)));
        dto.setTipType("NONE");
        dto.setTipValue(0);

        Order guestOrder = new Order();
        guestOrder.setId(4L);
        guestOrder.setGuestOrder(true);
        guestOrder.setCustomer(null);
        guestOrder.setStatus(OrderStatus.PENDING);
        guestOrder.setSubtotal(5.00);
        guestOrder.setTax(0.10);
        guestOrder.setTip(0.00);
        guestOrder.setTotal(5.10);
        guestOrder.setCreatedAt(LocalDateTime.now());
        guestOrder.setItems(new ArrayList<>());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(taxRateService.getTaxRate()).thenReturn(
                new edu.ncsu.csc326.wolfcafe.dto.TaxRateDto(1L, 2.0));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenReturn(guestOrder);

        orderService.placeOrder(dto);

        Order saved = captor.getValue();
        assertNull(saved.getCustomer());
        assertTrue(saved.isGuestOrder());
        verify(inventoryRepository, never()).findAll();
    }

    // ──────────────────────────────────────────────
    // getPendingOrders
    // ──────────────────────────────────────────────

    @Test
    public void testGetPendingOrders_Success() {
        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(Arrays.asList(order));

        List<OrderDto> result = orderService.getPendingOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testcustomer", result.get(0).getCustomerName());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    @Test
    public void testGetPendingOrders_Empty() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());

        List<OrderDto> result = orderService.getPendingOrders();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    // ──────────────────────────────────────────────
    // getOrder
    // ──────────────────────────────────────────────

    @Test
    public void testGetOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.getOrder(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testcustomer", result.getCustomerName());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetOrder_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrder(99L));
        assertEquals("Order not found", ex.getMessage());
    }

    // ──────────────────────────────────────────────
    // getMyOrders
    // ──────────────────────────────────────────────

    @Test
    public void testGetMyOrders_Success() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(order));

        List<OrderDto> result = orderService.getMyOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testcustomer", result.get(0).getCustomerName());
        verify(orderRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    public void testGetMyOrders_Empty() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(new ArrayList<>());

        List<OrderDto> result = orderService.getMyOrders();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(orderRepository, times(1)).findByCustomerId(1L);
    }

    // ──────────────────────────────────────────────
    // fulfillOrder
    // ──────────────────────────────────────────────

    @Test
    public void testFulfillOrder_Success() {
        // order has 1x Coffee (requires 2 COFFEE); inventory has 50 COFFEE — should succeed
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.fulfillOrder(1L);

        assertNotNull(result);
        assertEquals("Order fulfilled successfully", result.getMessage());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        // Inventory must be checked and saved at fulfillment
        verify(inventoryRepository, times(1)).findAll();
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    public void testFulfillOrder_DeductsInventory() {
        // Verify the actual inventory amounts are reduced after fulfillment
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        when(inventoryRepository.save(inventoryCaptor.capture())).thenReturn(inventory);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.fulfillOrder(1L);

        // order has 1x Coffee item, each requiring 2 COFFEE; expect 50 - 2 = 48
        Inventory saved = inventoryCaptor.getValue();
        assertEquals(48, saved.getIngredients().get("COFFEE"));
    }

    @Test
    public void testFulfillOrder_InsufficientInventory() {
        // Set inventory to have only 1 COFFEE, but order requires 2 — should throw
        inventory.getIngredients().put("COFFEE", 1);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.fulfillOrder(1L));
        assertEquals("Insufficient inventory to fulfill this order", ex.getMessage());
        // Order must NOT be saved, inventory must NOT be deducted
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    /**
     * Fix #4: fulfillOrder must now throw when no inventory record exists,
     * rather than silently skipping the check and fulfilling for free.
     */
    @Test
    public void testFulfillOrder_EmptyInventory_Throws() {
        when(inventoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.fulfillOrder(1L));
        assertEquals("No inventory exists. Cannot fulfill order without inventory.", ex.getMessage());
        // Order must NOT be saved when inventory is absent
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    public void testFulfillOrder_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.fulfillOrder(99L));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    public void testFulfillOrder_NotPending_ReturnsInformationalMessage() {
        // Already READY_FOR_PICKUP — should return informational message, not throw
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.fulfillOrder(1L);

        assertNotNull(result);
        assertEquals("READY_FOR_PICKUP", result.getStatus());
        assertEquals("Order already fulfilled", result.getMessage());
        // Inventory must NOT be checked or modified for a non-pending order
        verify(inventoryRepository, never()).findAll();
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testFulfillOrder_MultipleItems_DeductsAll() {
        // Order with 2x Coffee (requires 2 COFFEE each = 4 total) and 3x Pastry (1 PASTRY each = 3 total)
        OrderItem coffeeOrderItem = new OrderItem();
        coffeeOrderItem.setItem(item1);
        coffeeOrderItem.setQuantity(2);
        coffeeOrderItem.setPrice(5.00);

        OrderItem pastryOrderItem = new OrderItem();
        pastryOrderItem.setItem(item2);
        pastryOrderItem.setQuantity(3);
        pastryOrderItem.setPrice(3.00);

        order.setItems(Arrays.asList(coffeeOrderItem, pastryOrderItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        when(inventoryRepository.save(inventoryCaptor.capture())).thenReturn(inventory);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.fulfillOrder(1L);

        Inventory saved = inventoryCaptor.getValue();
        // 50 COFFEE - (2 per item * 2 items) = 46
        assertEquals(46, saved.getIngredients().get("COFFEE"));
        // 30 PASTRY - (1 per item * 3 items) = 27
        assertEquals(27, saved.getIngredients().get("PASTRY"));
    }

    // ──────────────────────────────────────────────
    // cancelOrder
    // ──────────────────────────────────────────────

    @Test
    public void testCancelOrder_Success_PendingOrder() {
        // Staff cancels a PENDING order — inventory was never deducted so no restore needed
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals("Order cancelled successfully", result.getMessage());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        // PENDING cancel must NOT touch inventory (nothing was deducted yet)
        verify(inventoryRepository, never()).findAll();
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    /**
     * Fix #1: Cancelling a READY_FOR_PICKUP order must restore inventory since
     * ingredients were already deducted at fulfillment time.
     */
    @Test
    public void testCancelOrder_ReadyForPickup_RestoresInventory() {
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals("Order cancelled successfully", result.getMessage());
        // Inventory must be fetched and saved to restore the deducted ingredients
        verify(inventoryRepository, times(1)).findAll();
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    /**
     * Verifies the actual ingredient amounts are restored when a READY_FOR_PICKUP
     * order is cancelled.
     */
    @Test
    public void testCancelOrder_ReadyForPickup_CorrectAmountsRestored() {
        // order contains 1x Coffee item, each requiring 2 COFFEE
        // simulate inventory already deducted: 50 - 2 = 48
        inventory.getIngredients().put("COFFEE", 48);

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        when(inventoryRepository.save(inventoryCaptor.capture())).thenReturn(inventory);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L);

        Inventory saved = inventoryCaptor.getValue();
        // 48 + 2 (1 item * 2 COFFEE each) = 50
        assertEquals(50, saved.getIngredients().get("COFFEE"));
    }

    @Test
    public void testCancelOrder_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder(99L));
        assertEquals("Order not found", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testCancelOrder_AlreadyPickedUp() {
        // PICKED_UP orders cannot be cancelled
        order.setStatus(OrderStatus.PICKED_UP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L));
        assertTrue(ex.getMessage().contains("cannot be cancelled"));
        assertTrue(ex.getMessage().contains("PICKED_UP"));
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testCancelOrder_AlreadyCancelled() {
        // Already CANCELLED orders cannot be cancelled again
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L));
        assertTrue(ex.getMessage().contains("cannot be cancelled"));
        assertTrue(ex.getMessage().contains("CANCELLED"));
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryRepository, never()).findAll();
    }

    @Test
    public void testCancelOrder_SetsTimestamp() {
        // Verify cancelledAt timestamp is written when cancelling a PENDING order
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenReturn(order);

        orderService.cancelOrder(1L);

        Order saved = captor.getValue();
        assertEquals(OrderStatus.CANCELLED, saved.getStatus());
        assertNotNull(saved.getCancelledAt());
    }

    /**
     * Verify cancelledAt timestamp is also written when cancelling a
     * READY_FOR_PICKUP order (the restore path).
     */
    @Test
    public void testCancelOrder_ReadyForPickup_SetsTimestamp() {
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenReturn(order);

        orderService.cancelOrder(1L);

        Order saved = captor.getValue();
        assertEquals(OrderStatus.CANCELLED, saved.getStatus());
        assertNotNull(saved.getCancelledAt());
    }

    // ──────────────────────────────────────────────
    // pickupOrder — ownership enforcement
    // ──────────────────────────────────────────────

    @Test
    public void testPickupOrder_Success_OwnerPicksUp() {
        // The authenticated customer owns this order — should succeed
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.pickupOrder(1L);

        assertNotNull(result);
        assertEquals("Order picked up successfully", result.getMessage());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testPickupOrder_WrongCustomer_Rejected() {
        // A different authenticated customer attempts to pick up someone else's order
        setOtherCustomerSecurityContext();

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        // order.getCustomer() is `customer` (id=1), but current user is `otherCustomer` (id=2)
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.pickupOrder(1L));
        assertEquals("You are not authorized to pick up this order", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testPickupOrder_GuestOrder_Rejected() {
        // Guest orders have no customer account and cannot be picked up
        Order guestOrder = new Order();
        guestOrder.setId(5L);
        guestOrder.setGuestOrder(true);
        guestOrder.setCustomer(null);
        guestOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
        guestOrder.setCreatedAt(LocalDateTime.now());
        guestOrder.setItems(new ArrayList<>());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(guestOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.pickupOrder(5L));
        assertEquals("Guest orders cannot be picked up through this endpoint", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testPickupOrder_NotReady() {
        // Order is PENDING — cannot be picked up even by the owner
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.pickupOrder(1L));
        assertEquals("Order is not ready for pickup", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testPickupOrder_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderService.pickupOrder(99L));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    public void testPickupOrder_SetsTimestamp() {
        // Verify pickedUpAt timestamp is written on successful pickup
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenReturn(order);

        orderService.pickupOrder(1L);

        Order saved = captor.getValue();
        assertEquals(OrderStatus.PICKED_UP, saved.getStatus());
        assertNotNull(saved.getPickedUpAt());
    }

    @Test
    public void testPickupOrder_CancelledOrder_Rejected() {
        // A cancelled order cannot be picked up
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.pickupOrder(1L));
        assertEquals("Order is not ready for pickup", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ──────────────────────────────────────────────
    // getAllOrders
    // ──────────────────────────────────────────────

    @Test
    public void testGetAllOrders_Success() {
        Order fulfilledOrder = new Order();
        fulfilledOrder.setId(2L);
        fulfilledOrder.setCustomer(customer);
        fulfilledOrder.setGuestOrder(false);
        fulfilledOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
        fulfilledOrder.setSubtotal(5.00);
        fulfilledOrder.setTax(0.10);
        fulfilledOrder.setTip(0.00);
        fulfilledOrder.setTotal(5.10);
        fulfilledOrder.setCreatedAt(LocalDateTime.now());
        fulfilledOrder.setItems(new ArrayList<>());

        Order cancelledOrder = new Order();
        cancelledOrder.setId(3L);
        cancelledOrder.setCustomer(otherCustomer);
        cancelledOrder.setGuestOrder(false);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.setSubtotal(3.00);
        cancelledOrder.setTax(0.06);
        cancelledOrder.setTip(0.00);
        cancelledOrder.setTotal(3.06);
        cancelledOrder.setCreatedAt(LocalDateTime.now());
        cancelledOrder.setItems(new ArrayList<>());

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, fulfilledOrder, cancelledOrder));

        List<OrderDto> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllOrders_Empty() {
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        List<OrderDto> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllOrders_ContainsAllStatuses() {
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setCustomer(customer);
        pendingOrder.setGuestOrder(false);
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setSubtotal(5.00);
        pendingOrder.setTax(0.10);
        pendingOrder.setTip(0.00);
        pendingOrder.setTotal(5.10);
        pendingOrder.setCreatedAt(LocalDateTime.now());
        pendingOrder.setItems(new ArrayList<>());

        Order readyOrder = new Order();
        readyOrder.setId(2L);
        readyOrder.setCustomer(customer);
        readyOrder.setGuestOrder(false);
        readyOrder.setStatus(OrderStatus.READY_FOR_PICKUP);
        readyOrder.setSubtotal(5.00);
        readyOrder.setTax(0.10);
        readyOrder.setTip(0.00);
        readyOrder.setTotal(5.10);
        readyOrder.setCreatedAt(LocalDateTime.now());
        readyOrder.setItems(new ArrayList<>());

        Order pickedUpOrder = new Order();
        pickedUpOrder.setId(3L);
        pickedUpOrder.setCustomer(customer);
        pickedUpOrder.setGuestOrder(false);
        pickedUpOrder.setStatus(OrderStatus.PICKED_UP);
        pickedUpOrder.setSubtotal(5.00);
        pickedUpOrder.setTax(0.10);
        pickedUpOrder.setTip(0.00);
        pickedUpOrder.setTotal(5.10);
        pickedUpOrder.setCreatedAt(LocalDateTime.now());
        pickedUpOrder.setItems(new ArrayList<>());

        Order cancelledOrder = new Order();
        cancelledOrder.setId(4L);
        cancelledOrder.setCustomer(otherCustomer);
        cancelledOrder.setGuestOrder(false);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.setSubtotal(5.00);
        cancelledOrder.setTax(0.10);
        cancelledOrder.setTip(0.00);
        cancelledOrder.setTotal(5.10);
        cancelledOrder.setCreatedAt(LocalDateTime.now());
        cancelledOrder.setItems(new ArrayList<>());

        when(orderRepository.findAll()).thenReturn(
                Arrays.asList(pendingOrder, readyOrder, pickedUpOrder, cancelledOrder));

        List<OrderDto> result = orderService.getAllOrders();

        assertEquals(4, result.size());
        List<String> statuses = result.stream()
                .map(OrderDto::getStatus)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(statuses.contains("PENDING"));
        assertTrue(statuses.contains("READY_FOR_PICKUP"));
        assertTrue(statuses.contains("PICKED_UP"));
        assertTrue(statuses.contains("CANCELLED"));
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllOrders_IncludesGuestOrders() {
        Order guestOrder = new Order();
        guestOrder.setId(5L);
        guestOrder.setGuestOrder(true);
        guestOrder.setCustomer(null);
        guestOrder.setStatus(OrderStatus.PENDING);
        guestOrder.setSubtotal(5.00);
        guestOrder.setTax(0.10);
        guestOrder.setTip(0.00);
        guestOrder.setTotal(5.10);
        guestOrder.setCreatedAt(LocalDateTime.now());
        guestOrder.setItems(new ArrayList<>());

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, guestOrder));

        List<OrderDto> result = orderService.getAllOrders();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> "Guest".equals(o.getCustomerName())));
        assertTrue(result.stream().anyMatch(o -> "testcustomer".equals(o.getCustomerName())));
        verify(orderRepository, times(1)).findAll();
    }
}