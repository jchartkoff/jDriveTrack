package jdrivetrack;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CPUUsageCollector implements Runnable {
  private final static long INTERVAL = 1000L; // polling interval in ms
  private long totalCpuTime = 0L; // total CPU time in millis
  private double load = 0d; // average load over the interval
  private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
  private boolean stopped = false;

  @Override
  public void run() {
    try {
      while (!isStopped()) {
        long start = System.currentTimeMillis();
        long[] ids = threadMXBean.getAllThreadIds();
        long time = 0L;
        for (long id: ids) {
          long l = threadMXBean.getThreadCpuTime(id);
          if (l >= 0L) time += l;
        }
        long newCpuTime = time / 1000000L;
        synchronized(this) {
          long oldCpuTime = totalCpuTime;
          totalCpuTime = newCpuTime;
          // load = CPU time difference / sum of elapsed time for all CPUs
          load = (double) (newCpuTime - oldCpuTime) / 
           (double) (INTERVAL * Runtime.getRuntime().availableProcessors());
        }
        long sleepTime = INTERVAL - (System.currentTimeMillis() - start);
        goToSleep(sleepTime <= 0L ? INTERVAL : sleepTime);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public synchronized double getLoad() {
    return load;
  }

  public synchronized void goToSleep(final long time) {
    try {
      wait(time);
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  public synchronized void setStopped(final boolean stopped) {
    this.stopped = stopped;
  }
}
