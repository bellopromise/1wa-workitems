package com.example.workitem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "workitems")
public class WorkItem {

    @Id
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    private int value;
    private boolean processed;
    private Integer result;

    public WorkItem(){}

    // Constructors, getters, and setters

    public WorkItem(int value) {
        this.value = value;
        this.processed = false;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkItem)) return false;
        WorkItem workItem = (WorkItem) o;
        return getValue() == workItem.getValue() &&
                isProcessed() == workItem.isProcessed() &&
                Objects.equals(getId(), workItem.getId()) &&
                Objects.equals(getResult(), workItem.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue(), isProcessed(), getResult());
    }
}
