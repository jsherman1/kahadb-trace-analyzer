package com.redhat;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class KahaDBTraceAnalyzerTest {

    @Test
    public void testDefaultLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"kahadb.log"});

        Assert.assertNotNull(stats);
        Assert.assertEquals(6, stats.size());
    }

    @Test
    public void testClassPathLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"classpath:kahadb.log"});

        Assert.assertNotNull(stats);
        Assert.assertEquals(6, stats.size());
    }

    @Test
    public void testFilePathLoader() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test1-kahadb.log";

        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"file:" + log});

        Assert.assertNotNull(stats);
        Assert.assertEquals(6, stats.size());
    }

    @Test
    public void testCorrectNumbersForFile1() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test1-kahadb.log";

        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"file:" + log});

        Assert.assertNotNull(stats);
        Assert.assertEquals(6, stats.size());
        Assert.assertEquals(new Integer(0), stats.get("producerSequenceIdTrackerLocation"));
        Assert.assertEquals(new Integer(5), stats.get("Queue.5 (Queue)"));
        Assert.assertEquals(new Integer(2), stats.get("Queue.3 (Queue)"));
        Assert.assertEquals(new Integer(1), stats.get("ActiveMQ.DLQ-2 (Queue)"));
        Assert.assertEquals(new Integer(2), stats.get("DLQ-EMPTY (Queue)"));
    }

    @Test
    public void testCorrectNumbersForFile2() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test2-kahadb.log";

        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"file:" + log});

        Assert.assertNotNull(stats);
        Assert.assertEquals(9, stats.size());
        Assert.assertEquals(new Integer(1), stats.get("producerSequenceIdTrackerLocation"));
        Assert.assertEquals(new Integer(0), stats.get("Message.IN (Queue)"));
        Assert.assertEquals(new Integer(1), stats.get("ANOTHER.MESSAGE.RECEIVED (Queue)"));
        Assert.assertEquals(new Integer(1), stats.get("SOMETHING.OUT (Queue)"));
        Assert.assertEquals(new Integer(106), stats.get("SOMETHING.RECEIVED (Queue)"));
        Assert.assertEquals(new Integer(0), stats.get("AUDIT (Queue)"));
        Assert.assertEquals(new Integer(1), stats.get("ActiveMQ.DLQ (Queue)"));
        Assert.assertEquals(new Integer(2), stats.get("DEAD.LETTER.QUEUE (Queue)"));
    }

    @Test
    public void testCorrectNumbersForFile3() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test3-kahadb.log";

        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"file:" + log});

        Assert.assertNotNull(stats);
        Assert.assertEquals(6, stats.size());
        Assert.assertEquals(new Integer(0), stats.get("producerSequenceIdTrackerLocation"));
        Assert.assertEquals(new Integer(4), stats.get("Queue.5 (Queue)"));
        Assert.assertEquals(new Integer(0), stats.get("ActiveMQ.DLQ-2 (Queue)"));
        Assert.assertEquals(new Integer(2), stats.get("DLQ-EMPTY (Queue)")); //should it not be 0?
    }
}
