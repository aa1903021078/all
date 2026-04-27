package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.aop.LogOp;
import com.dahaiwuliang.common.BizException;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import com.dahaiwuliang.service.ReferralService;
import com.dahaiwuliang.service.ScheduleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;
    private final SysUserMapper userMapper;
    private final OperationLogMapper logMapper;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleService scheduleService;
    private final ReferralService referralService;
    private final PasswordEncoder encoder;

    // Hospitals
    @GetMapping("/hospitals")
    public R<List<Hospital>> hospitals() {
        return R.ok(hospitalMapper.selectList(Wrappers.<Hospital>lambdaQuery().orderByAsc(Hospital::getId)));
    }

    @PostMapping("/hospitals")
    @LogOp(action = "新增机构", target = "hospital")
    public R<Hospital> createHospital(@RequestBody Hospital h) {
        h.setId(null); hospitalMapper.insert(h); return R.ok(h);
    }

    @PutMapping("/hospitals/{id}")
    @LogOp(action = "修改机构", target = "hospital")
    public R<?> updateHospital(@PathVariable Long id, @RequestBody Hospital h) {
        h.setId(id); hospitalMapper.updateById(h); return R.ok();
    }

    @DeleteMapping("/hospitals/{id}")
    @LogOp(action = "删除机构", target = "hospital")
    public R<?> deleteHospital(@PathVariable Long id) { hospitalMapper.deleteById(id); return R.ok(); }

    // Departments
    @GetMapping("/departments")
    public R<List<Department>> departments(@RequestParam(required = false) Long hospitalId) {
        return R.ok(deptMapper.selectList(Wrappers.<Department>lambdaQuery()
                .eq(hospitalId != null, Department::getHospitalId, hospitalId)
                .orderByAsc(Department::getId)));
    }

    @PostMapping("/departments")
    @LogOp(action = "新增科室", target = "department")
    public R<Department> createDept(@RequestBody Department d) {
        d.setId(null); deptMapper.insert(d); return R.ok(d);
    }

    @PutMapping("/departments/{id}")
    @LogOp(action = "修改科室", target = "department")
    public R<?> updateDept(@PathVariable Long id, @RequestBody Department d) {
        d.setId(id); deptMapper.updateById(d); return R.ok();
    }

    @DeleteMapping("/departments/{id}")
    @LogOp(action = "删除科室", target = "department")
    public R<?> deleteDept(@PathVariable Long id) { deptMapper.deleteById(id); return R.ok(); }

    // Users
    @GetMapping("/users")
    public R<List<SysUser>> users(@RequestParam(required = false) String role,
                                  @RequestParam(required = false) String keyword) {
        List<SysUser> list = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .eq(role != null && !role.isEmpty(), SysUser::getRole, role)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getRealName, keyword)
                        .or().like(SysUser::getPhone, keyword))
                .orderByAsc(SysUser::getId));
        list.forEach(u -> u.setPassword(null));
        return R.ok(list);
    }

    @PostMapping("/users")
    @LogOp(action = "新增用户", target = "user")
    public R<SysUser> createUser(@RequestBody SysUser u) {
        if (u.getPassword() == null || u.getPassword().length() < 6)
            throw new BizException(400, "初始密码至少 6 位");
        Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, u.getUsername()));
        if (exists != null && exists > 0) throw new BizException(400, "用户名已存在");
        u.setId(null);
        u.setPassword(encoder.encode(u.getPassword()));
        if (u.getEnabled() == null) u.setEnabled(1);
        if (u.getRole() == null) u.setRole("PATIENT");
        userMapper.insert(u);
        u.setPassword(null);
        return R.ok(u);
    }

    @PutMapping("/users/{id}")
    @LogOp(action = "修改用户", target = "user")
    public R<?> updateUser(@PathVariable Long id, @RequestBody SysUser u) {
        u.setId(id); u.setPassword(null);
        userMapper.updateById(u);
        return R.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    @LogOp(action = "重置密码", target = "user")
    public R<?> resetPwd(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String pwd = body.get("password");
        if (pwd == null || pwd.length() < 6) throw new BizException(400, "密码至少 6 位");
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "用户不存在");
        u.setPassword(encoder.encode(pwd));
        userMapper.updateById(u);
        return R.ok();
    }

    @PostMapping("/users/{id}/toggle")
    @LogOp(action = "启用/禁用账号", target = "user")
    public R<?> toggleUser(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "用户不存在");
        u.setEnabled(u.getEnabled() != null && u.getEnabled() == 1 ? 0 : 1);
        userMapper.updateById(u);
        return R.ok();
    }

    // Schedules (admin overrides)
    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules() { return R.ok(scheduleService.listAll()); }

    @PutMapping("/schedules/{id}")
    @LogOp(action = "调整排班", target = "schedule")
    public R<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule s) {
        return R.ok(scheduleService.update(id, s));
    }

    @PostMapping("/schedules/{id}/close")
    @LogOp(action = "停诊（管理员）", target = "schedule")
    public R<?> close(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        scheduleService.close(id, body == null ? null : body.get("reason"));
        return R.ok();
    }

    // Referrals supervision
    @GetMapping("/referrals")
    public R<List<Map<String, Object>>> referrals(@RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long hospitalId) {
        return R.ok(referralService.listAll(status, hospitalId));
    }

    @PostMapping("/referrals/{id}/status")
    @LogOp(action = "管理员干预转诊", target = "referral")
    public R<Referral> setStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(referralService.updateStatus(id, body.get("status")));
    }

    // Logs
    @GetMapping("/logs")
    public R<List<OperationLog>> logs(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "200") int limit) {
        return R.ok(logMapper.selectList(Wrappers.<OperationLog>lambdaQuery()
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(OperationLog::getAction, keyword)
                        .or().like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getTarget, keyword))
                .orderByDesc(OperationLog::getId)
                .last("limit " + Math.max(1, Math.min(limit, 1000)))));
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("hospitals", hospitalMapper.selectCount(Wrappers.emptyWrapper()));
        m.put("departments", deptMapper.selectCount(Wrappers.emptyWrapper()));
        m.put("doctors", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "DOCTOR")));
        m.put("patients", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "PATIENT")));
        m.put("schedules", scheduleMapper.selectCount(Wrappers.emptyWrapper()));
        return R.ok(m);
    }
}
