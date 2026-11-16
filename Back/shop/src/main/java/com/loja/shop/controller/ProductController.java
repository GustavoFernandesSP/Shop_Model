package com.loja.shop.controller;

import com.loja.shop.model.Product;
import com.loja.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository products;

    // o front espera um ARRAY direto (data.map(...))
    @GetMapping("/get-products")
    public List<Product> getProducts() {
        return products.findAll();
    }
}
