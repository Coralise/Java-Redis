package me.wayne;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import me.wayne.daos.ConsumerGroup;
import me.wayne.daos.StreamId;

public class ConsumerGroupTest {
    private ConsumerGroup consumerGroup;

    @Before
    public void setUp() {
        consumerGroup = new ConsumerGroup("testGroup", "testStream", new StreamId("0-0"));
    }

    @Test
    public void testAddConsumer() {
        Thread consumerThread = new Thread();
        consumerGroup.addConsumer("consumer1", consumerThread);
        assertEquals(1, consumerGroup.getConsumers().size());
        assertTrue(consumerGroup.getConsumers().containsKey("consumer1"));
    }

    @Test
    public void testRemoveConsumer() {
        Thread consumerThread = new Thread();
        consumerGroup.addConsumer("consumer1", consumerThread);
        consumerGroup.removeConsumer("consumer1");
        assertEquals(0, consumerGroup.getConsumers().size());
        assertFalse(consumerGroup.getConsumers().containsKey("consumer1"));
    }

    @Test
    public void testHasConsumer() {
        Thread consumerThread = new Thread();
        consumerGroup.addConsumer("consumer1", consumerThread);
        assertTrue(consumerGroup.hasConsumer("consumer1"));
        assertFalse(consumerGroup.hasConsumer("nonExistentConsumer"));
    }

    @Test
    public void testHasConsumers() {
        assertFalse(consumerGroup.hasConsumers());
        Thread consumerThread = new Thread();
        consumerGroup.addConsumer("consumer1", consumerThread);
        assertTrue(consumerGroup.hasConsumers());
    }
}
