package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequireRole;
import com.dahaiwuliang.entity.Reservation;
import com.dahaiwuliang.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约接口
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @RequireLogin
    public R<Reservation> create(@RequestBody Reservation reservation) {
        return R.ok("预约提交成功", reservationService.create(reservation));
    }

    @GetMapping("/mine")
    @RequireLogin
    public R<List<Reservation>> mine() {
        return R.ok(reservationService.myReservations());
    }

    @PutMapping("/{id}/cancel")
    @RequireLogin
    public R<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return R.ok();
    }

    // ---------------- 商家 ----------------

    @GetMapping("/merchant")
    @RequireRole("MERCHANT")
    public R<List<Reservation>> merchant(@RequestParam(required = false) Integer status) {
        return R.ok(reservationService.merchantReservations(status));
    }

    @PutMapping("/{id}/handle")
    @RequireRole("MERCHANT")
    public R<Void> handle(@PathVariable Long id, @RequestParam Integer status) {
        reservationService.handle(id, status);
        return R.ok();
    }
}
