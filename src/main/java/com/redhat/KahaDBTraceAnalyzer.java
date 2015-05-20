/*
Copyright 2015 Jason Sherman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.redhat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * kahaDB MessageDatabase Trace Analyzer
 */
public class KahaDBTraceAnalyzer {

    private String[] fullSet = null;
    private String[] currentSet = null;
    private String[] priorSet = null;
    private static String LOG_FILE = "kahadb.log";
    private int count = -1;
    private int containsAcks = 0;
    private static boolean concise = false;
    private boolean checkpointDone = false;

    private Map<String, Integer> stats = new HashMap<String, Integer>();

    public static void main(String[] args) {
        KahaDBTraceAnalyzer analyzer = new KahaDBTraceAnalyzer();
        analyzer.analyze(args);
    }

    private void parseArgs(String[] args) {
        if (args.length > 0) {
            LOG_FILE = args[0];
        }

        String sConcise = System.getProperty("concise", "false");
        if (sConcise.equalsIgnoreCase("true")) {
            concise = true;
        }
    }

    public Map<String, Integer> analyze(String[] args) {
        parseArgs(args);

        try {
            System.out.println("Using log file: " + LOG_FILE);

            URL fileURL = getLogFileURL(LOG_FILE);
            if (fileURL == null) {
                System.out.println("Unable to locate log file, please check the name.");
                System.exit(-3);
            }

            BufferedReader br = new BufferedReader(new FileReader(new File(fileURL.toURI())));
            String line;
            int inUse = 0;

            while ((line = br.readLine()) != null) {
                if ((line.contains("MessageDatabase")) && (line.contains("gc candidates"))) {

                    line = line.substring(line.indexOf("gc candidates"), line.length());
                    // beginning of trace output, acquire full journal set
                    if (line.contains("set:")) {
                        // check if we have processed multiple occurrences
                        if (count != -1) {
                            logStats();
                            // reset containsAcks
                            containsAcks = 0;
                            checkpointDone = false;
                        }

                        System.out.println("Acquiring Full Set...");

                        fullSet = acquireSet(line, false);
                        System.out.println("\nFull journal set: " + fullSet.length);
                        count = fullSet.length;
                        priorSet = fullSet;
                    }

                    // only makes sense to gather stats once we have the full set
                    if (fullSet != null) {
                        if (line.contains("after first tx:")) {
                            currentSet = acquireSet(line, false);
                            inUse = priorSet.length - currentSet.length;
                            logDestStats("after first tx", inUse);
                            count = count - inUse;
                            priorSet = currentSet;
                        }

                        if (line.contains("producerSequenceIdTrackerLocation")) {
                            currentSet = acquireSet(line, false);
                            inUse = priorSet.length - currentSet.length;
                            logDestStats("producerSequenceIdTrackerLocation", inUse);
                            count = count - inUse;
                            priorSet = currentSet;
                        }

                        if (line.contains("ackMessageFileMapLocation")) {
                            currentSet = acquireSet(line, false);
                            inUse = priorSet.length - currentSet.length;
                            logDestStats("ackMessageFileMapLocation", inUse);
                            count = count - inUse;
                            priorSet = currentSet;
                        }

                        if (line.contains("tx range")) {
                            currentSet = acquireSet(line, true);
                            inUse = priorSet.length - currentSet.length;
                            logDestStats("tx range", inUse);
                            count = count - inUse;
                            priorSet = currentSet;
                        }

                        if (line.contains("dest")) {
                            currentSet = acquireSet(line, false);
                            inUse = priorSet.length - currentSet.length;
                            logDestStats(acquireDest(line), inUse);
                            count = count - inUse;
                            priorSet = currentSet;
                        }
                    }
                }

                // once we have the full working set, track acks and the final checkpoint
                if (fullSet != null) {
                    if ((line.contains("MessageDatabase")) && (line.contains("not removing data file"))) {
                        containsAcks++;
                    }

                    if ((line.contains("MessageDatabase")) && (line.contains("Checkpoint done."))) {
                        checkpointDone = true;
                    }
                }
            }
            br.close();

            logStats();
        } catch (IOException ioe) {
            System.out.println("Error: " + ioe);
            System.exit(-1);
        } catch (URISyntaxException urise) {
            System.out.println("Error: " + urise);
            System.exit(-2);
        }

        return stats;
    }

    private URL getLogFileURL(String log) throws MalformedURLException {
        URL logUrl = null;
        if (log.startsWith("classpath:")) {
            logUrl = this.getClass().getResource("/" + LOG_FILE.replace("classpath:", ""));
        } else if (log.startsWith("file:")) {
            logUrl = new URL(log);
        } else {
            logUrl = this.getClass().getResource("/" + LOG_FILE);
        }

        return logUrl;
    }

    private String[] acquireSet(String line, boolean tx) {
        String[] set = null;
        ArrayList<String> setList = new ArrayList();

        if (tx) {
            line = line.substring(line.indexOf("]") + 2, line.length());
            line = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
        } else {
            line = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
        }

        StringTokenizer token = new StringTokenizer(line, ",");

        while (token.hasMoreElements()) {
            setList.add((String)token.nextElement());
        }

        set = new String[setList.size()];
        return set;
    }

    private String acquireDest(String line) {
        String type = line.substring(line.indexOf("dest:") + 5, line.indexOf("dest:") + 6);
        line = line.substring(line.indexOf("dest:") + 7, line.length());
        line = line.substring(0, line.indexOf(",")) + (type.equals("0") ? " (Queue)" : " (Topic)");

        return line;
    }

    private void logDestStats(String dest, int count) {
        if (concise && count == 0) {
            // do nothing and return
            return;
        }

        System.out.println(count + " --- " + dest);

        stats.put(dest, count);
    }

    private void logStats() {
        if (fullSet != null) {
            if (containsAcks > 0) {
                System.out.println("Journals containing acks: " + containsAcks);
            }

            System.out.println("Candidates for cleanup: " + (count - containsAcks) + "\n");
            System.out.println((checkpointDone ? "Analysis is complete\n" : "!!! Unable to determine if checkpoint is done. Please try increasing log size !!!\n"));
        } else {
            System.out.println("\nUnable to determine full log set\nCheck that TRACE level logging has been enabled for org.apache.activemq.store.kahadb.MessageDatabase"
                               + "\nand that the log contains a full output from the trace logging.  In some cases you may need to increase the log size.\n");
        }
    }
}
