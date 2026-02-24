package br.com.nexus.nexus_wms.infrastructure.config;

import br.com.nexus.nexus_wms.domain.entity.iam.User;
import br.com.nexus.nexus_wms.domain.enums.UserRole;
import br.com.nexus.nexus_wms.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Value("${nexus.seeder.admin.password:NexusAdmin@123!}")
    private String adminPassword;

    @Value("${nexus.seeder.operador.password:NexusOperador@123!}")
    private String operadorPassword;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@nexus.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@nexus.com");
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setFullName("Administrador do Sistema");
                admin.setRole(UserRole.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("operador@nexus.com").isEmpty()) {
                User oper = new User();
                oper.setEmail("operador@nexus.com");
                oper.setPasswordHash(passwordEncoder.encode(operadorPassword));
                oper.setFullName("Operador de Estoque");
                oper.setRole(UserRole.OPERADOR_ESTOQUE);
                oper.setActive(true);
                userRepository.save(oper);
            }
        };
    }
}
