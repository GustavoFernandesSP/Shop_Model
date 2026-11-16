package com.loja.shop.config;

import com.loja.shop.model.Product;
import com.loja.shop.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class ProductSeeder {
    @Bean
    CommandLineRunner seed(ProductRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            for (int i = 1; i <= 6; i++) {
                Product p = new Product();
                p.setName("Produto " + i);
                p.setDescription("Descrição do produto " + i);
                p.setPrice(new BigDecimal("19.90").multiply(BigDecimal.valueOf(i)));
                p.setImage("https://picsum.photos/seed/loja" + i + "/400/400");
                p.setCategory("geral");
                repo.save(p);
            }
        };
    }
}
