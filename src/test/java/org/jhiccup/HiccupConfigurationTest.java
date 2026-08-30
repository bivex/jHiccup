package org.jhiccup;

import org.junit.Assert;
import org.junit.Test;

public class HiccupConfigurationTest {

    @Test
    public void testHelpFlag() {
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(new String[]{"-h"}, "default.hlog");
        Assert.assertTrue(config.error);
        Assert.assertNotNull(config.errorMessage);
    }

    @Test
    public void testDefaultConfiguration() {
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(new String[]{}, "default_log.hlog");

        Assert.assertFalse(config.error);
        Assert.assertEquals(0L, config.startDelayMs);
        Assert.assertEquals(5000L, config.reportingIntervalMs);
        Assert.assertEquals(1.0, config.resolutionMs, 0.001);
        Assert.assertEquals(2, config.numberOfSignificantValueDigits);
        Assert.assertEquals(0L, config.runTimeMs);
        Assert.assertFalse(config.allocateObjects);
        Assert.assertFalse(config.startTimeAtZero);
        Assert.assertFalse(config.logFormatCsv);
        Assert.assertFalse(config.launchControlProcess);
        Assert.assertFalse(config.attachToProcess);
        Assert.assertEquals("default_log.hlog", config.logFileName);
    }

    @Test
    public void testExplicitParameters() {
        String[] args = new String[]{
                "-d", "1500",
                "-i", "250",
                "-r", "0.5",
                "-s", "3",
                "-t", "5000",
                "-0",
                "-a",
                "-o",
                "-v",
                "-l", "custom_log.csv"
        };

        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(args, "default.hlog");

        Assert.assertFalse(config.error);
        Assert.assertEquals(1500L, config.startDelayMs);
        Assert.assertEquals(250L, config.reportingIntervalMs);
        Assert.assertEquals(0.5, config.resolutionMs, 0.001);
        Assert.assertEquals(3, config.numberOfSignificantValueDigits);
        Assert.assertEquals(5000L, config.runTimeMs);
        Assert.assertTrue(config.startTimeAtZero);
        Assert.assertTrue(config.allocateObjects);
        Assert.assertTrue(config.logFormatCsv);
        Assert.assertTrue(config.verbose);
        Assert.assertEquals("custom_log.csv", config.logFileName);
    }

    @Test
    public void testLogFileNameSubstitutions() {
        String template = "test.%pid.%date.%host.hlog";
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(new String[]{"-l", template}, "default.hlog");

        Assert.assertFalse(config.error);
        Assert.assertFalse("Template %pid should be replaced", config.logFileName.contains("%pid"));
        Assert.assertFalse("Template %date should be replaced", config.logFileName.contains("%date"));
        Assert.assertFalse("Template %host should be replaced", config.logFileName.contains("%host"));
    }

    @Test
    public void testAttachOptions() {
        String[] args = new String[]{
                "-p", "99999",
                "-j", "/path/to/agent/jHiccup.jar",
                "-i", "1000",
                "-d", "200"
        };

        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(args, "default.hlog");

        Assert.assertFalse(config.error);
        Assert.assertTrue(config.attachToProcess);
        Assert.assertEquals("99999", config.pidOfProcessToAttachTo);
        Assert.assertEquals("/path/to/agent/jHiccup.jar", config.agentJarFileName);
        Assert.assertNotNull(config.agentArgs);
        Assert.assertTrue(config.agentArgs.contains("-i 1000"));
        Assert.assertTrue(config.agentArgs.contains("-d 200"));
    }

    @Test
    public void testAttachMissingJarFails() {
        String[] args = new String[]{"-p", "99999"};
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(args, "default.hlog");

        Assert.assertTrue("Attaching without -j must produce configuration error", config.error);
    }

    @Test
    public void testFileInputConfiguration() {
        String[] args = new String[]{
                "-f", "samples.txt",
                "-fz",
                "-r", "2"
        };

        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(args, "default.hlog");

        Assert.assertFalse(config.error);
        Assert.assertEquals("samples.txt", config.inputFileName);
        Assert.assertTrue(config.fillInZerosInInputFile);
        Assert.assertEquals(1L, config.lowestTrackableValue);
    }

    @Test
    public void testInvalidOptionFails() {
        String[] args = new String[]{"-unknownOption"};
        HiccupMeter.HiccupMeterConfiguration config =
                new HiccupMeter.HiccupMeterConfiguration(args, "default.hlog");

        Assert.assertTrue(config.error);
    }
}
