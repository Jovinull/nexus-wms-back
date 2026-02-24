package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.auth.LoginRequestDTO;
import br.com.nexus.nexus_wms.application.dto.auth.LoginResponseDTO;
import br.com.nexus.nexus_wms.infrastructure.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final JwtEncoder jwtEncoder;

        public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
                this.authenticationManager = authenticationManager;
                this.jwtEncoder = jwtEncoder;
        }

        public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(),
                                                loginRequestDTO.getPassword()));

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                if (userDetails == null) {
                        throw new IllegalStateException("Ocorreu um erro: Detalhes do usuário não encontrados.");
                }
                Instant now = Instant.now();
                long expiry = 3600L; // 1 hora de expiração

                String scope = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(" "));

                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("nexus-wms")
                                .issuedAt(now)
                                .expiresAt(now.plusSeconds(expiry))
                                .subject(userDetails.getUsername())
                                .claim("scope", scope)
                                .claim("userId", userDetails.getUser().getId())
                                .build();

                String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

                return LoginResponseDTO.builder()
                                .accessToken(token)
                                .expiresIn(expiry)
                                .name(userDetails.getUser().getFullName())
                                .role(userDetails.getUser().getRole())
                                .build();
        }
}
