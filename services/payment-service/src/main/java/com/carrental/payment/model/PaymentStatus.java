package com.carrental.payment.model;

public enum PaymentStatus {
    PENDING,        // Chờ thanh toán
    PROCESSING,     // Đang xử lý
    COMPLETED,      // Hoàn thành
    FAILED,         // Thất bại
    REFUNDED,       // Đã hoàn tiền
    PARTIALLY_REFUNDED  // Hoàn tiền một phần
}
