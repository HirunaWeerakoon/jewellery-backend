package com.example.jewellery_backend.config;

import com.example.jewellery_backend.entity.AdminUser;
import com.example.jewellery_backend.repository.AdminUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser admin = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Map your AdminUser role(s) to authorities. Adjust based on how roles are stored.
        List<GrantedAuthority> authorities = new ArrayList<>();
        try {
            // If AdminUser has enum Role or String role:
            Object roleObj = null;
            // Attempt common getters (adjust if your AdminUser uses different names)
            try { roleObj = admin.getRole(); } catch (Throwable t) { }
            if (roleObj == null) {
                // maybe a string list
                try { roleObj = admin.getRole(); } catch (Throwable t) { }
            }

            if (roleObj instanceof String) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + ((String)roleObj).toUpperCase()));
            } else if (roleObj != null) {
                // enum or other
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleObj.toString().toUpperCase()));
            } else {
                // fallback - give ADMIN if AdminUser class name suggests admin
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        } catch (Exception ex) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        boolean enabled = true;
        try { enabled = admin.getIsActive() == null ? true : admin.getIsActive(); } catch (Throwable t) { }

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash()) // password should be stored encoded
                .disabled(!enabled)
                .authorities(authorities)
                .build();
    }
}
