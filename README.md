# kahadb-trace-analyzer
KahaDB MessageDatabase Trace logging analyzer

# Overview:

This program will will analyze the output of the MessageDatabase trace logging and quickly show you what destinations
are using the journal files.

# Enabling Trace Logging for KahaDB MessageDatabase

The following link provides instructions on enabling trace logging for the KahaDB MessageDatabase class

[1] http://activemq.apache.org/why-do-kahadb-log-files-remain-after-cleanup.html

# Setup

After enabling trace logging and the output is captured, copy the log file to the resources directory of this project.

# Building the project:
`
mvn clean install
`

# Running the project:
`
mvn -Dconcise=true exec:java -Dexec.mainClass="com.redhat.KahaDBTraceAnalyzer" -Dexec.args="kahadb.log"
`

# Options:
```
concise=true - only show destinations which are using journal files
concise=false - (default) show all destinations
```

# Arguments

`
Arg[0] = Name of log file (default = kahadb.log)
`

# Output from the analysis
```
   Acquiring Full Set...
   
   Full journal set: 725
   1 --- producerSequenceIdTrackerLocation
   0 --- tx range
   0 --- MESSAGE.IN (Queue)
   0 --- MESSAGE.OUT (Queue)
   5 --- MESSAGE.QUEUE (Queue)
   0 --- REPORTS.IN (Queue)
   0 --- REPORTS.OUT (Queue)
   0 --- REPORTS.QUEUE (Queue)
   718 --- ActiveMQ.DLQ (Queue)
   Journals containing acks: 1
   Candidates for cleanup: 0
   
   Analysis is complete
```

The output above shows which destinations are using the journal files.  In this case the Activemq.DLQ is the biggest offender with 718 journal files in use.
The result is that there are no journal files available for clean up at this time.  The recommendation in this case would be to purge the Activem.DLQ to free up
space if these messages are no longer needed.  

# Incomplete logs

In some cases the analysis might be incomplete if the log file rolled during the logging.  In this case you might see the following output:

```
   Acquiring Full Set...

   Full journal set: 725
   1 --- producerSequenceIdTrackerLocation
   0 --- tx range
   0 --- MESSAGE.IN (Queue)
   0 --- MESSAGE.OUT (Queue)
   5 --- MESSAGE.QUEUE (Queue)
   Candidates for cleanup: 0

   !!! Unable to determine if checkpoint is done. Please try increasing log size !!!
```

Or there could be cases where the beginning of the output is missing:

```
   Unable to determine full log set
   Check that TRACE level logging has been enabled for org.apache.activemq.store.kahadb.MessageDatabase
   and that the log contains a full output from the trace logging.  In some cases you may need to increase the log size.
```

Should one of these cases occur try increasing the log file size when capturing the trace logging to prevent it from rolling.
