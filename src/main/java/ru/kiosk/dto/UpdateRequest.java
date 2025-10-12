package ru.kiosk.dto;

public record UpdateRequest(ProductDto key, ProductDto patch) { }