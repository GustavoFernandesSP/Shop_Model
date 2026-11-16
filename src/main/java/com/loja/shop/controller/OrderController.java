package com.loja.shop.controller;

import com.loja.shop.dto.Requests;
import com.loja.shop.model.*;
import com.loja.shop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    @PostMapping("/place-order")
    @Transactional
    public ResponseEntity<Map<String, Object>> placeOrder(@AuthenticationPrincipal Long userId,
                                                          @RequestBody Requests.PlaceOrder req) {
        AppUser user = users.findById(userId).orElse(null);
        if (user == null) return ApiResponse.error(401, "Unauthorized");
        if (req.items() == null || req.items().isEmpty()) return ApiResponse.error(400, "Carrinho vazio");

        CustomerOrder o = new CustomerOrder();
        o.setUser(user);   // usuário vem do token, não do user_id do payload
        o.setDeliveryType(req.delivery_type());
        o.setPromoCode(req.promo_code());
        o.setContactNumber(req.contact_number());

        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        for (Requests.CartItem ci : req.items()) {
            Product p = products.findById(ci.id()).orElse(null);
            if (p == null) return ApiResponse.error(400, "Produto não encontrado: " + ci.id());
            int qty = ci.quantity() == null ? 1 : Math.max(1, ci.quantity());

            OrderItem it = new OrderItem();
            it.setOrder(o);
            it.setProductId(p.getId());
            it.setName(p.getName());
            it.setDescription(p.getDescription());
            it.setProductImage(p.getImage());
            it.setPrice(p.getPrice());   // preço SEMPRE do banco, nunca do cliente
            it.setQuantity(qty);
            o.getItems().add(it);

            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
            count += qty;
        }
        // TODO: calcular o frete no servidor (hoje confia no valor enviado pelo front)
        BigDecimal delivery = req.delivery_type_cost() == null ? BigDecimal.ZERO : req.delivery_type_cost();
        o.setDeliveryTypeCost(delivery);
        o.setTotalItemCount(count);
        o.setCostBeforeDelivery(subtotal);
        o.setCostAfterDelivery(subtotal.add(delivery));
        orders.save(o);

        return ResponseEntity.ok(Map.of("message", "Pedido realizado com sucesso!", "order_id", o.getId()));
    }

    @GetMapping("/get-orders/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getOrders(@AuthenticationPrincipal Long me,
                                                         @PathVariable Long userId) {
        if (!me.equals(userId)) return ApiResponse.error(403, "Forbidden");
        List<Map<String, Object>> list = orders.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(Map.of("orders", list));
    }

    @PostMapping("/cancel-order")
    @Transactional
    public ResponseEntity<Map<String, Object>> cancelOrder(@AuthenticationPrincipal Long me,
                                                           @RequestBody Requests.CancelOrder req) {
        CustomerOrder o = req.order_id() == null ? null : orders.findById(req.order_id()).orElse(null);
        if (o == null || !o.getUser().getId().equals(me)) return ApiResponse.error(404, "Pedido não encontrado");
        if (!"pending".equals(o.getStatus())) return ApiResponse.error(400, "Este pedido não pode mais ser cancelado");
        o.setStatus("cancelled");
        return ResponseEntity.ok(Map.of("message", "Pedido cancelado", "user_id", me));
    }

    private Map<String, Object> toDto(CustomerOrder o) {
        String status = o.getStatus();
        int days = "Express".equalsIgnoreCase(o.getDeliveryType()) ? 3 : 7;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", String.valueOf(o.getId()));   // o front usa _id.slice(...)
        m.put("user_id", o.getUser().getId());
        m.put("status", status);
        m.put("createdAt", o.getCreatedAt().toString());
        m.put("expected_delivery_date", o.getCreatedAt().plus(days, ChronoUnit.DAYS).toString());
        m.put("percentage_complete", percentage(status));
        m.put("order_cancelled", "cancelled".equals(status));
        m.put("order_processed", "delivered".equals(status));
        m.put("totalItemCount", o.getTotalItemCount());
        m.put("delivery_type", o.getDeliveryType());
        m.put("delivery_type_cost", o.getDeliveryTypeCost());
        m.put("cost_before_delivery_rate", o.getCostBeforeDelivery());
        m.put("cost_after_delivery_rate", o.getCostAfterDelivery());
        m.put("promo_code", o.getPromoCode());
        m.put("contact_number", o.getContactNumber());
        m.put("items", o.getItems().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("_id", i.getProductId());
            im.put("name", i.getName());
            im.put("description", i.getDescription());
            im.put("product_image", i.getProductImage());
            im.put("price", i.getPrice());
            im.put("quantity", i.getQuantity());
            return im;
        }).toList());
        return m;
    }

    // a barra de progresso e as faixas do front dependem destes valores
    private int percentage(String status) {
        return switch (String.valueOf(status)) {
            case "paid" -> 40;
            case "processing" -> 60;
            case "shipped" -> 75;
            case "delivered" -> 100;
            case "cancelled" -> 0;
            default -> 10;   // pending
        };
    }
}
