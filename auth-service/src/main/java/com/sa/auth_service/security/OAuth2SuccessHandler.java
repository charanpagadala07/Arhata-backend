package com.sa.auth_service.security;

import com.sa.auth_service.entity.User;
import com.sa.auth_service.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String frontendUrl;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${shortlistai.frontend-url:http://localhost:5173}")
            String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User newUser = new User();

                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setPassword(null);
                    newUser.setAuthProvider("GOOGLE");

                    return userRepository.save(newUser);
                });

        String token = jwtService.generateToken(user.getEmail());

        response.sendRedirect(
                frontendUrl.replaceAll("/+$", "")
                        + "/auth/callback#token="
                        + token
        );
    }
}