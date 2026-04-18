package com.carrental.rental.model;

public enum VehicleStatus {
    AVAILABLE,      // Sẵn sàng cho thuê
    RESERVED,       // Đã đặt (chưa giao)
    IN_USE,         // Đang được thuê
    INSPECTION,     // Đang kiểm tra
    MAINTENANCE,    // Đang bảo trì
    UNAVAILABLE     // Không khả dụng
}
