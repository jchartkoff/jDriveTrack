package com;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

public final class NetworkTime {
    public final static String[] DEFAULT_TIME_SERVER = { "pool.ntp.org" };
    public final static String OFFSET = "OFFSET";
    public final static String CLOCK = "CLOCK";
    public final static String FAIL = "FAIL";
    public final static String STRATA_CHANGE = "STRATA_CHANGE";
    public final static int DEFAULT_INITIAL_DELAY = 50;
    public final static int DEFAULT_DELAY = 60000;
    public final static int DEFAULT_TIMEOUT = 10000;
    public final static int DEFAULT_CLOCK_UPDATE_PERIOD = 1000;
    public final static int STRATA_DEBOUNCE_COUNTER = 3;
    
    private String[] hosts;
    private NtpV3Packet message;
    private int stratum = -1;
    private int version;
    private int leapIndicator;
    private int precision;
    private int poll;
    private String modeName;
    private int mode;
    private double disp;
    private double rootDelayInMillisDouble;
    private int refId;
    private String refAddr;
    private String refName;
    private long refNtpTime;
    private long origNtpTime;
    private long destJavaTime;
    private long rcvNtpTime;
    private long xmitNtpTime;
    private long destNtpTime;
    private long infoTime;
    private long offsetValue = -1;
    private long delayValue;
    private int timeout;
    private TimeInfo timeInfo;
    private long gpsTime = 0;
    private long ntpTimeLastUpdate = -1;
    private long gpsTimeLastUpdate = -1;
    private int timeStratumChangeTestCounter = STRATA_DEBOUNCE_COUNTER + 1;
    private int preTimeStratum = -2;
    
    private ScheduledFuture<?> refreshHandle = null;
    private ScheduledFuture<?> clockHandle = null;
    private ScheduledExecutorService refreshScheduler;
    private ScheduledExecutorService clockScheduler;
    
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public static final int STRATUM_GPS = -1;
	public static final int STRATUM_NTP0 = 0;
	public static final int STRATUM_NTP1 = 1;
	public static final int STRATUM_NTP2 = 2;
	public static final int STRATUM_NTP3 = 3;
	public static final int STRATUM_NTP4 = 4;
	public static final int STRATUM_NTP5 = 5;
	public static final int STRATUM_NTP6 = 6;
	public static final int STRATUM_NTP7 = 7;
	public static final int STRATUM_NTP8 = 8;
	public static final int STRATUM_NTP9 = 9;
	public static final int STRATUM_NTP10 = 10;
	public static final int STRATUM_NTP11 = 11;
	public static final int STRATUM_NTP12 = 12;
	public static final int STRATUM_NTP13 = 13;
	public static final int STRATUM_NTP14 = 14;
	public static final int STRATUM_NTP15 = 15;
	public static final int STRATUM_UNSYNC = 16;
	
    public NetworkTime() {
    	this(DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT, DEFAULT_INITIAL_DELAY, DEFAULT_DELAY, false, false);
    }

    public NetworkTime(String host, int timeout) {
    	this(toStringArray(host), timeout, 0, 0, false, false);
    }
    
    public NetworkTime(String[] hosts, int timeout) {
    	this(hosts, timeout, 0, 0, false, false);
    }
    
    public NetworkTime(String[] hosts, int timeout, int initialDelay, int delay, boolean enableAutomaticUpdates, 
    		boolean enableClock) {
    	this.hosts = hosts;
    	this.timeout = timeout;
    	this.delayValue = delay;
    	if (enableAutomaticUpdates) initializeAutomaticNetworkTimeUpdates(hosts, initialDelay, delay, timeout);
    	if (enableClock) startClock(DEFAULT_CLOCK_UPDATE_PERIOD);
    }
    
    private static String[] toStringArray(String host) {
    	String[] hosts = { host };
    	return hosts;
    }

    public void requestNetworkTime() {
    	requestNetworkTime(hosts);
    }

    public void requestNetworkTime(String[] hosts) {
    	networkTimeRefresh(hosts, timeout, pcs);
    }

    public void startClock() {
    	startClock(DEFAULT_CLOCK_UPDATE_PERIOD);
    }
    
    public void startClock(int period) {
    	initializeClock(period);
    }
    
    public void stopClock() {
    	clockHandle.cancel(true);
    	clockScheduler.shutdown();
    }
    
    public void startAutomaticNetworkTimeUpdates() {
    	startAutomaticNetworkTimeUpdates(DEFAULT_INITIAL_DELAY, DEFAULT_DELAY, DEFAULT_TIMEOUT);
    }
    
    public void startAutomaticNetworkTimeUpdates(int initialDelay, int delay, int timeout) {
    	initializeAutomaticNetworkTimeUpdates(hosts, initialDelay, delay, timeout);
    }
    
    public void stopAutomaticNetworkTimeUpdates(boolean mayInterruptIfRunning) {
    	refreshHandle.cancel(mayInterruptIfRunning);
    	refreshScheduler.shutdown();
    }
    
    private void initializeAutomaticNetworkTimeUpdates(final String[] hosts, int initialDelay, int delay, final int timeout) {
    	Runnable networkTimeRefresh = new Runnable() { public void run() { networkTimeRefresh(hosts, timeout, pcs); }};
        refreshScheduler = Executors.newScheduledThreadPool(2);
        refreshHandle = refreshScheduler.scheduleAtFixedRate(networkTimeRefresh, initialDelay, delay, TimeUnit.MILLISECONDS);
	}
    
    private void initializeClock(int period) {
    	final Runnable clockUpdate = new Runnable() { public void run() { clockUpdate(pcs); }};
    	clockScheduler = Executors.newScheduledThreadPool(2);
        clockHandle = clockScheduler.scheduleAtFixedRate(clockUpdate, 50, period, TimeUnit.MILLISECONDS);
	}
    
    public void setGpsTimeInMillis(long gpsTime) {
    	this.gpsTime = gpsTime;
    	gpsTimeLastUpdate = System.currentTimeMillis();
    }
    
    private void clockUpdate(PropertyChangeSupport pcs) {
		pcs.firePropertyChange(CLOCK, null, millisToString(getBestTimeInMillis()));
		checkUpdateStratumEventDebounce(pcs);	
    }

    private void checkUpdateStratumEventDebounce(PropertyChangeSupport pcs) {
    	if (timeStratumChangeTestCounter > STRATA_DEBOUNCE_COUNTER) {
    		int newTimeStratum = getTimeStratum();
			timeStratumChangeTestCounter = 0;
			pcs.firePropertyChange(STRATA_CHANGE, preTimeStratum, newTimeStratum);
			preTimeStratum = newTimeStratum;
    	} else {
    		timeStratumChangeTestCounter++;
    	}
    }
    
    private void networkTimeRefresh(final String[] hosts, final int timeout, final PropertyChangeSupport pcs) {
    	SwingWorker<TimeInfo, Void> worker = new SwingWorker<TimeInfo, Void>() {
			@Override
			protected TimeInfo doInBackground() throws Exception {
				NTPUDPClient client = new NTPUDPClient();
		    	client.setDefaultTimeout(timeout);
		    	TimeInfo info = null;
			    try {
			        client.open();
			        for (String host : hosts) {
			            try {
			                InetAddress hostAddr = InetAddress.getByName(host);
			                info = client.getTime(hostAddr);
			            } catch (IOException ioe) {
			            	pcs.firePropertyChange(FAIL, null, null);
			            }
			        }
			    } catch (SocketException e) {
			    	pcs.firePropertyChange(FAIL, null, null);
			    } finally {
			    	client.close();
			    }
				return info;
			}
			@Override
			protected void done() {
				try {
					TimeInfo info = get();
					if (info != null) processResponse(info);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
    	};
    	worker.execute();
    }

    public String getReferenceAddress() {
    	return refAddr;
    }
    
    public String getReferenceName() {
    	return refName;
    }
    
    public int getReferenceIdentifier() {
    	return refId;
    }
    
    public double getRootDelayInMillisDouble() {
    	return rootDelayInMillisDouble;
    }
    
    public double getRootDispersionInMillisDouble() {
    	return disp;
    }
    
    public String getModeName() {
    	return modeName;
    }
    
    public int getMode() {
    	return mode;
    }
    
    public int getPoll() {
    	return poll;
    }
    
    public int getPrecision() {
    	return precision;
    }
    
    public int getLeapIndicator() {
    	return leapIndicator;
    }
    
    public int getVersion() {
    	return version;
    }
    
    public int getStratum() {
    	return stratum;
    }
    
    public NtpV3Packet getNtpV3Message() {
    	return message;
    }
    
    public TimeInfo getTimeInfo() {
    	return timeInfo;
    }

    public long currentTimeInMillis() {
    	return destJavaTime;
    }
    
    public long currentTimeInNanos() {
    	return destNtpTime;
    }

    public long getDelayValueNanos() {
    	return delayValue;
    }
    
    public long getDelayValue() {
    	return delayValue;
    }
    
    public long getOffsetValueMillis() {
    	return offsetValue;
    }

    public long getTransmitNtpTime() {
    	return xmitNtpTime;
    }
    
    public long getReceiveNtpTime() {
    	return rcvNtpTime;
    }
    
    public long getOriginateNtpTime() {
    	return origNtpTime;
    }
    
    public long getReferenceNtpTime() {
    	return refNtpTime;
    }
    
    private void processResponse(TimeInfo info) {
    	timeInfo = info;
    	timeInfo.computeDetails();
    	offsetValue = timeInfo.getOffset();
    	delayValue = timeInfo.getDelay();
        message = timeInfo.getMessage();
        infoTime = timeInfo.getReturnTime();
        stratum = message.getStratum();
        version = message.getVersion();
        leapIndicator = message.getLeapIndicator();
        precision = message.getPrecision();
        modeName = message.getModeName();
        mode = message.getMode();
        poll = message.getPoll();
        disp = message.getRootDispersionInMillisDouble();
        rootDelayInMillisDouble = message.getRootDelayInMillisDouble();
        refId = message.getReferenceId();
        refAddr = NtpUtils.getHostAddress(refId);
        refNtpTime = message.getReferenceTimeStamp().ntpValue();
        rcvNtpTime = message.getReceiveTimeStamp().ntpValue();
        xmitNtpTime = message.getTransmitTimeStamp().ntpValue();
        origNtpTime = message.getOriginateTimeStamp().ntpValue();
        destNtpTime = TimeStamp.getNtpTime(infoTime).ntpValue();
        destJavaTime = TimeStamp.getNtpTime(infoTime).getTime();
        ntpTimeLastUpdate = System.currentTimeMillis();
        
        if (refId != 0) {
            if (refAddr.equals("127.127.1.0")) {
                refName = "LOCAL"; // This is the ref address for the Local Clock
            } else if (stratum >= 2) {
                // If reference id has 127.127 prefix then it uses its own reference clock
                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
                if (!refAddr.startsWith("127.127")) {
                    try {
                        InetAddress addr = InetAddress.getByName(refAddr);
                        String name = addr.getHostName();
                        if (name != null && !name.equals(refAddr)) {
                            refName = name;
                        }
                    } catch (UnknownHostException e) {
                        // some stratum-2 servers sync to ref clock device but fudge stratum level higher... (e.g. 2)
                        // ref not valid host maybe it's a reference clock name?
                        // otherwise just show the ref IP address.
                        refName = NtpUtils.getReferenceClock(message);
                    }
                }
            } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
                refName = NtpUtils.getReferenceClock(message);
                // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
            }
            // otherwise give up on naming...
        }
        if (refName != null && refName.length() > 1) {
            refAddr += " (" + refName + ")";
        }
    }

    public long getNetworkTimeAgeInMillis() {
    	return System.currentTimeMillis() - ntpTimeLastUpdate;
    }
    
    public long getGpsTimeAgeInMillis() {
    	return System.currentTimeMillis() - gpsTimeLastUpdate;
    }

    public long getGpsTimeInMillis() {
    	return gpsTime + getGpsTimeAgeInMillis();
    }
    
    public long getBestTimeInMillis() {
    	int ts = getTimeStratum();
    	if (ts == -1) return getGpsTimeInMillis();
    	if (ts >= 0 && ts <= 15) return localTimeToUTC(getNetworkTimeInMillis());
    	return localTimeToUTC(System.currentTimeMillis());
    }
    
    public long getNetworkTimeInMillis() {
    	return System.currentTimeMillis() + offsetValue;
    }
    
    public Calendar getNetworkTimeInCalendar() {
    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"), Locale.US);
    	cal.add(Calendar.MILLISECOND, (int) offsetValue);
    	return cal;
    }

    public int getTimeStratum() {
    	if (gpsTimeLastUpdate >= 0 && getGpsTimeAgeInMillis() < 2000) return -1; 
    	if (ntpTimeLastUpdate >= 0 && getNetworkTimeAgeInMillis() < 90000 && stratum != -1) return stratum;
    	if (ntpTimeLastUpdate >= 0 && millisToHours(getNetworkTimeAgeInMillis()) < 4) return 15;
    	return 16;
    }

    public long millisToSeconds(long millis) {
    	return millis / 1000;
    }
    
    public long millisToMinutes(long millis) {
    	return millis / 1000 / 60;
    }
    
    public long millisToHours(long millis) {
    	return millis / 1000 / 60 / 60;
    }
    
    public long millisToDays(long millis) {
    	return millis / 1000 / 60 / 60 / 24;
    }

    public String millisToString(long millis) {
    	Date date = new Date(millis);
    	DateFormat formatter = new SimpleDateFormat("HH.mm.ss.SSS");
    	return formatter.format(date);
    }
    
    public String calendarToString(Calendar cal) {
    	long millis = cal.getTimeInMillis();
    	Date date = new Date(millis);
    	DateFormat formatter = new SimpleDateFormat("HH.mm.sss.SSS");
    	return formatter.format(date);
    }
    
    public long localTimeToUTC(long millis) {
        TimeZone tz = TimeZone.getDefault();
        Calendar c = Calendar.getInstance(tz);
        long localMillis = millis;
        int offset, time;

        c.set(1970, Calendar.JANUARY, 1, 0, 0, 0);

        while (localMillis > Integer.MAX_VALUE)
        {
            c.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
            localMillis -= Integer.MAX_VALUE;
        }
        c.add(Calendar.MILLISECOND, (int)localMillis);

        time = c.get(Calendar.MILLISECOND);
        time += c.get(Calendar.SECOND) * 1000;
        time += c.get(Calendar.MINUTE) * 60 * 1000;
        time += c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        offset = tz.getOffset(c.get(Calendar.ERA), c.get(Calendar.YEAR), c.get(Calendar.MONTH), 
        		c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_WEEK), time);

        return millis - offset;
    }
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}