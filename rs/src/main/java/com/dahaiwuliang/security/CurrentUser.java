package com.dahaiwuliang.security;

import com.dahaiwuliang.security.JwtUtil.TokenInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {
    public static TokenInfo get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof TokenInfo)) return null;
        return (TokenInfo) a.getPrincipal();
    }

    public static Long id() {
        TokenInfo t = get();
        return t == null ? null : t.getUserId();
    }

    public static String role() {
        TokenInfo t = get();
        return t == null ? null : t.getRole();
    }
}
