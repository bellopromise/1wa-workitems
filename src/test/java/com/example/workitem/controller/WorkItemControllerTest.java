package com.example.workitem.controller;


import com.example.workitem.dtos.CreateWorkItemDto;
import com.example.workitem.dtos.CreateWorkItemResponseDto;
import com.example.workitem.dtos.ReportDto;
import com.example.workitem.exceptions.ReportGenerationException;
import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class WorkItemControllerTest {

    private WorkItemController workItemController;

    private MockMvc mockMvc;

    @Mock
    private WorkItemService workItemService;

    @BeforeEach
    public void setUp() {
        workItemService = mock(WorkItemService.class);
        workItemController = new WorkItemController(workItemService);
        mockMvc = MockMvcBuilders.standaloneSetup(workItemController).build();
    }

    @Test
    void getAllWorkItems_ShouldReturnListOfWorkItems() throws Exception {
        // Arrange
        List<WorkItem> expectedWorkItems = List.of(new WorkItem(1), new WorkItem(2));
        when(workItemService.getAllWorkItems()).thenReturn(expectedWorkItems);

        // Act and Assert
        mockMvc.perform(get("/work-items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedWorkItems.size())))
                .andExpect(jsonPath("$[0].value").value(1))
                .andExpect(jsonPath("$[1].value").value(2));

        verify(workItemService, times(1)).getAllWorkItems();
    }


    @Test
    void getAllWorkItems_WithNoWorkItems_ShouldReturnEmptyList() throws Exception {
        // Arrange
        List<WorkItem> expectedWorkItems = Collections.emptyList();
        when(workItemService.getAllWorkItems()).thenReturn(expectedWorkItems);

        // Act and Assert
        mockMvc.perform(get("/work-items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(workItemService, times(1)).getAllWorkItems();
    }


    @Test
    public void createWorkItem_WithValidData_ShouldReturnCreatedResponse() throws Exception {
        // Arrange
        CreateWorkItemDto createWorkItemDto = new CreateWorkItemDto();
        createWorkItemDto.setValue(5); // Set a valid value here
        String expectedId = "123";
        when(workItemService.createWorkItem(createWorkItemDto.getValue())).thenReturn(expectedId);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": 5}")) // Pass JSON payload
                .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(workItemService, times(1)).createWorkItem(createWorkItemDto.getValue());
    }

    @Test
    public void createWorkItem_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateWorkItemDto createWorkItemDto = new CreateWorkItemDto();
        createWorkItemDto.setValue(15); // Set an invalid value here

        // Create a mock BindingResult with errors
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(new ObjectError("createWorkItemDto", "Value must be at most 10")));

        // Act and Assert
        mockMvc.perform(post("/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": 15}")
                        .param("value", String.valueOf(createWorkItemDto.getValue()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(workItemService, never()).createWorkItem(createWorkItemDto.getValue());
    }




    @Test
    public void getWorkItem_WithValidId_ShouldReturnWorkItem() throws Exception {
        // Arrange
        String id = "123";
        WorkItem expectedWorkItem = new WorkItem(2);
        expectedWorkItem.setId(id);
        when(workItemService.getWorkItem(id)).thenReturn(expectedWorkItem);

        // Act and Assert
        mockMvc.perform(get("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.value").value(expectedWorkItem.getValue()));

        verify(workItemService, times(1)).getWorkItem(id);
    }


    @Test
    public void getWorkItem_WithInvalidId_ShouldReturnNotFoundResponse() throws Exception {
        // Arrange
        String id = "123";
        when(workItemService.getWorkItem(id)).thenReturn(null);

        // Act and Assert
        mockMvc.perform(get("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Work item not found"));

        verify(workItemService, times(1)).getWorkItem(id);
    }




    @Test
    public void getWorkItem_WithSpecialCharactersInId_ShouldReturnNotFoundResponse() throws Exception {
        // Arrange
        String id = "abc!@#";

        // Act and Assert
        mockMvc.perform(get("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Work item not found"));

        verify(workItemService, times(1)).getWorkItem(id);
    }




    @Test
    public void deleteWorkItem_WithExistingUnprocessedWorkItem_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        String id = "123";
        WorkItem workItem = new WorkItem(1);
        workItem.setId(id);
        workItem.setProcessed(false);
        when(workItemService.getWorkItem(id)).thenReturn(workItem);

        // Act and Assert
        mockMvc.perform(delete("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Work item deleted successfully"));

        verify(workItemService, times(1)).getWorkItem(id);
        verify(workItemService, times(1)).deleteWorkItem(id);
    }


    @Test
    public void deleteWorkItem_WithExistingProcessedWorkItem_ShouldReturnBadRequestResponse() throws Exception {
        // Arrange
        String id = "123";
        WorkItem workItem = new WorkItem(1);
        workItem.setId(id);
        workItem.setProcessed(true);
        when(workItemService.getWorkItem(id)).thenReturn(workItem);

        // Act and Assert
        mockMvc.perform(delete("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Work item has been processed and cannot be deleted"));

        // Verify
        verify(workItemService, times(1)).getWorkItem(id);
    }


    @Test
    public void deleteWorkItem_WithNonExistingWorkItem_ShouldReturnNotFoundResponse() throws Exception {
        // Arrange
        String id = "123";
        when(workItemService.getWorkItem(id)).thenReturn(null);

        // Act and Assert
        mockMvc.perform(delete("/work-items/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Work item not found"));

        verify(workItemService, times(1)).getWorkItem(id);
    }


    // Test for getReport method when report is generated successfully
    @Test
    public void getReport_WithSuccessfulReportGeneration_ShouldReturnReportDto() throws Exception {
        // Arrange
        Map<Integer, ReportDto.ReportItem> reportData = new LinkedHashMap<>();
        // Populate reportData with test data
        // ...

        ReportDto expectedReportDto = new ReportDto();
        expectedReportDto.setReportData(reportData);

        when(workItemService.generateReport()).thenReturn(expectedReportDto);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/work-items/report")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportData").exists())
                .andExpect(jsonPath("$.reportData").isMap())
                .andExpect(jsonPath("$.reportData").isEmpty());

        verify(workItemService, times(1)).generateReport();
    }


    // Test for downloadPdfReport method when PDF report is generated successfully
    @Test
    public void downloadPdfReport_WithSuccessfulReportGeneration_ShouldReturnPdfReport() throws Exception {
        // Arrange
        byte[] expectedPdfReport = "Sample PDF Report".getBytes();
        when(workItemService.generatePdfReport()).thenReturn(expectedPdfReport);

        // Act and Assert
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/work-items/report-pdf")
                        .accept(MediaType.APPLICATION_PDF)) // Set the Accept header to specify PDF content
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String contentDisposition = response.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        String expectedFilename = "report.pdf";

        assertTrue(contentDisposition.contains("filename=\"" + expectedFilename + "\""));
        assertArrayEquals(expectedPdfReport, response.getContentAsByteArray());

        verify(workItemService, times(1)).generatePdfReport();
    }



    // Test for downloadPdfReport method when ReportGenerationException is thrown
    @Test
    public void downloadPdfReport_WithReportGenerationException_ShouldReturnErrorResponse() throws Exception {
        // Arrange
        when(workItemService.generatePdfReport()).thenThrow(ReportGenerationException.class);

        // Act and Assert
        mockMvc.perform(get("/work-items/report-pdf").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Failed to generate PDF report"));

        verify(workItemService, times(1)).generatePdfReport();
    }
}