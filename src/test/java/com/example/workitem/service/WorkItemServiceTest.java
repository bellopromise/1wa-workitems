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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
//import org.powermock.api.mockito.PowerMockito;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkItemServiceTest {

    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private WorkItemProducer workItemProducer;

    @InjectMocks
    private WorkItemServiceImpl workItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllWorkItems_shouldReturnAllWorkItems() {
        // Prepare test data
        List<WorkItem> expectedWorkItems = new ArrayList<>();
        expectedWorkItems.add(new WorkItem(1));
        expectedWorkItems.add(new WorkItem(2));
        when(workItemRepository.findAll()).thenReturn(expectedWorkItems);

        // Execute the method under test
        List<WorkItem> actualWorkItems = workItemService.getAllWorkItems();

        // Verify the result
        assertEquals(expectedWorkItems, actualWorkItems);
    }

    @Test
    void createWorkItem_shouldSaveWorkItemAndSendToProducer() {
        // Prepare test data
        int value = 10;
        WorkItem workItem = new WorkItem(value);

        // Execute the method under test
        String id = workItemService.createWorkItem(value);

        // Verify the interactions
        verify(workItemRepository).save(workItem);
        verify(workItemProducer).sendWorkItem(workItem.getId(), workItem.getValue());

        // Verify the result
        assertEquals(workItem.getId(), id);
    }




    @Test
    void getWorkItem_shouldReturnExistingWorkItem() {
        // Prepare test data
        String id = "123";
        WorkItem expectedWorkItem = new WorkItem(10);
        when(workItemRepository.findById(id)).thenReturn(Optional.of(expectedWorkItem));

        // Execute the method under test
        WorkItem actualWorkItem = workItemService.getWorkItem(id);

        // Verify the result
        assertEquals(expectedWorkItem, actualWorkItem);
    }

    @Test
    void getWorkItem_shouldReturnNullForNonExistingWorkItem() {
        // Prepare test data
        String id = "123";
        when(workItemRepository.findById(id)).thenReturn(Optional.empty());

        // Execute the method under test
        WorkItem actualWorkItem = workItemService.getWorkItem(id);

        // Verify the result
        assertNull(actualWorkItem);
    }

    @Test
    void deleteWorkItem_shouldDeleteExistingWorkItem() {
        // Prepare test data
        String id = "123";

        // Execute the method under test
        workItemService.deleteWorkItem(id);

        // Verify the interaction
        verify(workItemRepository).deleteById(id);
    }

    @Test
    void saveWorkItem_shouldSaveWorkItem() {
        // Prepare test data
        WorkItem workItem = new WorkItem(10);

        // Execute the method under test
        workItemService.saveWorkItem(workItem);

        // Verify the interaction
        verify(workItemRepository).save(workItem);
    }



    @Test
    void getAllWorkItems_shouldReturnEmptyListWhenNoWorkItemsExist() {
        // Prepare test data
        when(workItemRepository.findAll()).thenReturn(Collections.emptyList());

        // Execute the method under test
        List<WorkItem> actualWorkItems = workItemService.getAllWorkItems();

        // Verify the result
        assertNotNull(actualWorkItems);
        assertTrue(actualWorkItems.isEmpty());
    }



    @Test
    void deleteWorkItem_shouldHandleExceptionWhenDeletingWorkItem() {
        // Prepare test data
        String id = "123";
        doThrow(new RuntimeException("Failed to delete work item")).when(workItemRepository).deleteById(id);

        // Execute the method under test
        assertThrows(RuntimeException.class, () -> workItemService.deleteWorkItem(id));

        // Verify the interaction
        verify(workItemRepository).deleteById(id);
        // Additional verification for error handling if applicable
    }

    @Test
    void generateReport_shouldReturnValidReportDto() {
        // Prepare test data
        List<WorkItem> workItems = Arrays.asList(
                new WorkItem(10),
                new WorkItem(10),
                new WorkItem(8),
                new WorkItem(8),
                new WorkItem(8),
                new WorkItem(5)
        );
        when(workItemRepository.findAll()).thenReturn(workItems);

        // Execute the method under test
        ReportDto reportDto = workItemService.generateReport();

        // Verify the result
        assertNotNull(reportDto);
        assertEquals(3, reportDto.getReportData().size());
        assertEquals(2, reportDto.getReportData().get(10).getTotalItems());
        assertEquals(3, reportDto.getReportData().get(8).getTotalItems());
        assertEquals(1, reportDto.getReportData().get(5).getTotalItems());
    }

    @Test
    void getReportsForExport_shouldReturnValidReportExports() {
        // Prepare test data
        List<WorkItem> workItems = Arrays.asList(
                new WorkItem(10),
                new WorkItem(10),
                new WorkItem(8),
                new WorkItem(8),
                new WorkItem(8),
                new WorkItem(5)
        );
        when(workItemRepository.findAll()).thenReturn(workItems);

        // Execute the method under test
        List<ReportExport> reportExports = workItemService.getReportsForExport();

        // Verify the result
        assertNotNull(reportExports);
        assertEquals(3, reportExports.size());

        // Verify the individual report exports
        Map<Integer, ReportExport> reportExportMap = new HashMap<>();
        for (ReportExport reportExport : reportExports) {
            reportExportMap.put(reportExport.getValue(), reportExport);
        }
        assertEquals(2, reportExportMap.get(10).getTotalItems());
        assertEquals(3, reportExportMap.get(8).getTotalItems());
        assertEquals(1, reportExportMap.get(5).getTotalItems());
    }

    @Test
    void generatePdfReport_shouldReturnValidPdfReport() throws Exception {
        // Prepare test data
        List<WorkItem> workItems = Arrays.asList(
                new WorkItem(10),
                new WorkItem(20),
                new WorkItem(10),
                new WorkItem(30),
                new WorkItem(20),
                new WorkItem(20)
        );
        when(workItemRepository.findAll()).thenReturn(workItems);

        // Execute the method under test
        byte[] pdfReport = workItemService.generatePdfReport();

        // Verify the result
        assertNotNull(pdfReport);
        assertTrue(pdfReport.length > 0);
    }

    @Test
    void generatePdfReport_shouldThrowExceptionForEmptyReportData() {
        // Prepare test data
        List<WorkItem> emptyWorkItems = Collections.emptyList();
        when(workItemRepository.findAll()).thenReturn(emptyWorkItems);

        // Execute and verify
        assertThrows(Exception.class, () -> workItemService.generatePdfReport());
    }



    @Test
    public void testGeneratePdfReport_FileNotFoundException() throws FileNotFoundException {
        // Mocking the repository to return an empty list
        when(workItemRepository.findAll()).thenReturn(new ArrayList<>());

        assertThrows(EmptyReportDataException.class, () -> {
            workItemService.generatePdfReport();
        });
    }


}
