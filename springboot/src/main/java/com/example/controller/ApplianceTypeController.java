package com.example.controller;

import com.example.common.Result;
import com.example.entity.ApplianceType;
import com.example.service.ApplianceTypeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 家电品类前端操作接口
 */
@RestController
@RequestMapping("/applianceType")
public class ApplianceTypeController {

    @Resource
    private ApplianceTypeService applianceTypeService;

    /**
     * 新增品类（管理员）
     */
    @PostMapping("/add")
    public Result add(@RequestBody ApplianceType applianceType) {
        applianceTypeService.add(applianceType);
        return Result.success();
    }

    /**
     * 删除品类（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        applianceTypeService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改品类（管理员）
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody ApplianceType applianceType) {
        applianceTypeService.updateById(applianceType);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ApplianceType type = applianceTypeService.selectById(id);
        return Result.success(type);
    }

    /**
     * 查询所有（管理员用，带条件）
     */
    @GetMapping("/selectAll")
    public Result selectAll(ApplianceType applianceType) {
        List<ApplianceType> list = applianceTypeService.selectAll(applianceType);
        return Result.success(list);
    }

    /**
     * 分页查询（管理员用）
     */
    @GetMapping("/selectPage")
    public Result selectPage(ApplianceType applianceType,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ApplianceType> page = applianceTypeService.selectPage(applianceType, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查询启用的品类列表（前端用户预约用）
     */
    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        List<ApplianceType> list = applianceTypeService.selectEnabled();
        return Result.success(list);
    }

    /**
     * 计算预估积分（新公式：重量 × 每公斤积分）
     *
     * @param typeId 品类ID
     * @param weight 重量(kg)，不传则使用品类默认值
     */
    @GetMapping("/calculatePoints")
    public Result calculatePoints(@RequestParam Integer typeId,
                                  @RequestParam(required = false) BigDecimal weight) {
        Integer points = applianceTypeService.calculatePoints(typeId, weight);
        return Result.success(points);
    }

    /**
     * 计算预估碳减排（公式：重量 × 减碳系数）
     *
     * @param typeId 品类ID
     * @param weight 重量(kg)，不传则使用品类默认值
     */
    @GetMapping("/calculateCarbon")
    public Result calculateCarbon(@RequestParam Integer typeId,
                                  @RequestParam(required = false) BigDecimal weight) {
        BigDecimal carbonSaved = applianceTypeService.calculateCarbonSaved(typeId, weight);
        return Result.success(carbonSaved);
    }

    /**
     * 基于机况计算预估回收价格
     *
     * @param typeId 品类ID
     * @param brandLevel 品牌等级：一线品牌/二线品牌/杂牌
     * @param usageYears 使用年限：1年内/2-3年/5年以上
     * @param functionalStatus 功能状况：正常使用/零件损坏/无法开机
     * @param appearanceLevel 外观成色：完好/轻微划痕/破损
     * @return 预估价格（元）
     */
    @GetMapping("/calculatePrice")
    public Result calculatePrice(@RequestParam Integer typeId,
                                 @RequestParam String brandLevel,
                                 @RequestParam String usageYears,
                                 @RequestParam String functionalStatus,
                                 @RequestParam String appearanceLevel) {
        BigDecimal price = applianceTypeService.calculatePriceByCondition(
                typeId, brandLevel, usageYears, functionalStatus, appearanceLevel
        );
        return Result.success(price);
    }

    /**
     * 根据系数快速计算价格（用于订单查询）
     */
    @GetMapping("/calculatePriceByFactor")
    public Result calculatePriceByFactor(@RequestParam Integer typeId,
                                         @RequestParam BigDecimal conditionFactor) {
        BigDecimal price = applianceTypeService.calculatePriceByFactor(typeId, conditionFactor);
        return Result.success(price);
    }
}