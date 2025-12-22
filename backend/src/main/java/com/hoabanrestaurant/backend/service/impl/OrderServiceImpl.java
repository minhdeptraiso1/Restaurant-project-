package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.AddOrderItemReq;
import com.hoabanrestaurant.backend.dto.request.ApplyVoucherReq;
import com.hoabanrestaurant.backend.dto.request.CreateOrderReq;
import com.hoabanrestaurant.backend.dto.request.PayOrderReq;
import com.hoabanrestaurant.backend.dto.request.UpdateCartItemReq;
import com.hoabanrestaurant.backend.dto.request.UpdateOrderStatusReq;
import com.hoabanrestaurant.backend.dto.response.OrderDto;
import com.hoabanrestaurant.backend.dto.response.OrderItemDto;
import com.hoabanrestaurant.backend.entity.LoyaltyAccount;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.entity.OrderItem;
import com.hoabanrestaurant.backend.entity.Payment;
import com.hoabanrestaurant.backend.entity.User;
import com.hoabanrestaurant.backend.entity.UserVoucher;
import com.hoabanrestaurant.backend.entity.Voucher;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.OrderTypes;
import com.hoabanrestaurant.backend.enums.PaymentMethod;
import com.hoabanrestaurant.backend.enums.PaymentStatus;
import com.hoabanrestaurant.backend.enums.VoucherStatus;
import com.hoabanrestaurant.backend.enums.VoucherType;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.ComboRepository;
import com.hoabanrestaurant.backend.repository.DishRepository;
import com.hoabanrestaurant.backend.repository.LoyaltyAccountRepository;
import com.hoabanrestaurant.backend.repository.OrderItemRepository;
import com.hoabanrestaurant.backend.repository.OrderRepository;
import com.hoabanrestaurant.backend.repository.PaymentRepository;
import com.hoabanrestaurant.backend.repository.RestaurantTableRepository;
import com.hoabanrestaurant.backend.repository.UserVoucherRepository;
import com.hoabanrestaurant.backend.repository.VoucherRepository;
import com.hoabanrestaurant.backend.service.EmailService;
import com.hoabanrestaurant.backend.service.OrderService;
import com.hoabanrestaurant.backend.util.QrSigner;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final PaymentRepository paymentRepo;
    private final DishRepository dishRepo;
    private final ComboRepository comboRepo;
    private final RestaurantTableRepository tableRepo;
    private final VoucherRepository voucherRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final EmailService emailService;
    private final QrSigner qrSigner;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // ví dụ 8%
    private static final long POINT_PER_VND = 1000; // 1 điểm / 1,000đ

    @Override
    @Transactional
    public OrderDto create(UUID userIdOrNull, CreateOrderReq req) {
        Order o = new Order();
        o.setId(UUID.randomUUID());
        o.setStatus(OrderStatus.OPEN);
        o.setTypes(OrderTypes.DINE_IN);
        o.setSubtotal(BigDecimal.ZERO);
        o.setDiscount(BigDecimal.ZERO);
        o.setTax(BigDecimal.ZERO);
        o.setTotal(BigDecimal.ZERO);
        o.setNote(req.note());


        if (req.tableId() != null) {
            var table = tableRepo.findById(req.tableId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Bàn không tồn tại"));
            var opening = orderRepo.findByTable_IdAndStatus(table.getId(), OrderStatus.OPEN);
            if (!opening.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "Bàn đang có order mở");
            o.setTable(table);
        }
        if (userIdOrNull != null) {
            var user = new User();
            user.setId(userIdOrNull); // hoặc load nếu bạn cần email
            o.setUser(user);
        }
        o = orderRepo.save(o);
        return toDto(o, List.of(), null);
    }

    @Override
    @Transactional
    public OrderDto addItem(UUID orderId, AddOrderItemReq req) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));
        if (o.getStatus() != OrderStatus.OPEN) throw new BusinessException(ErrorCode.BAD_REQUEST, "Order không còn mở");

        String type = req.itemType().toUpperCase();
        String name;
        BigDecimal unitPrice;
        if ("DISH".equals(type)) {
            var d = dishRepo.findById(req.itemId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Món không tồn tại"));
            name = d.getName();
            unitPrice = d.getPrice();
        } else if ("COMBO".equals(type)) {
            var c = comboRepo.findById(req.itemId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Combo không tồn tại"));
            name = c.getName();
            unitPrice = c.getPrice();
        } else throw new BusinessException(ErrorCode.BAD_REQUEST, "itemType chỉ hỗ trợ DISH/COMBO");

        BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(req.quantity()));
        OrderItem it = OrderItem.builder()
                .id(UUID.randomUUID()).order(o).itemType(type).itemId(req.itemId())
                .name(name).unitPrice(unitPrice).quantity(req.quantity()).lineTotal(line).build();
        itemRepo.save(it);

        recalcTotals(o);
        return toDto(o, itemRepo.findByOrder_Id(o.getId()), null);
    }

    @Override
    @Transactional
    public OrderDto applyVoucher(UUID orderId, ApplyVoucherReq req) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));
        if (o.getStatus() != OrderStatus.OPEN) throw new BusinessException(ErrorCode.BAD_REQUEST, "Order không còn mở");

        Voucher v = voucherRepo.findByCodeIgnoreCase(req.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));

        validateVoucherUsable(v, o.getSubtotal());

        BigDecimal discount = calcDiscount(v, o.getSubtotal());
        o.setDiscount(discount);

        recalcTotals(o);
        orderRepo.save(o);
        return toDto(o, itemRepo.findByOrder_Id(o.getId()), v.getCode());
    }

    @Override
    @Transactional
    public OrderDto pay(UUID orderId, PayOrderReq req) {

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));

        if (o.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order không còn mở");
        }

        // ================ CASE 1: COD = Thu tiền khi giao hàng ================
        if (req.method() == PaymentMethod.COD) {
            o.setStatus(OrderStatus.UNPAID); // chưa thanh toán

            // không tạo payment succeeded
            Payment p = Payment.builder()
                    .id(UUID.randomUUID())
                    .order(o)
                    .method(PaymentMethod.COD)
                    .amount(o.getTotal())
                    .status(PaymentStatus.PENDING)   // chờ thu tiền
                    .createdAt(Instant.now())
                    .build();
            paymentRepo.save(p);

            orderRepo.save(o);

            if (o.getUser() != null) {
                emailService.sendTemplate(
                        o.getUser().getEmail(),
                        "Đặt hàng thành công – Hoa Ban Restaurant",
                        "email/order_created_cod.html",
                        Map.of(
                                "fullName", o.getUser().getFullName(),
                                "orderId", o.getId().toString(),
                                "total", o.getTotal().toPlainString()
                        )
                );
            }


            return toDto(o, itemRepo.findByOrder_Id(o.getId()), null);
        }

        // ================ CASE 2: CASH tại quán (DINE-IN) =================
        if (req.method() == PaymentMethod.CASH) {

            if (req.amount().compareTo(o.getTotal()) < 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Số tiền thanh toán chưa đủ");
            }

            Payment p = Payment.builder()
                    .id(UUID.randomUUID())
                    .order(o)
                    .method(PaymentMethod.CASH)
                    .amount(req.amount())
                    .status(PaymentStatus.SUCCEEDED)
                    .paidAt(Instant.now())
                    .build();
            paymentRepo.save(p);

            confirmPaid(o);

            return toDto(o, itemRepo.findByOrder_Id(o.getId()), null);
        }

        // ================ CASE 3: VNPay = Gửi qua PaymentService ================
        if (req.method() == PaymentMethod.VNPAY) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Không hỗ trợ phương thức VNPAY vui lòng chọn lại hình thức thanh toán");
        }

        throw new BusinessException(ErrorCode.BAD_REQUEST, "Phương thức thanh toán không hợp lệ");
    }

    @Override
    @Transactional
    public void confirmPaid(Order order) {

        if (order.getStatus() == OrderStatus.PAID) {
            return; // tránh double-paid
        }

        order.setStatus(OrderStatus.PAID);

        // Redeem voucher
        if (order.getAppliedUserVoucherId() != null) {
            userVoucherRepo.findById(order.getAppliedUserVoucherId())
                    .ifPresent(uv -> {
                        if (!uv.isRedeemed()) {
                            uv.setRedeemed(true);
                            uv.setRedeemedAt(Instant.now());
                            userVoucherRepo.save(uv);
                        }
                    });
        }

        // Loyalty + email
        if (order.getUser() != null) {
            addLoyaltyPoints(order.getUser().getId(), order.getTotal());

            long earned = order.getTotal()
                    .divide(BigDecimal.valueOf(1000), 0, RoundingMode.FLOOR)
                    .longValue();

            emailService.sendTemplate(
                    order.getUser().getEmail(),
                    "Hóa đơn thanh toán – Hoa Ban",
                    "email/order_paid.html",
                    Map.of(
                            "fullName", order.getUser().getFullName(),
                            "orderId", order.getId().toString(),
                            "total", order.getTotal().toPlainString(),
                            "points", earned
                    )
            );
        }

        orderRepo.save(order);
    }


    // ===== Helpers =====


    private void recalcTotals(Order o) {
        var items = itemRepo.findByOrder_Id(o.getId());
        BigDecimal subtotal = items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.subtract(o.getDiscount()).multiply(TAX_RATE).max(BigDecimal.ZERO);
        BigDecimal total = subtotal.subtract(o.getDiscount()).add(tax).max(BigDecimal.ZERO);

        o.setSubtotal(subtotal);
        o.setTax(tax);
        o.setTotal(total);
        orderRepo.save(o);
    }

    private void validateVoucherUsable(Voucher v, BigDecimal subtotal) {
        var now = Instant.now();
        if (v.getStatus() != VoucherStatus.ACTIVE)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher không hoạt động");
        if (v.getStartAt() != null && now.isBefore(v.getStartAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher chưa bắt đầu");
        if (v.getEndAt() != null && now.isAfter(v.getEndAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher đã hết hạn");
        if (subtotal.compareTo(v.getMinOrder()) < 0)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Chưa đạt giá trị tối thiểu");
    }

    private BigDecimal calcDiscount(Voucher v, BigDecimal subtotal) {
        if (v.getType() == VoucherType.FIXED) {
            return v.getValue().min(subtotal);
        }
        // PERCENT
        BigDecimal disc = subtotal.multiply(v.getValue()).divide(BigDecimal.valueOf(100));
        if (v.getMaxDiscount() != null) disc = disc.min(v.getMaxDiscount());
        return disc;
    }

    private void addLoyaltyPoints(UUID userId, BigDecimal total) {
        long earn = total.divide(BigDecimal.valueOf(POINT_PER_VND), 0, RoundingMode.FLOOR).longValue(); // floor(total/1000)
        LoyaltyAccount acc = loyaltyRepo.findById(userId).orElseGet(() -> {
            LoyaltyAccount a = new LoyaltyAccount();
            a.setUserId(userId);
            a.setPoints(0);
            return a;
        });
        acc.setPoints(acc.getPoints() + earn);
        acc.setUpdatedAt(Instant.now());
        loyaltyRepo.save(acc);
    }

    private OrderDto toDto(Order o, List<OrderItem> items, String voucherCode) {
        List<OrderItemDto> itemDtos = items.stream().map(i ->
                new OrderItemDto(i.getId(), i.getItemType(), i.getItemId(), i.getName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal())
        ).toList();
        return new OrderDto(
                o.getId(),
                o.getUser() != null ? o.getUser().getId() : null,
                o.getTable() != null ? o.getTable().getId() : null,
                o.getTypes(),
                o.getStatus(),
                o.getSubtotal(), o.getDiscount(), o.getTax(), o.getTotal(),
                o.getNote(),
                itemDtos,
                voucherCode
        );
    }

    @Override
    @Transactional
    public OrderDto getOrCreateCart(UUID userId) {

        // Tìm giỏ DELIVERY đang OPEN
        var existing = orderRepo.findFirstByUser_IdAndStatusAndTypes(
                userId,
                OrderStatus.OPEN,
                OrderTypes.DELIVERY
        );

        if (existing.isPresent()) {
            var o = existing.get();
            return toDto(o, itemRepo.findByOrder_Id(o.getId()), null);
        }

        User u = new User();
        u.setId(userId);

        Order o = new Order();
        o.setId(UUID.randomUUID());
        o.setUser(u);
        o.setTypes(OrderTypes.DELIVERY);
        o.setStatus(OrderStatus.OPEN);
        o.setSubtotal(BigDecimal.ZERO);
        o.setDiscount(BigDecimal.ZERO);
        o.setTax(BigDecimal.ZERO);
        o.setTotal(BigDecimal.ZERO);
        o.setNote("Giỏ hàng giao tận nơi");
        o.setCreatedAt(Instant.now());
        o.setUpdatedAt(Instant.now());

        o = orderRepo.save(o);

        return toDto(o, List.of(), null);
    }

    @Override
    @Transactional
    public OrderDto addItemToCart(UUID userId, AddOrderItemReq req) {

        // Tìm giỏ hàng Delivery
        var cart = orderRepo.findFirstByUser_IdAndStatusAndTypes(
                userId,
                OrderStatus.OPEN,
                OrderTypes.DELIVERY
        ).orElseGet(() -> {
            // Nếu chưa có → tạo đúng DELIVERY CART
            var created = getOrCreateCart(userId);
            return orderRepo.findById(created.id()).orElseThrow();
        });

        return addItem(cart.getId(), req);
    }

    @Override
    @Transactional
    public OrderDto updateCartItem(UUID userId, UUID itemId, UpdateCartItemReq req) {

        var cart = orderRepo.findFirstByUser_IdAndStatusAndTypes(
                userId,
                OrderStatus.OPEN,
                OrderTypes.DELIVERY
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Chưa có giỏ hàng"));

        ensureOwnedBy(cart, userId);

        var item = itemRepo.findByIdAndOrder_Id(itemId, cart.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Mục không tồn tại trong giỏ"));

        int newQuantity = req.quantity();

        if (newQuantity <= 0) {
            itemRepo.delete(item);
        } else {
            item.setQuantity(newQuantity);
            item.setLineTotal(
                    item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity))
            );
            itemRepo.save(item);
        }

        recalcTotals(cart);

        return toDto(cart, itemRepo.findByOrder_Id(cart.getId()), null);
    }


    @Override
    @Transactional
    public OrderDto removeCartItem(UUID userId, UUID itemId) {

        var cart = orderRepo.findFirstByUser_IdAndStatusAndTypes(
                userId,
                OrderStatus.OPEN,
                OrderTypes.DELIVERY
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Chưa có giỏ hàng"));

        ensureOwnedBy(cart, userId);

        var item = itemRepo.findByIdAndOrder_Id(itemId, cart.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Mục không tồn tại trong giỏ"));

        itemRepo.delete(item);
        recalcTotals(cart);

        return toDto(cart, itemRepo.findByOrder_Id(cart.getId()), null);
    }


    // helper: đảm bảo giỏ thuộc về user
    private void ensureOwnedBy(Order o, UUID userId) {
        if (o.getUser() == null || !o.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Không có quyền với giỏ hàng");
        }
    }

    @Override
    @Transactional
    public OrderDto applyUserVoucher(UUID orderId, UUID userId, UUID userVoucherId) {

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));
        if (o.getStatus() != OrderStatus.OPEN) throw new BusinessException(ErrorCode.BAD_REQUEST, "Order không còn mở");

        // Order phải thuộc user (giỏ hàng) hoặc là order dine-in nhưng có user gắn (tuỳ bạn muốn hạn chế)
        if (o.getUser() == null || !o.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Không thể áp voucher cho order không thuộc bạn");
        }

        UserVoucher uv = userVoucherRepo.findByIdAndUser_IdAndRedeemedFalse(userVoucherId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher không khả dụng trong ví"));


        Voucher v = uv.getVoucher();
        validateVoucherUsable(v, o.getSubtotal());

        BigDecimal discount = calcDiscount(v, o.getSubtotal());
        o.setDiscount(discount);
        o.setAppliedUserVoucherId(uv.getId());
        userVoucherRepo.save(uv);
        recalcTotals(o);
        orderRepo.save(o);

        return toDto(o, itemRepo.findByOrder_Id(o.getId()), v.getCode());
    }

    @Override
    public List<OrderDto> getUserOrders(UUID userId) {
        List<Order> orders = orderRepo.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, OrderStatus.PAID);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = itemRepo.findByOrder_Id(order.getId());
                    return toDto(order, items, null);
                })
                .toList();
    }


    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepo.findAllByOrderByCreatedAtDesc();
        return orders.stream().map(order -> {
            List<OrderItem> items = itemRepo.findByOrder_Id(order.getId());
            return toDto(order, items, null);
        }).toList();
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));
        List<OrderItem> items = itemRepo.findByOrder_Id(orderId);
        return toDto(order, items, null);
    }


    @Override
    @Transactional
    public int cleanEmptyOrders() {
        List<Order> emptyOrders = orderRepo.findAll().stream()
                .filter(o ->
                        o.getStatus() == OrderStatus.OPEN &&   // chỉ xoá đơn trạng thái OPEN
                                (
                                        o.getTotal() == null ||
                                                o.getTotal().compareTo(BigDecimal.ZERO) == 0 ||
                                                itemRepo.findByOrder_Id(o.getId()).isEmpty()
                                )
                )
                .toList();

        emptyOrders.forEach(order -> {
            itemRepo.deleteAllByOrder_Id(order.getId());
            orderRepo.delete(order);
        });

        return emptyOrders.size();
    }


    @Override
    @Transactional
    public OrderDto openByQr(UUID userIdOrNull, String qrCode, String note) {
        // 1) Verify QR
        var decoded = qrSigner.verify(qrCode);
        var now = Instant.now();
        if (decoded.exp() != null && now.isAfter(decoded.exp())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "QR đã hết hạn");
        }
        UUID tableId = decoded.tableId();

        // 2) Load bàn + khóa bi quan
        var table = tableRepo.findByIdForUpdate(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Bàn không tồn tại"));

        // 3) THAY ĐỔI LOGIC: Tìm order đang OPEN
        var existingOrder = orderRepo.findByTable_IdAndStatus(tableId, OrderStatus.OPEN);

        if (!existingOrder.isEmpty()) { // Kiểm tra List không rỗng
            // Lấy phần tử đầu tiên trong danh sách
            Order o = existingOrder.get(0);
            return toDto(o, List.of(), null);
        }

        // 4) Nếu chưa có thì mới tạo order mới (Logic cũ)
        Order o = new Order();
        o.setId(UUID.randomUUID());
        o.setTypes(OrderTypes.DINE_IN);
        o.setStatus(OrderStatus.OPEN);
        o.setSubtotal(BigDecimal.ZERO);
        o.setDiscount(BigDecimal.ZERO);
        o.setTax(BigDecimal.ZERO);
        o.setTotal(BigDecimal.ZERO);
        o.setNote(note);
        o.setTable(table);

        if (userIdOrNull != null) {
            var user = new User();
            user.setId(userIdOrNull);
            o.setUser(user);
        }

        o = orderRepo.save(o);
        return toDto(o, List.of(), null);
    }


    @Override
    @Transactional
    public OrderDto updateStatus(UUID orderId, UpdateOrderStatusReq req, Jwt jwt) {

        // ===== Lấy userId từ JWT =====
        UUID actorUserId = null;
        if (jwt != null && jwt.getClaimAsString("uid") != null) {
            actorUserId = UUID.fromString(jwt.getClaimAsString("uid"));
        }

        // ===== Lấy roles =====
        boolean isStaffOrAdmin = false;
        List<String> roles = jwt != null ? jwt.getClaimAsStringList("roles") : null;
        if (roles != null) {
            isStaffOrAdmin = roles.stream()
                    .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("STAFF"));
        }

        // ===== Order =====
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));

        OrderStatus current = o.getStatus();
        OrderStatus target = req.status();

        boolean isOwner = (o.getUser() != null
                && actorUserId != null
                && o.getUser().getId().equals(actorUserId));

        // ===== Nếu đã PAID => không cho đổi =====
        if (current == OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Đơn đã thanh toán không thể đổi trạng thái");
        }

        /*
         * ============================================
         *              FLOW DINE-IN
         * ============================================
         */
        if (o.getTypes() == OrderTypes.DINE_IN) {

            // CUSTOMER không được update DINE-IN
            if (!isStaffOrAdmin) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không thể cập nhật đơn tại bàn");
            }

            switch (target) {

                case CANCELLED -> {
                    if (current != OrderStatus.OPEN)
                        throw new BusinessException(ErrorCode.BAD_REQUEST, "Chỉ hủy đơn đang mở");

                    o.setAppliedUserVoucherId(null);
                    o.setStatus(OrderStatus.CANCELLED);
                }

                case OPEN -> {
                    if (current != OrderStatus.CANCELLED)
                        throw new BusinessException(ErrorCode.BAD_REQUEST, "Chỉ mở lại đơn đã hủy");

                    o.setStatus(OrderStatus.OPEN);
                }

                default -> throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "Trạng thái không hợp lệ cho DINE-IN");
            }
        } else {

            if (!isStaffOrAdmin) {

                if (!isOwner) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "Không thể cập nhật đơn không thuộc bạn");
                }

                // Customer chỉ được OPEN → UNPAID
                if (current == OrderStatus.OPEN && target == OrderStatus.UNPAID) {
                    o.setStatus(OrderStatus.UNPAID);
                } else {
                    throw new BusinessException(ErrorCode.FORBIDDEN,
                            "Khách chỉ được chuyển đơn giao hàng từ OPEN → UNPAID");
                }
            } else {

                switch (target) {

                    case CANCELLED -> {
                        if (current != OrderStatus.OPEN && current != OrderStatus.UNPAID)
                            throw new BusinessException(ErrorCode.BAD_REQUEST,
                                    "Chỉ hủy đơn OPEN hoặc UNPAID");

                        o.setAppliedUserVoucherId(null);
                        o.setStatus(OrderStatus.CANCELLED);
                    }

                    case OPEN -> {
                        if (current != OrderStatus.CANCELLED && current != OrderStatus.UNPAID)
                            throw new BusinessException(ErrorCode.BAD_REQUEST,
                                    "Chỉ mở lại đơn CANCELLED hoặc UNPAID");

                        o.setStatus(OrderStatus.OPEN);
                    }

                    case UNPAID -> {
                        o.setStatus(OrderStatus.UNPAID);
                    }

                    case PAID -> {
                        if (current != OrderStatus.UNPAID)
                            throw new BusinessException(ErrorCode.BAD_REQUEST,
                                    "Chỉ xác nhận COD khi đơn đang UNPAID");

                        confirmPaid(o);
                    }

                    default -> throw new BusinessException(ErrorCode.BAD_REQUEST,
                            "Trạng thái không hợp lệ cho DELIVERY");
                }
            }
        }

        o.setUpdatedAt(Instant.now());
        orderRepo.save(o);

        return toDto(o, itemRepo.findByOrder_Id(o.getId()), null);
    }

    @Override
    public Map<String, Long> getOrderStatsToday() {

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<Object[]> stats = orderRepo.countByStatusBetween(start, end);

        Map<String, Long> result = new HashMap<>();

        for (Object[] row : stats) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            result.put(status.name(), count);
        }

        return result;
    }


}
