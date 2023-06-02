package com.example.workitem.messaging;



import com.example.workitem.model.WorkItem;
import com.example.workitem.service.WorkItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class WorkItemConsumer implements MessageListener {

    private final WorkItemService workItemService;

    private final RabbitTemplate rabbitTemplate;

    private final  ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(WorkItemConsumer.class);
    @Autowired
    public WorkItemConsumer(WorkItemService workItemService, RabbitTemplate rabbitTemplate) {
        this.workItemService = workItemService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onMessage(Message message) {
        HashMap<String, Object> messageBody = (HashMap<String, Object>) rabbitTemplate.getMessageConverter().fromMessage(message);
        String messageString = (String) messageBody.get("id");
        processWorkItem(messageString);
    }




    @RabbitListener(queues = "work-item-queue")
    private void processWorkItem(String message) {

        try {
            WorkItem receivedWorkItem = objectMapper.readValue(message, WorkItem.class);
            WorkItem workItem = workItemService.getWorkItem(receivedWorkItem.getId());

            // Check if the work item exists and if the value matches
            if (workItem != null && workItem.getValue() == receivedWorkItem.getValue()) {
                // Simulate processing delay
                try {
                    Thread.sleep(workItem.getValue() * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // Calculate the result
                int result = workItem.getValue() * workItem.getValue();

                // Update the work item in the database
                workItem.setProcessed(true);
                workItem.setResult(result);
                workItemService.saveWorkItem(workItem);
                logger.info("Work item processed successfully. ID: {}", workItem.getId());
            } else {
                logger.warn("Invalid work item ID or value mismatch. ID: {}", receivedWorkItem.getId());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Failed to process work item due to JSON processing error.", e);
        }
    }

}
