package com.cuscatlan.reserva.config;

import com.cuscatlan.reserva.entity.Role;
import com.cuscatlan.reserva.entity.User;
import com.cuscatlan.reserva.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeedRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminSeedProperties properties;

    public AdminSeedRunner(
            UserRepository userRepository, PasswordEncoder passwordEncoder, AdminSeedProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled() || userRepository.existsByEmail(properties.getEmail())) {
            return;
        }

        User admin = User.builder()
                .email(properties.getEmail())
                .password(passwordEncoder.encode(properties.getPassword()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
