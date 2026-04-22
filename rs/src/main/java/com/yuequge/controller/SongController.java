package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.Song;
import com.yuequge.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongMapper songMapper;

    @GetMapping
    public Result<List<Song>> list() {
        return Result.ok(songMapper.selectList(new LambdaQueryWrapper<Song>()
                .orderByDesc(Song::getCreateTime)));
    }
}

@RestController
@RequestMapping("/api/admin/songs")
@RequiredArgsConstructor
class AdminSongController {

    private final SongMapper songMapper;

    @GetMapping
    public Result<PageResult<Song>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size) {
        Page<Song> p = songMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<Song>().orderByDesc(Song::getCreateTime));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    public Result<Song> save(@RequestBody Song song) {
        if (song.getId() == null) {
            song.setCreateTime(LocalDateTime.now());
            songMapper.insert(song);
        } else {
            songMapper.updateById(song);
        }
        return Result.ok(song);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        songMapper.deleteById(id);
        return Result.ok();
    }
}
