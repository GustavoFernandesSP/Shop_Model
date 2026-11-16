package com.loja.shop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class Requests {

    // CONFIRMAR: campos do formulário de cadastro/login do front
    public record Register(String name, String username, String email, String password) {}
    public record Login(String email, String password) {}

    public record CartItem(@JsonProperty("_id") Long id, Integer quantity) {}

    // Nomes iguais ao payload enviado por products.js (confirmOrder)
    public record PlaceOrder(
            List<CartItem> items,
            Integer totalItemCount,
            String delivery_type,
            BigDecimal delivery_type_cost,
            BigDecimal cost_before_delivery_rate,
            BigDecimal cost_after_delivery_rate,
            String promo_code,
            String contact_number,
            Long user_id) {}

    public record CancelOrder(Long order_id) {}
}
