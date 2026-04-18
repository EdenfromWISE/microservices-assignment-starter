package com.carrental.rental.model;

public enum RentalStatus {
    PENDING,        // Đang chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    IN_PROGRESS,    // Đang thuê (xe đã giao)
    INSPECTION,     // Đang kiểm tra khi trả xe
    COMPLETED,      // Hoàn thành
    PENALTY_DUE,    // Có phạt chưa thanh toán
    CANCELLED       // Đã hủy
}
