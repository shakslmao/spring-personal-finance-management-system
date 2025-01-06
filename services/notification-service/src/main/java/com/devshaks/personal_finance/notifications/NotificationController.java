package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/notifications")
@Tag(name = "Notification Controller", description = "Handles Notification Related Operations")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a new Notification for a User")
    @ApiResponse(responseCode = "201", description = "Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "Notification Creation Failed")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody @Valid NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send the Notification for User")
    @ApiResponse(responseCode = "201", description = "Notification Sent Successfully")
    @ApiResponse(responseCode = "400", description = "Notification Sent Failed")
    public ResponseEntity<Void> sendNotification(@PathVariable("id") String id) {
        notificationService.sendNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Send the Notification for User")
    @ApiResponse(responseCode = "201", description = "Notification Read Successfully")
    @ApiResponse(responseCode = "400", description = "Notification Read Failed")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable("id") String id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Retrieve the Notifications for User")
    @ApiResponse(responseCode = "201", description = "Notification Retrieved Successfully")
    @ApiResponse(responseCode = "400", description = "Notification Retrieval Failed")
    public ResponseEntity<List<NotificationResponse>> getUsersNotifications(@RequestParam String recepientId,
            @RequestParam(required = false) NotificationStatus status) {
        List<NotificationResponse> response = notificationService.getNotificationsByRecipient(recepientId, status);
        return ResponseEntity.ok(response);
    }
}