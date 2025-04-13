package com.mpt.merrbiompt.repository;

import com.mpt.merrbiompt.entity.Product;
import com.mpt.merrbiompt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    @Query(value="select u from User u where u.username like %:keyword%")
    List<User> searchByUsername(String keyword);

    User getUserByUsername(String username);

    @Query("SELECT u FROM User u JOIN FETCH u.basket WHERE u.userId = :userId")
    Optional<User> findByIdWithBasket(@Param("userId") Long userId);

        @Query(value = "select u from User u where u.role = 'Admin'")
    User findByRole();
}
