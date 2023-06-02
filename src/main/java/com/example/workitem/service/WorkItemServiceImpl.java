package com.example.workitem.service;

import com.example.workitem.dtos.ReportDto;
import com.example.workitem.dtos.ReportExport;
import com.example.workitem.exceptions.EmptyReportDataException;
import com.example.workitem.exceptions.ReportGenerationException;
import com.example.workitem.messaging.WorkItemProducer;
import com.example.workitem.model.WorkItem;
import com.example.workitem.repositories.WorkItemRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkItemServiceImpl implements WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final WorkItemProducer workItemProducer;

    private static final Logger logger = LoggerFactory.getLogger(WorkItemServiceImpl.class);

    @Autowired
    public WorkItemServiceImpl(WorkItemRepository workItemRepository, WorkItemProducer workItemProducer) {
        this.workItemRepository = workItemRepository;
        this.workItemProducer = workItemProducer;
    }

    public List<WorkItem> getAllWorkItems() {
        return workItemRepository.findAll();
    }

    @Override
    public String createWorkItem(int value) {
        WorkItem workItem = new WorkItem(value);
        workItemRepository.save(workItem);
        workItemProducer.sendWorkItem(workItem.getId(), workItem.getValue());

        logger.info("Work item created with ID: {}", workItem.getId());

        return workItem.getId();
    }



    @Override
    public WorkItem getWorkItem(String id) {
        return workItemRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteWorkItem(String id) {
        workItemRepository.deleteById(id);
    }

    @Override
    public void saveWorkItem(WorkItem workItem) {
        workItemRepository.save(workItem);
    }

    @Override
    public ReportDto generateReport() {
        // Retrieve all work items from the database
        List<WorkItem> allWorkItems = workItemRepository.findAll();

        // Calculate the report data
        Map<Integer, ReportDto.ReportItem> reportData = new HashMap<>();
        for (WorkItem workItem : allWorkItems) {
            int value = workItem.getValue();
            boolean processed = workItem.isProcessed();
            //int result = workItem.getResult();

            // Update the report data
            if (!reportData.containsKey(value)) {
                reportData.put(value, new ReportDto.ReportItem());
            }
            ReportDto.ReportItem reportItem = reportData.get(value);
            reportItem.incrementTotalItems();
            if (processed) {
                reportItem.incrementProcessedItems();
            }

            logger.debug("Work item value: {}", value);
            logger.debug("Work item processed: {}", processed);
        }

        // Create the ReportDTO object
        ReportDto reportDTO = new ReportDto();
        reportDTO.setReportData(reportData);

        return reportDTO;
    }


    public List<ReportExport> getReportsForExport() {
        // Retrieve all work items from the database
        ReportDto reportDto = generateReport();

        // Create a list to store the converted report items
        List<ReportExport> reportExports = new ArrayList<>();

        // Iterate over the reportData map and convert each entry to ReportExport
        for (Map.Entry<Integer, ReportDto.ReportItem> entry : reportDto.getReportData().entrySet()) {
            int value = entry.getKey();
            ReportDto.ReportItem reportItem = entry.getValue();
            int totalItems = reportItem.getTotalItems();
            int processedItems = reportItem.getProcessedItems();

            // Create a new ReportExport object and add it to the list
            ReportExport reportExport = new ReportExport(value);
            reportExport.setTotalItems(totalItems);
            reportExport.setProcessedItems(processedItems);

            reportExports.add(reportExport);

        }

        return reportExports;
    }

    @Override
    public byte[] generatePdfReport() throws ReportGenerationException, EmptyReportDataException {

        logger.info("Generating PDF report");
        try {

            List<WorkItem> allWorkItems = workItemRepository.findAll();

            // Check if report data is empty
            if (allWorkItems.isEmpty()) {
                throw new EmptyReportDataException("No work items found for generating the report.");
            }
            // Load the JasperReports template
            File file = ResourceUtils.getFile("classpath:report3.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            // Fetch the report data
            List<ReportExport> reportData = getReportsForExport();
            // Create a JRDataSource using the report data
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            // Compile the JasperPrint object
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "promise bello");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export the JasperPrint to PDF
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();

            logger.info("PDF report generated");

            return outputStream.toByteArray();
        } catch (FileNotFoundException e) {
            logger.error("Failed to generate PDF report: JasperReports template file not found", e);
            throw new ReportGenerationException("Failed to generate PDF report: JasperReports template file not found.", e);
        } catch (JRException e) {
            logger.error("Failed to generate PDF report: JasperReports exception", e);
            throw new ReportGenerationException("Failed to generate PDF report: JasperReports exception.", e);
        }
    }




}
