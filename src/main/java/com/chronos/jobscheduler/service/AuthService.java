package com.chronos.jobscheduler.service;

import com.chronos.jobscheduler.model.User;
import com.chronos.jobscheduler.repository.UserRepository;
import com.chronos.jobscheduler.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Register a new user
    public User register(Map<String, String> body) {
        String username = body.get("username");
        String password = encoder.encode(body.get("password"));
        String email = body.get("email");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("USER");

        return userRepository.save(user);
    }

    // Login and return JWT token
    public Map<String, String> login(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && encoder.matches(password, userOpt.get().getPassword())) {
            String token = jwtUtil.generateToken(username);
            return Map.of("token", token);
        }
        return Map.of("error", "invalid_credentials");
    }
}
