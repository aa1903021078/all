package com.dahaiwuliang.common;

/**
 * 当前登录用户上下文
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    /** 获取当前用户ID, 未登录抛出异常 */
    public static Long requireUserId() {
        Long id = getUserId();
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        return id;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
