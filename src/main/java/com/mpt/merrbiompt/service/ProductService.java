package com.mpt.merrbiompt.service;

import com.mpt.merrbiompt.entity.Product;
import com.mpt.merrbiompt.repository.OrderRepository;
import com.mpt.merrbiompt.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void addProduct(Product product) {
        product.setAuthorized("Unauthorized");
        productRepository.save(product);
    }

    public void updateProduct(Long id, Product updatedProduct) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setProduct_name(updatedProduct.getProduct_name());
            product.setCategory(updatedProduct.getCategory());
            product.setProduct_description(updatedProduct.getProduct_description());
            product.setQuantity(updatedProduct.getQuantity());
            product.setPrice(updatedProduct.getPrice());
            product.setImagePath(updatedProduct.getImagePath()); // Update the image path
            productRepository.save(product);
        }
    }

    public void authorize(Long id, Product updatedAuthorized) {
        Optional<Product> authorizeProduct = productRepository.findById(id);
        if (authorizeProduct.isPresent()) {
            Product product = authorizeProduct.get();
            product.setAuthorized(updatedAuthorized.getAuthorized());
            productRepository.save(product);
        }
    }

    public void deleteProduct(Long id) {
        orderRepository.deleteById(id);
        productRepository.deleteById(id);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> searchProductByNameOrCategory(String keyword) {
        return productRepository.searchByNameOrCategory(keyword);
    }

    public List<Product> getProductByCategoryPerime() {
        return productRepository.findProductsByCategoryPerime();
    }

    public List<Product> getProductByCategoryFruta() {
        return productRepository.findProductsByCategoryFruta();
    }

    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    @Transactional
    public void updateProductAuthorization(Long productId, boolean isAuthorized) {
        // Convert boolean to consistent string representation
        String authStatus = isAuthorized ? "Authorized" : "Unauthorized"; // or "true"/"false", match your DB values
        productRepository.updateAuthorizationStatus(productId, authStatus);
    }
}