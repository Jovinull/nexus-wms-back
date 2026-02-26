package br.com.nexus.nexus_wms.modules.iam.infra.service;

import br.com.nexus.nexus_wms.modules.iam.api.dto.LoginRequestDTO;
import br.com.nexus.nexus_wms.modules.iam.api.dto.LoginResponseDTO;
import br.com.nexus.nexus_wms.modules.iam.domain.entity.RefreshToken;
import br.com.nexus.nexus_wms.modules.iam.domain.entity.User;
import br.com.nexus.nexus_wms.modules.iam.domain.repository.RefreshTokenRepository;
import br.com.nexus.nexus_wms.core.security.UserDetailsImpl;
import br.com.nexus.nexus_wms.core.exception.BusinessException;
import br.com.nexus.nexus_wms.core.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
            RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (userDetails == null) {
            throw new BusinessException("Ocorreu um erro: Detalhes do usuário não encontrados.");
        }

        User user = userDetails.getUser();
        String accessToken = generateAccessToken(authentication, user);
        String refreshToken = generateAndSaveRefreshToken(user);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .expiresIn(3600L) // 1 hora
                .name(user.getFullName())
                .role(user.getRole())
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponseDTO refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token não encontrado ou inválido"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())
                || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token expirado ou revogado");
        }

        User user = refreshToken.getUser();
        String newAccessToken = generateAccessTokenForUser(user);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .expiresIn(3600L)
                .name(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private String generateAccessToken(Authentication authentication, User user) {
        Instant now = Instant.now();
        long expiry = 3600L;

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexus-wms")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(user.getEmail())
                .claim("scope", scope)
                .claim("userId", user.getId())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateAccessTokenForUser(User user) {
        Instant now = Instant.now();
        long expiry = 3600L;
        String scope = "SCOPE_" + user.getRole().name();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexus-wms")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(user.getEmail())
                .claim("scope", scope)
                .claim("userId", user.getId())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateAndSaveRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
