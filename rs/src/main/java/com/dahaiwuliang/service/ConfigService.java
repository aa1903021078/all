package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.entity.SysConfig;
import com.dahaiwuliang.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置(key-value)
 */
@Service
public class ConfigService {

    private final SysConfigMapper configMapper;

    public ConfigService(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public List<SysConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getId));
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (SysConfig c : listAll()) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    /** 按 key 保存(存在则更新) */
    public SysConfig save(SysConfig config) {
        SysConfig db = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey()).last("limit 1"));
        if (db != null) {
            db.setConfigValue(config.getConfigValue());
            if (config.getRemark() != null) {
                db.setRemark(config.getRemark());
            }
            configMapper.updateById(db);
            return db;
        }
        configMapper.insert(config);
        return config;
    }

    public void update(Long id, String value) {
        SysConfig db = configMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("配置项不存在");
        }
        db.setConfigValue(value);
        configMapper.updateById(db);
    }
}
