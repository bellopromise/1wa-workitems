package com.example.workitem.controller;


import com.example.workitem.dtos.CreateWorkItemDto;
import com.example.workitem.dtos.CreateWorkItemResponseDto;
import com.example.workitem.dtos.ReportDto;
import com.example.workitem.exceptions.EmptyReportDataException;
import com.example.workitem.exceptions.ReportGenerationException;
import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Validated
@Tag(name="Work Items", description = "Endpoints to process, retrieve, and to generate reports of work items.")
@RequestMapping("/work-items")
public class WorkItemController {

    private final WorkItemService workItemService;

    @Autowired
    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @GetMapping
    public List<WorkItem> getAllWorkItems() {
        return workItemService.getAllWorkItems();
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CreateWorkItemResponseDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Value must be at most 10", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Value must be at least 1", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> createWorkItem(@RequestBody @Valid CreateWorkItemDto createWorkItemDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        String id = workItemService.createWorkItem(createWorkItemDto.getValue());
        CreateWorkItemResponseDto responseDto = new CreateWorkItemResponseDto(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = WorkItem.class))
            }),
            @ApiResponse(responseCode = "404", description = "Work item not found",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getWorkItem(@PathVariable String id) {

        WorkItem workItem =  workItemService.getWorkItem(id);
        if(workItem == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Work item not found");
        }
        return ResponseEntity.ok(workItem);
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Work item deleted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Work item not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Work item has been processed and cannot be deleted",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> deleteWorkItem(@PathVariable String id) {
        WorkItem workItem = workItemService.getWorkItem(id);

        if(workItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Work item not found");
        }
        else if (workItem.isProcessed()) {
            return ResponseEntity.badRequest().body("Work item has been processed and cannot be deleted");
        }

        workItemService.deleteWorkItem(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Work item deleted successfully");

    }



    @GetMapping("/report")
    @ApiResponse(responseCode = "200", description = "Success", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ReportDto.class))
    })
    public ReportDto getReport() {
       return workItemService.generateReport();
    }


    @GetMapping("/report-pdf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF report downloaded",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "500", description = "Failed to generate PDF report",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity<?> downloadPdfReport() throws ReportGenerationException, FileNotFoundException, JRException, EmptyReportDataException {
        try{
            byte[] pdfReport = workItemService.generatePdfReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report.pdf");

            return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
        } catch (ReportGenerationException e) {
            // Handle the exception and return an error response
            String errorMessage = "Failed to generate PDF report";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", errorMessage));
        } catch (FileNotFoundException | JRException | EmptyReportDataException e) {
            // Handle other exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
