package com.example.workitem.messaging;

import com.example.workitem.messaging.WorkItemConsumer;
import com.example.workitem.messaging.WorkItemProducer;
import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class WorkItemProducerTest {

    private WorkItemProducer workItemProducer;


    @Mock
    private RabbitTemplate rabbitTemplate;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        workItemProducer = new WorkItemProducer(rabbitTemplate);
    }

    @Test
    public void sendWorkItem_SendsMessageToQueue() {
        // Arrange
        String id = "123";
        int value = 10;
        String queueName = "work-item-queue";

        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        WorkItemProducer workItemProducer = new WorkItemProducer(rabbitTemplate);

        // Act
        workItemProducer.sendWorkItem(id, value);

        // Assert
        Map<String, Object> expectedMessage = new HashMap<>();
        expectedMessage.put("id", id);
        expectedMessage.put("value", value);
        verify(rabbitTemplate, times(1)).convertAndSend(queueName, expectedMessage);
    }


}
