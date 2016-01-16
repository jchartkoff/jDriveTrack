package jdrivetrack;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.net.time.TimeUDPClient;

public class TimeUDP extends SwingWorker<Date, Void> {
	public static final String[] DEFAULT_TIME_SERVER = { "pool.ntp.org" };
    public static final String NETWORK_UDP_TIME_AVAILABLE = "NETWORK_UDP_TIME_AVAILABLE";
    public static final int DEFAULT_TIMEOUT = 60000;
    
	private String[] hosts;
	private int timeout;
	
	public TimeUDP() {
		this(DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT);
	}
	
	public TimeUDP(String host, int timeout) {
		this(toStringArray(host) , timeout);
	}
	
	public TimeUDP(String[] hosts, int timeout) {
		this.hosts = hosts;
		this.timeout = timeout;
	}
	
	private static String[] toStringArray(String host) {
    	String[] hosts = { host };
    	return hosts;
    }
	
    @Override
    protected Date doInBackground() {
    	TimeUDPClient client = new TimeUDPClient();
        Date date = null;
        client.setDefaultTimeout(timeout);
        try {
	        for (String host : hosts) {
		        try {
					client.open();
					date = client.getDate(InetAddress.getByName(host));
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
        } finally {
        	client.close();
        }
        return date;
    }
    @Override
    protected void done() {
    	try {
			firePropertyChange(NETWORK_UDP_TIME_AVAILABLE, null, get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
    }
		    
}
