package br.com.nexus.nexus_wms.modules.iam.infra.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "refresh_token";

    public ResponseCookie createRefreshTokenCookie(String token, long duration) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true) // Should be true in production (HTTPS)
                .path("/api/auth") // Limit cookie access to auth endpoints
                .maxAge(duration)
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}
