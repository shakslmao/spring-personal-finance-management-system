package com.devshaks.personal_finance.notifications;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.email.SESService;
import com.devshaks.personal_finance.pushnotification.FCMService;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notifications")
@Tag(name = "Notification Controller", description = "Handles Notification Related Operations")
public class NotificationController {

    private final NotificationService notificationService;
    private final FCMService fcmService;
    private final SESService sesService;
    
    @PostMapping("/push")
    @Operation(summary = "Create a Push Notification for a User")
    @ApiResponse(responseCode = "201", description = "Push Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "Push Notification Creation Failed")
    public ResponseEntity<NotificationResponse> createPushNotification(
            @RequestBody @Valid PushNotificationRequest request) {
        try {
            NotificationResponse response = notificationService.createPushNotification(request);
            fcmService.sendFCMNotification(request.deviceToken(), request.title(), request.message());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/email")
    @Operation(summary = "Create a Email Notification for a User")
    @ApiResponse(responseCode = "201", description = "Email Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "Email Notification Creation Failed")
    public ResponseEntity<NotificationResponse> sendEmailNotification(
            @RequestBody @Valid EmailNotificationRequest emailRequest) {
        try {
            NotificationResponse response = notificationService.createEmailNotification(emailRequest);
            sesService.sendEmailNotification(emailRequest.to(), emailRequest.subject(), emailRequest.body(),
                    emailRequest.from());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
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