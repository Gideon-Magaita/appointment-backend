package com.magaita.appointment.security;


import com.magaita.appointment.entity.User;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()->new NotFoundException("Email not found!"));

        return AuthUser.builder()
                .user(user)
                .build();
    }
}
