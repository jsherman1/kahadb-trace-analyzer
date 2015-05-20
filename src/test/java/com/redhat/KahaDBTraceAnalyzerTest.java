package com.redhat;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class KahaDBTraceAnalyzerTest {

    @Test
    public void testDefaultLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"kahadb.log"});

        Assert.assertTrue(true);
    }

    @Test
    public void testClassPathLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        Map<String, Integer> stats = analyzer.analyze(new String[] {"classpath:kahadb.log"});

        Assert.assertTrue(true);
    }

    @Test
    public void testFilePathLoader() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test-kahadb.log";

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
}
