package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.service.UserActionService;
import com.yuequge.util.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通用行为埋点接口（前端手动上报，如商品浏览、音乐播放等）。
 */
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionTrackController {

    private final UserActionService userActionService;

    @PostMapping
    public Result<Void> track(@RequestBody ActionDTO dto) {
        var u = UserContext.get();
        Long uid = u == null ? null : u.userId();
        userActionService.track(uid, dto.getTargetType(), dto.getTargetId(), dto.getAction(), dto.getExtra());
        return Result.ok();
    }

    @Data
    public static class ActionDTO {
        private String targetType;
        private Long targetId;
        private String action;
        private String extra;
    }
}
