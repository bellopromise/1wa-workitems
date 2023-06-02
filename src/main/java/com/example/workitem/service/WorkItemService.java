package com.example.workitem.service;

import com.example.workitem.dtos.ReportDto;
import com.example.workitem.exceptions.EmptyReportDataException;
import com.example.workitem.model.WorkItem;
import net.sf.jasperreports.engine.JRException;

import java.io.FileNotFoundException;
import java.util.List;

public interface WorkItemService {
    List<WorkItem> getAllWorkItems();
    String createWorkItem(int value);
    WorkItem getWorkItem(String id);
    void deleteWorkItem(String id);
    void saveWorkItem(WorkItem workItem);
    ReportDto generateReport();
    byte[] generatePdfReport() throws FileNotFoundException, JRException, EmptyReportDataException;
}