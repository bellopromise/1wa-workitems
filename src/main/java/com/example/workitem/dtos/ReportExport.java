package com.example.workitem.dtos;

public class ReportExport {
    private int value;
    private int totalItems;
    private int processedItems;

    public ReportExport(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getProcessedItems() {
        return processedItems;
    }

    public void setProcessedItems(int processedItems) {
        this.processedItems = processedItems;
    }

    public void incrementTotalItems() {
        totalItems++;
    }

    public void incrementProcessedItems() {
        processedItems++;
    }
}