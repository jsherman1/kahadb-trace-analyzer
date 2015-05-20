package com.redhat;

import org.junit.Assert;
import org.junit.Test;

public class KahaDBTraceAnalyzerTest {

    @Test
    public void test() {
        String[] args = new String[] {"kahadb.log"};
        KahaDBTraceAnalyzer.main(args);

        Assert.assertTrue(true);
    }
}
