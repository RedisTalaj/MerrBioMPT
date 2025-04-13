package com.mpt.merrbiompt.repository;

import com.mpt.merrbiompt.entity.Order;
import com.mpt.merrbiompt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findByCustomer(User customer);


    @Modifying
    @Query("DELETE FROM Order o WHERE o.customer.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
