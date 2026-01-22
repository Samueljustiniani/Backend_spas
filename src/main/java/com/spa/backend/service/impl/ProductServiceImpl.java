package com.spa.backend.service.impl;

import com.spa.backend.model.Product;
import com.spa.backend.repository.ProductRepository;
import com.spa.backend.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    public ProductServiceImpl(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Product> findAll() { return repo.findAll(); }

    @Override
    public Optional<Product> findById(Long id) { return repo.findById(id); }

    @Override
    public Product save(Product product) { return repo.save(product); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
