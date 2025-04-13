package com.mpt.merrbiompt.repository;

import com.mpt.merrbiompt.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "select b from Product b where b.product_name = :name")
    Product findByName(@Param("name") String name);

    List<Product> searchByCategory(String category);

    @Query(value = "select p from Product p where p.product_name like %:keyword% or p.category like %:keyword%")
    List<Product> searchByNameOrCategory(@Param("keyword") String keyword);

    @Query(value = "select b from Product b")
    List<Product> findAllProducts();

    @Query(value = "select b from Product b where b.category = 'Perime'")
    List<Product> findProductsByCategoryPerime();

    @Query(value = "select b from Product b where b.category = 'Fruta'")
    List<Product> findProductsByCategoryFruta();

    Optional<Product> findById(Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.authorized = :authorized WHERE p.productId = :productId")
    void updateAuthorizationStatus(@Param("productId") Long productId,
                                   @Param("authorized") String authorized);
}
