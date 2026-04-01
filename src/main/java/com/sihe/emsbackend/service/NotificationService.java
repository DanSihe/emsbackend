package com.sihe.emsbackend.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public String buildCancellationRefundMessage(
            String customerName,
            String eventTitle,
            Double refundAmount,
            String refundStatus
    ) {
        return String.format(
                "Hello %s, your booking for \"%s\" was updated. Refund status: %s. Refund amount: %.2f.",
                customerName,
                eventTitle,
                refundStatus,
                refundAmount == null ? 0.0 : refundAmount
        );
    }
}
