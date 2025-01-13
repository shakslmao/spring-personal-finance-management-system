package com.devshaks.personal_finance.notifications;

import com.devshaks.personal_finance.email.EmailNotificationRequest;
import com.devshaks.personal_finance.email.SESService;
import com.devshaks.personal_finance.pushnotification.FCMService;
import com.devshaks.personal_finance.pushnotification.PushNotificationRequest;
import com.devshaks.personal_finance.sms.SMSNotificationRequest;
import com.devshaks.personal_finance.sms.TwilioSMSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// use spring retry for failures
// use thymelefe for personalised sms messages & emails.
// impl twilio web hoook.

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notifications")
@Tag(name = "Notification Controller", description = "Handles Notification Related Operations")
public class NotificationController {

    private final NotificationService notificationService;
    private final FCMService fcmService;
    private final SESService sesService;
    private final TwilioSMSService twilioSMSService;

    @PostMapping("/push")
    @Operation(summary = "Create a Push Notification for a User")
    @ApiResponse(responseCode = "201", description = "Push Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "Push Notification Creation Failed")
    public ResponseEntity<NotificationResponse> createPushNotification(@RequestBody @Valid PushNotificationRequest pushRequest) {
        try {
            NotificationResponse response = notificationService.createPushNotification(pushRequest);
            fcmService.sendFCMNotification(pushRequest.deviceToken(), pushRequest.title(), pushRequest.message());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/email")
    @Operation(summary = "Create a Email Notification for a User")
    @ApiResponse(responseCode = "201", description = "Email Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "Email Notification Creation Failed")
    public ResponseEntity<NotificationResponse> sendEmailNotification(@RequestBody @Valid EmailNotificationRequest emailRequest) {
        try {
            NotificationResponse response = notificationService.createEmailNotification(emailRequest);
            sesService.sendEmailNotification(emailRequest.to(), emailRequest.subject(), emailRequest.body(),
                    emailRequest.from());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/sms")
    @Operation(summary = "Create a SMS Notification for a User")
    @ApiResponse(responseCode = "201", description = "SMS Notification Created Successfully")
    @ApiResponse(responseCode = "400", description = "SMS Notification Creation Failed")
    public ResponseEntity<?> sendSMSNotification(@RequestBody @Valid SMSNotificationRequest smsRequest) {
        log.info("Received SMS notification request: {}", smsRequest);
        try {
            NotificationResponse response = notificationService.createSMSNotification(smsRequest);
            twilioSMSService.sendSMS(smsRequest.to(), smsRequest.body());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to process SMS notification request: {}", smsRequest, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    @Operation(summary = "Retrieve the Notifications for User")
    @ApiResponse(responseCode = "201", description = "Notification Retrieved Successfully")
    @ApiResponse(responseCode = "400", description = "Notification Retrieval Failed")
    public ResponseEntity<List<NotificationResponse>> getUsersNotifications(@RequestParam String recipientId, @RequestParam(required = false) NotificationStatus status) {
        List<NotificationResponse> response = notificationService.getNotificationsByRecipient(recipientId, status);
        return ResponseEntity.ok(response);
    }
}