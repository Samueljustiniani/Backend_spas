package com.spa.backend.service.impl;

import com.spa.backend.dto.SaleRequest;
import com.spa.backend.dto.SaleResponse;
import com.spa.backend.model.Product;
import com.spa.backend.model.Sale;
import com.spa.backend.model.SaleDetail;
import com.spa.backend.model.User;
import com.spa.backend.repository.ProductRepository;
import com.spa.backend.repository.SaleRepository;
import com.spa.backend.repository.UserRepository;
import com.spa.backend.service.SaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public SaleServiceImpl(SaleRepository saleRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream()
                .filter(s -> "A".equals(s.getStatus()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SaleResponse> findById(Long id) {
        return saleRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional
    public SaleResponse create(SaleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setSaleDate(OffsetDateTime.now());
        sale.setPaymentType(request.getPaymentType());
        sale.setStatus("A");

        List<SaleDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SaleRequest.SaleDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findById(detailReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detailReq.getProductId()));

            if (product.getStock() < detailReq.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setQuantity(detailReq.getQuantity());
            detail.setUnitPrice(product.getPrice());
            detail.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(detailReq.getQuantity())));
            detail.setSale(sale);
            details.add(detail);

            total = total.add(detail.getSubtotal());

            // Actualizar stock
            product.setStock(product.getStock() - detailReq.getQuantity());
            productRepository.save(product);
        }

        sale.setDetails(details);
        sale.setTotal(total);

        Sale saved = saleRepository.save(sale);
        return toResponse(saved);
    }

    @Override
    public SaleResponse updateStatus(Long id, String status) {
        return saleRepository.findById(id).map(sale -> {
            sale.setStatus(status);
            return toResponse(saleRepository.save(sale));
        }).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        saleRepository.findById(id).ifPresent(sale -> {
            sale.setStatus("I");
            saleRepository.save(sale);
        });
    }

    @Override
    public List<SaleResponse> findByUserId(Long userId) {
        return saleRepository.findByUserIdAndStatus(userId, "A").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleResponse.SaleDetailResponse> detailResponses = new ArrayList<>();
        if (sale.getDetails() != null) {
            detailResponses = sale.getDetails().stream()
                    .map(d -> SaleResponse.SaleDetailResponse.builder()
                            .id(d.getId())
                            .productId(d.getProduct().getId())
                            .productName(d.getProduct().getName())
                            .quantity(d.getQuantity())
                            .unitPrice(d.getUnitPrice())
                            .subtotal(d.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return SaleResponse.builder()
                .id(sale.getId())
                .saleDate(sale.getSaleDate())
                .total(sale.getTotal())
                .paymentType(sale.getPaymentType())
                .status(sale.getStatus())
                .userId(sale.getUser().getId())
                .userName(sale.getUser().getName() + " " + (sale.getUser().getLastname() != null ? sale.getUser().getLastname() : ""))
                .details(detailResponses)
                .build();
    }
}
