package com.example.workitem.messaging;

import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class WorkItemConsumerTest {

    private WorkItemConsumer workItemConsumer;

    @Mock
    private WorkItemService workItemService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessageConverter messageConverter;

    @Mock
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        workItemConsumer = new WorkItemConsumer(workItemService, rabbitTemplate);
    }

    @Test
    public void onMessage_ValidMessage_ProcessesWorkItem() throws JsonProcessingException {
        // Arrange
        String messageString = "{\"id\":\"123\",\"value\":10}";
        Message message = mock(Message.class);

        SimpleMessageConverter messageConverter = mock(SimpleMessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);

        HashMap<String, Object> messageBody = new HashMap<>();
        messageBody.put("id", messageString);
        when(messageConverter.fromMessage(message)).thenReturn(messageBody);

        WorkItem receivedWorkItem = new WorkItem(10);
        receivedWorkItem.setId("123");
        when(objectMapper.readValue(messageString, WorkItem.class)).thenReturn(receivedWorkItem);

        WorkItem existingWorkItem = new WorkItem(10);
        existingWorkItem.setId("123");
        existingWorkItem.setProcessed(false);
        when(workItemService.getWorkItem("123")).thenReturn(existingWorkItem);

        // Act
        workItemConsumer.onMessage(message);

        // Assert
        verify(workItemService, times(1)).getWorkItem("123");
        verify(workItemService, times(1)).saveWorkItem(any(WorkItem.class));

    }

    @Test
    public void onMessage_ValidMessage_DoesNotProcessWorkItem() throws JsonProcessingException {
        // Arrange
        String messageString = "{\"id\":\"123\",\"value\":10}";
        Message message = mock(Message.class);

        SimpleMessageConverter messageConverter = mock(SimpleMessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);

        HashMap<String, Object> messageBody = new HashMap<>();
        messageBody.put("id", messageString);
        when(messageConverter.fromMessage(message)).thenReturn(messageBody);

        WorkItem receivedWorkItem = new WorkItem(10);
        receivedWorkItem.setId("123");
        when(objectMapper.readValue(messageString, WorkItem.class)).thenReturn(receivedWorkItem);

        WorkItem existingWorkItem = new WorkItem(5);
        existingWorkItem.setId("123");
        existingWorkItem.setProcessed(true); // Set processed to true
        when(workItemService.getWorkItem("123")).thenReturn(existingWorkItem);

        // Act
        workItemConsumer.onMessage(message);

        // Assert
        verify(workItemService, times(1)).getWorkItem("123");
        verify(workItemService, times(0)).saveWorkItem(any(WorkItem.class)); // Expect saveWorkItem to not be called
    }


}