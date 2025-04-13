package com.mpt.merrbiompt.controller;

import com.mpt.merrbiompt.entity.Order;
import com.mpt.merrbiompt.entity.Product;
import com.mpt.merrbiompt.entity.User;
import com.mpt.merrbiompt.repository.OrderRepository;
import com.mpt.merrbiompt.repository.ProductRepository;
import com.mpt.merrbiompt.service.ProductService;
import com.mpt.merrbiompt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("main")
public class MainPageController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/MainPage")
    public String getMainPage(HttpSession session, Model model, HttpServletRequest request) {
        model.addAttribute("requestPath", request.getRequestURI());
        User user = (User) session.getAttribute("user");
        if (user != null) {
            User userWithBasket = userService.getUserWithBasket(user.getUserId());
            model.addAttribute("user", userWithBasket);
        } else {
            model.addAttribute("user", null);
        }

        List<Product> products = productService.getAllProducts();
        List<Product> perimeProducts = productService.getProductByCategoryPerime();
        List<Product> frutaProducts = productService.getProductByCategoryFruta();

        model.addAttribute("products", products);
        model.addAttribute("frutaProducts", frutaProducts);
        model.addAttribute("perimeProducts", perimeProducts);
        return "MainPage/MainPage";
    }

    @GetMapping("/details/{id}")
    public String getProductDetails(@PathVariable Long id, Model model, HttpSession httpSession) {
        Optional<Product> productOptional = productService.getProductById(id);
        User user = (User) httpSession.getAttribute("user");
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            model.addAttribute("product", product);
            model.addAttribute("user", user);
            return "MainPage/product_detail";
        } else {
            return "redirect:/main/MainPage";
        }
    }

    @PostMapping("/purchase/{productId}")
    @ResponseBody
    public ResponseEntity<?> purchaseProduct(
            @PathVariable Long productId,
            @RequestParam double quantity) {

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();

        // Convert quantity to long if needed
        long quantityToDeduct = (long) quantity;

        if (product.getQuantity() < quantityToDeduct) {
            return ResponseEntity.badRequest().body("Not enough quantity available");
        }

        product.setQuantity(product.getQuantity() - quantityToDeduct);
        productRepository.save(product);

        Map<String, Object> response = new HashMap<>();
        response.put("remainingQuantity", product.getQuantity());
        response.put("message", "Purchase successful!");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-purchase/{productId}")
    @ResponseBody
    public ResponseEntity<?> requestPurchase(
            @PathVariable Long productId,
            @RequestParam double quantity,
            HttpSession session) {

        User customer = (User) session.getAttribute("user");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login first");
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();

        // Create new order
        Order order = new Order();
        order.setProduct(product);
        order.setCustomer(customer);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice() * quantity);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Purchase request sent to farmer for approval");
        response.put("orderId", order.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve-order/{orderId}")
    @ResponseBody
    public ResponseEntity<?> approveOrder(
            @PathVariable Long orderId,
            @RequestParam boolean approve,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (!"Farmer".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only farmers can approve orders");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (approve) {
            Product product = order.getProduct();
            if (product.getQuantity() < order.getQuantity()) {
                order.setStatus("REJECTED");
                orderRepository.save(order);
                return ResponseEntity.badRequest().body("Not enough stock");
            }
            product.setQuantity((long) (product.getQuantity() - order.getQuantity()));
            order.setStatus("APPROVED");
        } else {
            order.setStatus("REJECTED");
        }

        orderRepository.save(order);
        return ResponseEntity.ok(Map.of(
                "message", "Order " + (approve ? "approved" : "rejected"),
                "status", order.getStatus()
        ));
    }

    @GetMapping("/orders/pending")
    public String getPendingOrders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && "Farmer".equals(user.getRole())) {
            List<Order> pendingOrders = orderRepository.findByStatus("PENDING");
            model.addAttribute("orders", pendingOrders);
            return "MainPage/pending_orders";
        }
        return "redirect:/main/MainPage";
    }

    @GetMapping("/orders/history")
    public String getOrderHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            List<Order> orders = "Farmer".equals(user.getRole())
                    ? orderRepository.findAll()
                    : orderRepository.findByCustomer(user);
            model.addAttribute("orders", orders);
            return "MainPage/order_history"; // Create this Thymeleaf template
        }
        return "redirect:/main/MainPage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/main/MainPage";
    }

    @GetMapping("/user_profile")
    public String getUserProfile(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user == null){
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "MainPage/user_profile";
    }

    @GetMapping("/searchMainPage")
    public String goToSearchMain(@RequestParam("keyword") String keyword, Model model) {
        List<Product> products = productService.searchProductByNameOrCategory(keyword);
        if (products.isEmpty()) {
            model.addAttribute("errorMessageBookEmpty", "No name or category found with the given keyword");
        } else {
            model.addAttribute("products", products);
        }
        model.addAttribute("keyword", keyword);
        return "MainPage/MainPage"; //
    }
}
