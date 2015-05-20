package com.redhat;

import org.junit.Assert;
import org.junit.Test;

public class KahaDBTraceAnalyzerTest {

    @Test
    public void testDefaultLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        analyzer.parseArgs(new String[] {"kahadb.log"});
        analyzer.analyze();

        Assert.assertTrue(true);
    }

    @Test
    public void testClassPathLoader() {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        analyzer.parseArgs(new String[] {"classpath:kahadb.log"});
        analyzer.analyze();

        Assert.assertTrue(true);
    }

    @Test
    public void testFilePathLoader() {
        String log = System.getProperty("buildDirectory") + "/test-classes/test-kahadb.log";

        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        analyzer.parseArgs(new String[] {"file:" + log});
        analyzer.analyze();

        Assert.assertTrue(true);
    }
}
