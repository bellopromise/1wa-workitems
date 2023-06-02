package com.example.workitem.dtos;

import java.util.Map;

public class ReportDto {

    private Map<Integer, ReportItem> reportData;

    // Getter and Setter

    public Map<Integer, ReportItem> getReportData() {
        return reportData;
    }

    public void setReportData(Map<Integer, ReportItem> reportData) {
        this.reportData = reportData;
    }

    // Inner class representing the report item
    public static class ReportItem {
        private int totalItems;
        private int processedItems;

        // Getters and Setters

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
}
