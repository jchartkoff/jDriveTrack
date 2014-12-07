package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SignalRecorder {
	private RadioInterface radioInterface = null;
	private boolean testMode = false;
	private boolean recordRun = true;
	private boolean clockRun = true;
	private long time = 0;
	private int interval = 50;
	private long maxRecordingTime = 2000;
	private int maxMapSize = Math.min(Math.round(maxRecordingTime / interval), Integer.MAX_VALUE);
	private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> recorderHandle = null;
	private Map<Long, Double> rec = null;
	private String errorText;
	private int errorCode;

	public SignalRecorder(RadioInterface radioInterface) {
		this(radioInterface, 50, 2000);
	}
	
	public SignalRecorder(RadioInterface radioInterface, int interval) {
		this(radioInterface, interval, 2000);
	}

	public SignalRecorder(RadioInterface radioInterface, int interval, long maxRecordingTime) {
		this.radioInterface = radioInterface;
		this.interval = interval;
		this.maxRecordingTime = maxRecordingTime;
		setRecordTiming(maxRecordingTime, interval);
	}
	
	public void startRecording() {
		recordRun = true;
		clockRun = true;
		recorderRun();
	}

	public void stopRecording() {
		recordRun = false;
		if (!clockRun) stopTimer();
	}
	
	public void startClockOnly() {
		if (!clockRun && !recordRun) {
			clockRun = true;
			recorderRun();
		}
	}
	
	private void recorderRun() {
		Runnable recorder = new Runnable() {
	       public void run() { 
	    	   if (radioInterface != null && !testMode && recordRun) rec.put(time, radioInterface.getdBm());
	    	   if (radioInterface != null && testMode && recordRun) rec.put(time, radioInterface.getTestdBmValue());
	    	   if (clockRun) time += interval;
	       }
	    };
	    recorderHandle = service.scheduleAtFixedRate(recorder, 0, interval, TimeUnit.MILLISECONDS);
	}

	public void stopTimer() {
		if (recorderHandle != null) recorderHandle.cancel(true);
	}

	public void clear() {
		rec.clear();
	}
	
	public boolean isTimerRunning() {
		if (recorderHandle == null || service.isShutdown() || service.isTerminated()) return false;
			else return true;
	}
	
	public int getMaxMapSize() {
		return maxMapSize;
	}

	public int getCurrentMapSize() {
		return rec.size();
	}
	
	public boolean isEmpty() {
		return rec.isEmpty();
	}

	public String getErrorText() {
		return errorText;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	private boolean validateTimeRequest(long time) {
		if (getOldestTimeStampOnRecord() > time) {
			errorText = "Earliest Time Stamp On Record is " + getOldestTimeStampOnRecord() + " Milliseconds After Start Time";
			errorCode = 1;
			System.err.println(errorText);
			return false;
		}
		if (getNewestTimeStampOnRecord() < time) {
			errorText = "Latest Time Stamp On Record is " + getNewestTimeStampOnRecord() + " Milliseconds After Start Time";
			errorCode = 2;
			System.err.println(errorText);
			return false;
		}
		if (time % interval != 0) {
			errorText = "Invalid Time Request - Time is Recorded in Multiples of " + interval + " Milliseconds";
			errorCode = 3;
			System.err.println(errorText); 
			return false;
		}
		errorText = "";
		errorCode = 0;
		return true;
	}
	
	public double getdBmMillisFromEnd(long time) {
		long t = ((rec.size() - 1) * interval) - time;
		try {	
			if (validateTimeRequest(t)) return rec.get(t);
			else return 0;
		} catch (NullPointerException ex) {
			errorText = "No Data Available AT " + time + " Milliseconds";
			errorCode = 4;
			System.err.println(errorText);
			return 0;
		}
	}
	
	public double getdBmMillisFromStart(long time) {
		try {	
			if (validateTimeRequest(time)) return rec.get(time);
				else return 0;
		} catch (NullPointerException ex) {
			errorText = "No Data Available AT " + time + " Milliseconds";
			errorCode = 5;
			System.err.println(errorText);
			return 0;
		}
	}
	
	public long getCurrentTimeStamp() {
		return time;
	}
	
	public long getNewestTimeStampOnRecord() {
		ArrayList<Long> key = new ArrayList<Long>(rec.keySet());
		return key.get(key.size()-1);
	}
	
	public long getOldestTimeStampOnRecord() {
		ArrayList<Long> key = new ArrayList<Long>(rec.keySet());
		return key.get(0);
	}
	
	public long getMaxRecordingTime() {
		return maxRecordingTime;
	}
	
	public int getInterval() {
		return interval;
	}

	public void setRecordTiming(long maxRecordingTime, int interval) {
		stopTimer();
		this.maxRecordingTime = maxRecordingTime;
		this.interval = interval;
		this.maxMapSize = Math.min(Math.round(maxRecordingTime / interval), Integer.MAX_VALUE);
		rec = Collections.synchronizedMap(new MaxSizeHashMap<Long, Double>(maxMapSize));
	}
	
	public void setRadio(RadioInterface radioInterface) {
		stopTimer();
		this.radioInterface = radioInterface;
	}
	
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	private class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;
		private final int maxSize;

	    private MaxSizeHashMap(int maxSize) {
	        this.maxSize = maxSize;
	    }
	    
	    @Override
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	        return size() > maxSize;
	    }
	}
}
