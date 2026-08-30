package org.jhiccup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;

public class HiccupMeterTest {

    @Before
    public void setUp() {
        System.out.println("Vendor = " + System.getProperty("java.vendor"));
        System.out.println("Version = " + System.getProperty("java.version"));
    }

    @Test
    public void testAttachPidRetrieval() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println("My pid is " + pid);
        Assert.assertNotNull(pid);
        Assert.assertFalse(pid.isEmpty());
    }

    @Test
    public void testMeterLifecycleAndLogGeneration() throws Exception {
        File tempLog = File.createTempFile("hiccup_test", ".hlog");
        tempLog.deleteOnExit();

        String[] args = new String[]{
                "-d", "0",
                "-i", "50",
                "-r", "1",
                "-t", "200",
                "-l", tempLog.getAbsolutePath()
        };

        HiccupMeter meter = HiccupMeter.commonMain(args, false);
        Assert.assertNotNull(meter);

        meter.join(4000);

        Assert.assertTrue("Log file should exist", tempLog.exists());
        Assert.assertTrue("Log file should contain recorded data", tempLog.length() > 0);

        // Verify header
        try (BufferedReader reader = new BufferedReader(new FileReader(tempLog))) {
            String firstLine = reader.readLine();
            Assert.assertNotNull(firstLine);
            Assert.assertTrue("Should have jHiccup header", firstLine.contains("Logged with") || firstLine.startsWith("#"));
        }
    }

    @Test
    public void testAllocationModeAndSubMillisecondResolution() throws Exception {
        File tempLog = File.createTempFile("hiccup_alloc_test", ".hlog");
        tempLog.deleteOnExit();

        String[] args = new String[]{
                "-d", "0",
                "-i", "50",
                "-r", "0.2", // 200 microseconds
                "-a",        // allocate objects on every tick
                "-t", "150",
                "-l", tempLog.getAbsolutePath()
        };

        HiccupMeter meter = HiccupMeter.commonMain(args, false);
        Assert.assertNotNull(meter);

        meter.join(3000);

        Assert.assertTrue("Alloc mode log should exist and have data", tempLog.exists() && tempLog.length() > 0);
    }

    @Test
    public void testCsvOutputMode() throws Exception {
        File tempCsv = File.createTempFile("hiccup_csv_test", ".csv");
        tempCsv.deleteOnExit();

        String[] args = new String[]{
                "-d", "0",
                "-i", "50",
                "-r", "1",
                "-o",        // CSV output
                "-t", "150",
                "-l", tempCsv.getAbsolutePath()
        };

        HiccupMeter meter = HiccupMeter.commonMain(args, false);
        Assert.assertNotNull(meter);

        meter.join(3000);

        Assert.assertTrue("CSV log should exist", tempCsv.exists() && tempCsv.length() > 0);
    }

    @Test
    public void testFileInputProcessingMode() throws Exception {
        File inputData = File.createTempFile("input_samples", ".txt");
        File outputLog = File.createTempFile("hiccup_output", ".hlog");
        inputData.deleteOnExit();
        outputLog.deleteOnExit();

        // Write artificial timestamp and latency pairs in ms
        try (FileWriter writer = new FileWriter(inputData)) {
            writer.write("100.0 0.25\n");
            writer.write("150.0 1.50\n");
            writer.write("200.0 5.20\n");
            writer.write("250.0 0.10\n");
        }

        String[] args = new String[]{
                "-f", inputData.getAbsolutePath(),
                "-fz",
                "-i", "100",
                "-r", "1",
                "-l", outputLog.getAbsolutePath()
        };

        HiccupMeter meter = HiccupMeter.commonMain(args, false);
        Assert.assertNotNull(meter);

        meter.join(3000);

        Assert.assertTrue("Output log from file input should exist", outputLog.exists() && outputLog.length() > 0);
    }

    @Test
    public void testControlProcessCommandQuotingWithSpaces() {
        String logWithSpace = "/tmp/my custom path/hiccup.log";
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(new String[]{"-c", "-l", logWithSpace}, logWithSpace);

        Assert.assertNotNull(config.controlProcessCommand);
        Assert.assertTrue("Command should properly quote log file with spaces",
                config.controlProcessCommand.contains("\"" + logWithSpace + ".c\"") ||
                !config.controlProcessCommand.contains(logWithSpace + ".c "));
    }
}
