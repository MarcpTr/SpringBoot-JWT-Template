
package com.marcptr.jwt.controller;

import com.marcptr.jwt.dto.common.ApiResponse;
import com.marcptr.jwt.dto.response.AdminDashboardResponse;
import com.marcptr.jwt.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {

        return ResponseEntity.ok(
                ApiResponse.ok(adminDashboardService.getDashboard())
        );
    }
}
