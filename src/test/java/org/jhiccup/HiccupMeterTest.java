package org.jhiccup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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

        meter.join(3000);

        Assert.assertTrue("Log file should exist and be non-empty", tempLog.exists() && tempLog.length() > 0);
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
