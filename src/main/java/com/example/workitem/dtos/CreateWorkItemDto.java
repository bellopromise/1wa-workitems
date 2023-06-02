package com.example.workitem.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateWorkItemDto {

    @NotNull(message = "Value is required")
    @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "Value must be a valid number")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Min(value = 1, message = "Value must be at least 1")
    @Max(value = 10, message = "Value must be at most 10")
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
