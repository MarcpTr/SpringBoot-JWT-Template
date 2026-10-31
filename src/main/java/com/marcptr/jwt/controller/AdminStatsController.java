
package com.marcptr.jwt.controller;

import com.marcptr.jwt.dto.common.ApiResponse;
import com.marcptr.jwt.dto.response.AdminUserStatsResponse;
import com.marcptr.jwt.service.AdminDashboardService;
import com.marcptr.jwt.dto.response.RegistrationStatsResponse;
import com.marcptr.jwt.model.enums.RegistrationPeriod;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.marcptr.jwt.dto.response.AdminSessionStatsResponse;
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserStatsResponse>> getUserStats() {

        return ResponseEntity.ok(
                ApiResponse.ok(adminDashboardService.getUserStats()));
    }

    @GetMapping("/registrations")
    public ResponseEntity<ApiResponse<RegistrationStatsResponse>> getRegistrationStats(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue = "DAY") RegistrationPeriod period) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        adminDashboardService.getRegistrationStats(from, to, period)));
    }

@GetMapping("/sessions")
public ResponseEntity<ApiResponse<AdminSessionStatsResponse>> getSessionStats() {

    return ResponseEntity.ok(
            ApiResponse.ok(adminDashboardService.getSessionStats())
    );
}

}
