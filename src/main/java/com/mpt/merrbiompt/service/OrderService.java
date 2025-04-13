package com.mpt.merrbiompt.service;

import com.mpt.merrbiompt.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;


    @Transactional
    public void removeOrderUserByUserId(Long userId) {
        orderRepository.deleteByUserId(userId);
    }
}
