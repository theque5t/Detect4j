package com.github.theque5t.Log4jDetector;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loader {
    private static final Logger logger = LogManager.getLogger(Loader.class.getName());

    public static void main(String[] args) throws Exception {
        Scanner scanner = null;
        try{
            logger.info("Start");
            String detectorHome = System.getenv("LOG4J_DETECTOR_LOG_PATH");
            if(detectorHome == null || detectorHome.isEmpty()){
                detectorHome = System.getProperty("user.dir");
            }
            detectorHome = detectorHome + "\\Log4jDetector";
            String scanHome = detectorHome + "\\scan";
            logger.info("Detector Home: " + detectorHome);
            logger.info("Scan Home: " + scanHome);

            final String jvmTargetPattern;
            String _jvmTargetPattern = System.getenv("LOG4J_DETECTOR_JVM_TARGET_PATTERN");
            if(_jvmTargetPattern == null || _jvmTargetPattern.isEmpty()){
                jvmTargetPattern = ".*";
            }
            else{
                jvmTargetPattern = _jvmTargetPattern;
            }
            logger.info("Target JVM Pattern: " + jvmTargetPattern);

            scanner = new Scanner(System.in);
            System.out.println("How long should the delay be between scans (seconds)? ");
            Integer secondsToScan = scanner.nextInt();
            logger.info("Scan interval: Every " + secondsToScan + " seconds");
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            String scanID = UUID.randomUUID().toString().replace("-", "");
                            String prefix = "["+ scanID +"] ";
                            logger.info(prefix + "Scan start");
                            File thisJar = new File(Loader.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .getPath()
                            );
                            logger.info(prefix + "Searching for JVMs...");
                            List<VirtualMachineDescriptor> jvms = VirtualMachine.list();
                            for (VirtualMachineDescriptor jvmDescriptor : jvms) {
                                logger.info(prefix + "Found JVM: " + jvmDescriptor.displayName());
                            }
                            for (VirtualMachineDescriptor jvmDescriptor : jvms) {
                                String jvmDisplayName = jvmDescriptor.displayName();
                                if (jvmDisplayName.matches(jvmTargetPattern) && !jvmDisplayName.contains(thisJar.getName())) {
                                    logger.info(prefix + "Found JVM matches target pattern:");
                                    logger.info(prefix + "JVM Id: " + jvmDescriptor.id());
                                    logger.info(prefix + "JVM Display Name: " + jvmDescriptor.displayName());
                                    
                                    logger.info(prefix + "Attaching to the JVM...");
                                    VirtualMachine jvm = VirtualMachine.attach(jvmDescriptor.id());
                                    
                                    logger.info(prefix + "Loading agent into JVM...");
                                    jvm.loadAgent(thisJar.getAbsolutePath(), scanHome);

                                    logger.info(prefix + "Returning agent results:");
                                    String results = Files.lines(Paths.get(scanHome + "\\results.txt"), StandardCharsets.US_ASCII)
                                                        .collect(Collectors.joining(System.lineSeparator()));
                                    logger.info(prefix + "\n" + results);

                                    logger.info(prefix + "Returning agent errors:");
                                    String errors = Files.lines(Paths.get(scanHome + "\\errors.txt"), StandardCharsets.US_ASCII)
                                                        .collect(Collectors.joining(System.lineSeparator()));
                                    logger.info(prefix + "\n" + errors);
                                    
                                    logger.info(prefix + "Detaching from JVM instance...");
                                    jvm.detach();
                                }
                                else{
                                    logger.info(prefix + "Found JVM does not match target pattern. Skipping:");
                                    logger.info(prefix + "JVM Id: " + jvmDescriptor.id());
                                    logger.info(prefix + "JVM Display Name: " + jvmDescriptor.displayName());
                                }
                            }
                            logger.info(prefix + "Scan end");
                            Thread.sleep(secondsToScan * 1000);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            thread.start();
            System.out.println("press enter to quit");
            System.in.read();
            thread.interrupt();
            logger.info("Complete");
        }
        finally{
            if(scanner!=null){
                scanner.close();
            }
        }
    }
}
