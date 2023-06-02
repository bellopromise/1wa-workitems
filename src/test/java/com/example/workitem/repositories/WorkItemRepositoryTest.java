package com.example.workitem.repositories;


import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import com.example.workitem.service.WorkItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WorkItemRepositoryTest {

    @Mock
    private WorkItemRepository workItemRepository;

    @InjectMocks
    private WorkItemServiceImpl workItemService;

    @Test
    public void getWorkItem_ValidId_ReturnsWorkItem() {
        // Arrange
        String id = "123";
        WorkItem expectedWorkItem = new WorkItem();
        expectedWorkItem.setId(id);
        when(workItemRepository.findById(id)).thenReturn(Optional.of(expectedWorkItem));

        // Act
        WorkItem actualWorkItem = workItemService.getWorkItem(id);

        // Assert
        assertEquals(expectedWorkItem, actualWorkItem);
        verify(workItemRepository, times(1)).findById(id);
    }

    @Test
    public void saveWorkItem_ValidWorkItem_CallsRepositorySave() {
        // Arrange
        WorkItem workItem = new WorkItem();
        when(workItemRepository.save(workItem)).thenReturn(workItem);

        // Act
        workItemService.saveWorkItem(workItem);

        // Assert
        verify(workItemRepository, times(1)).save(workItem);
    }

    @Test
    public void getWorkItem_InvalidId_ReturnsNull() {
        // Arrange
        String id = "123";
        when(workItemRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        WorkItem actualWorkItem = workItemService.getWorkItem(id);

        // Assert
        assertNull(actualWorkItem);
        verify(workItemRepository, times(1)).findById(id);
    }

    @Test
    public void saveWorkItem_NullWorkItem_DoesNotCallRepositorySave() {
        // Arrange
        WorkItem workItem = null;

        // Act
        workItemService.saveWorkItem(workItem);

        // Assert
        verify(workItemRepository, never()).save(any(WorkItem.class));
    }

    @Test
    public void deleteWorkItem_ValidId_CallsRepositoryDelete() {
        // Arrange
        String id = "123";

        // Act
        workItemService.deleteWorkItem(id);

        // Assert
        verify(workItemRepository, times(1)).deleteById(id);
    }

    @Test
    public void getAllWorkItems_ReturnsAllWorkItems() {
        // Arrange
        List<WorkItem> expectedWorkItems = Arrays.asList(
                new WorkItem( 10),
                new WorkItem( 20),
                new WorkItem( 30)
        );
        when(workItemRepository.findAll()).thenReturn(expectedWorkItems);

        // Act
        List<WorkItem> actualWorkItems = workItemService.getAllWorkItems();

        // Assert
        assertEquals(expectedWorkItems.size(), actualWorkItems.size());
        assertTrue(actualWorkItems.containsAll(expectedWorkItems));
        verify(workItemRepository, times(1)).findAll();
    }
}