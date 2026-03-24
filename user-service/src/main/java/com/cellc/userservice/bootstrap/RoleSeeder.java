package com.cellc.userservice.bootstrap;

import com.cellc.userservice.entity.Role;
import com.cellc.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        ensureRoleExists("USER");
        ensureRoleExists("ADMIN");
    }

    private void ensureRoleExists(String nombre) {
        roleRepository.findByNombre(nombre).orElseGet(() -> {
            Role role = new Role();
            role.setNombre(nombre);
            return roleRepository.save(role);
        });
    }
}

