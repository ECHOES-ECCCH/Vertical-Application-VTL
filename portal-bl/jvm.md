
#JVM Parameters for BL application
<!-- set up in intellij pass listed options in Run/Edit configurations/VM options -->


<!-- IMPORTANT set -Xms, -Xmx to higher values(24G) , with local setup its impossible due hardware limitation  -->
-server
-XX:+AlwaysPreTouch
-Xms8G 
-Xmx8G 
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200 
-XX:ParallelGCThreads=20 
-XX:ConcGCThreads=5 
-XX:+UseCMSInitiatingOccupancyOnly
-XX:+ExplicitGCInvokesConcurrent
-XX:InitiatingHeapOccupancyPercent=70
-XX:+UseStringDeduplication
-XX:MaxMetaspaceSize=1G
-XX:+CMSClassUnloadingEnabled
-XX:ReservedCodeCacheSize=384m
-XX:+UseCodeCacheFlushing
-Dfile.encoding=UTF-8
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/dumps/jvm`date`.hprof


-XX:+AlwaysPreTouch : pre-zeroes memory mapped pages on JVM startup -- improves runtime performance
-Xms, -Xmx: Places boundaries on the heap size to increase the predictability of garbage collection. 
The heap size is limited in replica servers so that even Full GCs do not trigger SIP retransmissions.
 -Xms sets the starting size to prevent pauses caused by heap expansion.

-XX:+UseG1GC: Use the Garbage First (G1) Collector.

-XX:MaxGCPauseMillis: Sets a target for the maximum GC pause time. 
This is a soft goal, and the JVM will make its best effort to achieve it.

-XX:ParallelGCThreads: Sets the number of threads used during parallel phases of the garbage collectors. 
The default value varies with the platform on which the JVM is running.

-XX:ConcGCThreads: Number of threads concurrent garbage collectors will use. 
The default value varies with the platform on which the JVM is running.
-XX:+ExplicitGCInvokesConcurrent: Avoid explicit System.gc() call triggering full GC, instead trigger G1
 -XX:InitiatingHeapOccupancyPercent: Percentage of the (entire) heap occupancy to start a concurrent GC cycle. 
 GCs that trigger a concurrent GC cycle based on the occupancy of the entire heap and not just one of the generations,
  including G1, use this option. A value of 0 denotes 'do constant GC cycles'. The default value is 45.


--server 
JVM is running in server mode; enables the server JIT compiler, which is optimized for operating speed (instead of quick startup or small memory footprint)

-Xms=4g == -Xms=4g equal min and max heap size to avoid hear resizing during run time

-XX:MaxMetaspaceSize=1g  - memory allocated for storing class metadata information in addition to heap memory , by default is unlimited 



Save cause for out of memory error
-XX:+HeapDumpOnOutOfMemoryError
Path for a dump 
-XX:HeapDumpPath=/var/dumps/jvm`date`.hprof

For monitoring ivm running on a server 
-Djava.rmi.server.hostname=<external IP>
-Dcom.sun.mamagment.jmxremote.port=<port>

Code cache - resources for JIT compiler what performs 
optimization analyzing hot spots 
-XX:ReservedCodeCacheSize=384m
-XX:+UseCodeCacheFlushing