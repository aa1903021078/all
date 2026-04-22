package com.yuequge.util;

/**
 * 存取当前登录用户信息的 ThreadLocal。
 */
public class UserContext {

    public record CurrentUser(Long userId, String username, String role) {
        public boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(role);
        }
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser u) {
        HOLDER.set(u);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
