package com.mpt.merrbiompt.service;

import com.mpt.merrbiompt.entity.User;
import com.mpt.merrbiompt.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public void addUser(User user){
        user.setRole("User");
        user.setStatus("Active");
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserWithBasket(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public boolean getUserByEmailAndPassword(String email, String password){
        User user = getUserByEmail(email);
        return user != null && user.getPassword().equals(password);
    }

    public void createUser(User user){
        user.setStatus("Active");
        userRepository.save(user);
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElse(null);
    }

    public void updateUser(Long userId, User updateUser){
        User user = getUserById(userId);
        user.setUsername(updateUser.getUsername());
        user.setStatus(updateUser.getStatus());
        userRepository.save(user);
    }

    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }

    public List<User> searchByUsername(String keyword){
        return userRepository.searchByUsername(keyword);
    }

    public User getUserByRole(){
        return userRepository.findByRole();
    }
}
