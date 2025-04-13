package com.mpt.merrbiompt.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpt.merrbiompt.entity.Product;
import com.mpt.merrbiompt.entity.User;
import com.mpt.merrbiompt.repository.ProductRepository;
import com.mpt.merrbiompt.service.ProductService;
import com.mpt.merrbiompt.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return userService.getUserById(userId);
        }
        return null;
    }

    @GetMapping
    public String getAllProducts(Model model, HttpSession session) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        return "Products/ProductPage";
    }

    @GetMapping("/add")
    public String goToAddProducts(Model model, HttpSession session) {
        model.addAttribute("products", new Product());

        // Add current user to model
        User currentUser = getCurrentUser(session);
        model.addAttribute("currentUser", currentUser);

        return "Products/create_product";
    }


    //shtohet nga fermeri
    @PostMapping("/add")
    public String addProduct(
            @RequestParam("product_name") String name,
            @RequestParam("category") String category,
            @RequestParam("product_description") String description,
            @RequestParam("quantity") int quantity,
            @RequestParam("price") double price,
            @RequestParam("image") MultipartFile imageFile,
            Model model, RedirectAttributes redirectAttributes) throws IOException {

        Product product = new Product();
        product.setProduct_name(name);
        product.setCategory(category);
        product.setProduct_description(description);
        product.setQuantity(quantity);
        product.setPrice(price);

        if (productService.getProductByName(name) != null && product.getProduct_name().equals(productService.getProductByName(name).getProduct_name())) {
            model.addAttribute("errorMessage", "This book already exists in the library");
            return "Products/create_product";
        }
        if (!imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get("src/main/resources/static/images/" + fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            product.setImagePath("/images/" + fileName);
        }
        productService.addProduct(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product added successfully");
        return "redirect:/products/add";
    }

    @GetMapping("/update/{id}")
    public String goToUpdate(Model model, @PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
        } else {
            model.addAttribute("errorMessage", "Product not found");
        }
        return "Products/update_product";
    }

    //perditesohet nga fermeri
    @PostMapping("/update/{id}")
    public String updateProduct(
            @ModelAttribute("product") Product product,
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) throws IOException {

        Optional<Product> existingProduct = productService.getProductById(id);
        if (existingProduct.isPresent()) {
            Product currentProduct = existingProduct.get();
            if (!product.getProduct_name().equals(currentProduct.getProduct_name())) {
                Product productWithSameName = productService.getProductByName(product.getProduct_name());
                if (productWithSameName != null && !(productWithSameName.getProductId() == id)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "A product with this name already exists.");
                    return "redirect:/products/update/" + id;
                }
            }
            if (!imageFile.isEmpty()) {
                if (currentProduct.getImagePath() != null && !currentProduct.getImagePath().isEmpty()) {
                    Path oldFilePath = Paths.get("src/main/resources/static" + currentProduct.getImagePath());
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                }

                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path newFilePath = Paths.get("src/main/resources/static/images/" + fileName);
                Files.copy(imageFile.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImagePath("/images/" + fileName);
            } else {
                product.setImagePath(currentProduct.getImagePath());
            }
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
        }
        return "redirect:/products";
    }

    @GetMapping("/authorize/{id}")
    public String showAuthorizationForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", product);
        return "Products/authorize";
    }

    @PostMapping("/authorize/{id}")
    public String authorizeProduct(
            @PathVariable("id") Long id,
            @RequestParam(name = "authorized", defaultValue = "false") boolean authorized,
            RedirectAttributes redirectAttributes) {

        try {
            productService.updateProductAuthorization(id, authorized);
            redirectAttributes.addFlashAttribute("success", "Authorization updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update authorization");
        }

        return "redirect:/products";
    }
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) throws IOException {
        Product product = productService.getProductById(id).get();

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            Path filePath = Paths.get("src/main/resources/static" + product.getImagePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @GetMapping("/search")
    public String goToSearch(@RequestParam("keyword") String keyword, Model model) {
        List<Product> products = productService.searchProductByNameOrCategory(keyword);
        if (products.isEmpty()) {
            model.addAttribute("errorMessageBookEmpty", "No product or category found with the given keyword");
        } else {
            model.addAttribute("products", products);
        }
        model.addAttribute("keyword", keyword);
        return "Products/ProductPage";
    }
}
