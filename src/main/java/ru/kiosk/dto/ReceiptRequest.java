package ru.kiosk.dto;

public record ReceiptRequest(ProductDto dto, int quantity) { }