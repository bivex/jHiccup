package org.jhiccup;

import org.junit.Assert;
import org.junit.Test;

public class IdleTest {

    @Test
    public void testIdleExecutionWithTimeout() throws Exception {
        String[] args = new String[]{"-t", "100", "-n", "-v"};
        Idle idle = new Idle(args);
        Assert.assertNotNull(idle);
        Assert.assertEquals(100L, idle.config.runTimeMs);
        Assert.assertFalse(idle.config.useIdleReader);
        Assert.assertTrue(idle.config.verbose);

        idle.start();
        idle.join(2000);
        Assert.assertFalse("Idle thread should have finished execution", idle.isAlive());
    }

    @Test
    public void testIdleEarlyTermination() throws Exception {
        String[] args = new String[]{"-t", "10000", "-n"};
        Idle idle = new Idle(args);
        idle.start();

        Thread.sleep(50);
        idle.terminate();
        idle.join(1000);
        Assert.assertFalse("Idle thread should terminate upon interrupt", idle.isAlive());
    }
}
