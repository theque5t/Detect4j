package com.github.theque5t.Detect4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loader {
    static final Logger logger = LogManager.getLogger(Loader.class.getName());
    static final String sep = System.getProperty("file.separator");
    
    static void logError(String prefix, Exception e){
        logger.error(prefix + "Exception: " + e.toString());
        logger.error(prefix + "Exception Class: " + e.getClass().getCanonicalName().toString());
        logger.error(prefix + "Cause: " + e.getCause());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(prefix + " at " + element.toString());
        }
    }

    static class TimeOutTask extends TimerTask {
        private Thread thread;
        private Timer timer;
    
        TimeOutTask(Thread thread, Timer timer){
            this.thread = thread;
            this.timer = timer;
        }
     
        public void run() {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                timer.cancel();
            }
        }
    }

    static class Task implements Runnable {
        private File detectorAgent;
        private String jvmTargetPattern;
        private String scanHome;
        private Integer scanInterval;
        private String scanTargetPattern;
        
        public Task(File detectorAgent, String jvmTargetPattern, String scanHome, Integer scanInterval, String scanTargetPattern) {
            this.detectorAgent = detectorAgent;
            this.jvmTargetPattern = jvmTargetPattern;
            this.scanHome = scanHome;
            this.scanInterval = scanInterval;
            this.scanTargetPattern = scanTargetPattern;
        }

        public void run() {
            while (!Thread.interrupted()) {
                Boolean shouldInterrupt = false;
                String prefix = null;
                try {
                    String scanId = UUID.randomUUID().toString().replace("-", "");
                    prefix = "["+ scanId +"] ";
                    File thisJar = new File(Loader.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
                    );
                    logger.info(prefix + "Searching for JVMs...");
                    List<VirtualMachineDescriptor> jvms = VirtualMachine.list();
                    for (VirtualMachineDescriptor jvmDescriptor : jvms) {
                        logger.info(prefix + "Found JVM: Id: " + jvmDescriptor.id() + ", Name: " + jvmDescriptor.displayName());
                    }
                    for (VirtualMachineDescriptor jvmDescriptor : jvms) {
                        VirtualMachine jvm = null;
                        try{
                            String jvmDisplayName = jvmDescriptor.displayName();
                            if (jvmDisplayName.matches(jvmTargetPattern) && !jvmDisplayName.contains(thisJar.getName())) {
                                logger.info(prefix + "Found JVM matches target pattern. Scanning: Id: " + jvmDescriptor.id() + ", Name: " + jvmDescriptor.displayName());
                            
                                logger.info(prefix + "Attaching to the JVM...");
                                
                                jvm = VirtualMachine.attach(jvmDescriptor.id());
                                
                                String agentArgs = scanHome + "DETECTORDELIMITER" + scanTargetPattern;
                                logger.info(prefix + "Loading agent into JVM...");
                                jvm.loadAgent(detectorAgent.getAbsolutePath(), agentArgs);
    
                                logger.info(prefix + "Returning agent results:");
                                String results = Files.lines(Paths.get(scanHome + "/results.txt"), StandardCharsets.US_ASCII)
                                                    .collect(Collectors.joining(System.lineSeparator()));
                                logger.info(prefix + "\n" + results);
    
                                logger.info(prefix + "Returning agent errors:");
                                String errors = Files.lines(Paths.get(scanHome + "/errors.txt"), StandardCharsets.US_ASCII)
                                                    .collect(Collectors.joining(System.lineSeparator()));
                                logger.info(prefix + "\n" + errors);
                            }
                            else{
                                logger.info(prefix + "Found JVM does not match target pattern. Skipping: Id: " + jvmDescriptor.id() + ", Name: " + jvmDescriptor.displayName());
                            }
                        }
                        catch (IOException e){
                            shouldInterrupt = false;
                            logger.info(prefix + "Caught IOException");
                            logError(prefix, e);
                            logger.info(prefix + "Handling caught IOException for class: " + e.getClass().getCanonicalName());
                        }
                        catch (Exception e) {
                            shouldInterrupt = true;
                            logger.info(prefix + "Caught Exception");
                            logError(prefix, e);
                        }
                        finally{
                            try{
                                if(jvm != null){
                                    logger.info(prefix + "Detaching from JVM instance...");
                                    jvm.detach();
                                }
                            }
                            catch (IOException e){
                                shouldInterrupt = false;
                                logger.info(prefix + "Caught IOException");
                                logError(prefix, e);
                                logger.info(prefix + "Handling caught IOException for class: " + e.getClass().getCanonicalName());
                            }
                            catch (Exception e) {
                                shouldInterrupt = true;
                                logger.info(prefix + "Caught Exception");
                                logError(prefix, e);
                            }
                            
                            if(shouldInterrupt){
                                throw new InterruptedException();
                            }
                        }
                    }
                    logger.info("Waiting for next scan... If not interrupted next scan starts in " + scanInterval + " seconds");
                    Thread.sleep(scanInterval * 1000);
                }
                catch (InterruptedException e) {
                    shouldInterrupt = true;
                }
                catch (Exception e) {
                    shouldInterrupt = true;
                    logger.info("Caught Exception");
                    logError("", e);
                }
                finally{
                    if(shouldInterrupt){
                        logger.info("Interrupting thread...");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Thread thread = null;
        Timer timer = null;
        try{
            File detectorAgent = new File(System.getenv("DETECTOR_AGENT_PATH"));
            logger.info("Detector Agent: " + detectorAgent.getAbsolutePath());

            String detectorHome = System.getenv("DETECTOR_LOG_PATH");
            if(detectorHome == null || detectorHome.isEmpty()){
                detectorHome = System.getProperty("user.dir");
            }
            detectorHome = detectorHome + "/Detect4j";
            String scanHome = detectorHome + "/scan";
            logger.info("Detector Home: " + detectorHome);
            logger.info("Scan Home: " + scanHome);

            String detectorScanInterval = System.getenv("DETECTOR_SCAN_INTERVAL");
            if(detectorScanInterval == null || detectorScanInterval.isEmpty()){
                detectorScanInterval = "900";
            }
            Integer detectorScanIntervalAsInt = Integer.parseInt(detectorScanInterval); 
            logger.info("Scan interval: Every " + detectorScanIntervalAsInt + " seconds");

            final String detectorScanTargetPattern;
            String _detectorScanTargetPattern = System.getenv("DETECTOR_SCAN_TARGET_PATTERN");
            if(_detectorScanTargetPattern == null || _detectorScanTargetPattern.isEmpty()){
                detectorScanTargetPattern = ".*";
            }
            else{
                detectorScanTargetPattern = _detectorScanTargetPattern;
            }
            logger.info("Target Scan Pattern: " + detectorScanTargetPattern);

            final String jvmTargetPattern;
            String _jvmTargetPattern = System.getenv("DETECTOR_JVM_TARGET_PATTERN");
            if(_jvmTargetPattern == null || _jvmTargetPattern.isEmpty()){
                jvmTargetPattern = ".*";
            }
            else{
                jvmTargetPattern = _jvmTargetPattern;
            }
            logger.info("Target JVM Pattern: " + jvmTargetPattern);
            
            String detectorTimeout = System.getenv("DETECTOR_TIMEOUT");
            Integer detectorTimeoutAsInt = null;
            Boolean timeoutTask = false;
            if(detectorTimeout != null && !detectorTimeout.isEmpty()){
                detectorTimeoutAsInt = Integer.parseInt(detectorTimeout);
                timeoutTask = true;
            }
            logger.info("Timeout task: " + timeoutTask);
            logger.info("Timeout (seconds): " + detectorTimeout);

            thread = new Thread(new Task(detectorAgent, jvmTargetPattern, scanHome, detectorScanIntervalAsInt, detectorScanTargetPattern));
            if(timeoutTask){ 
                timer = new Timer();
                timer.schedule(new TimeOutTask(thread, timer), detectorTimeoutAsInt*1000);
                thread.start();
            }else{
                thread.start();
                System.out.println("press enter to quit");
                System.in.read();
                thread.interrupt();
            }
        }
        catch(Exception e){
            logger.info("Caught Exception");
            logError("", e);
        }
    }
}
