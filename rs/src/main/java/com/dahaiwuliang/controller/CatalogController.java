package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.Department;
import com.dahaiwuliang.entity.Hospital;
import com.dahaiwuliang.entity.Schedule;
import com.dahaiwuliang.mapper.DepartmentMapper;
import com.dahaiwuliang.mapper.HospitalMapper;
import com.dahaiwuliang.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Public catalog endpoints (any logged-in user).
 */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;
    private final ScheduleService scheduleService;

    @GetMapping("/hospitals")
    public R<List<Hospital>> hospitals() {
        return R.ok(hospitalMapper.selectList(Wrappers.<Hospital>lambdaQuery()
                .orderByAsc(Hospital::getId)));
    }

    @GetMapping("/departments")
    public R<List<Department>> departments(@RequestParam(required = false) Long hospitalId) {
        return R.ok(deptMapper.selectList(Wrappers.<Department>lambdaQuery()
                .eq(hospitalId != null, Department::getHospitalId, hospitalId)
                .orderByAsc(Department::getId)));
    }

    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return R.ok(scheduleService.listForBooking(hospitalId, deptId, from));
    }
}
