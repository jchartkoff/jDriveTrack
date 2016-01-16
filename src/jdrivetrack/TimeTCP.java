package jdrivetrack;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.net.time.TimeTCPClient;

public class TimeTCP extends SwingWorker<Date, Void> {
	public static final String[] DEFAULT_TIME_SERVER = { "pool.ntp.org" };
    public static final String NETWORK_TCP_TIME_AVAILABLE = "NETWORK_TCP_TIME_AVAILABLE";
    public static final int DEFAULT_TIMEOUT = 60000;
    
	private String[] hosts = new String[1];
	private int timeout;
	
	public TimeTCP() {
		this(DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT);
	}
	
	public TimeTCP(String host, int timeout) {
		this(toStringArray(host) , timeout);
	}
	
	public TimeTCP(String[] hosts, int timeout) {
		this.hosts = hosts;
		this.timeout = timeout;
	}
	
	private static String[] toStringArray(String host) {
    	String[] hosts = { host };
    	return hosts;
    }
	
    @Override
    protected Date doInBackground() {
        TimeTCPClient client = new TimeTCPClient();
	    Date date = null;
	    client.setDefaultTimeout(timeout);
	    for (String host : hosts) {
		    try {
		        client.connect(host);
		        date = client.getDate();
		    } catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return date;
    }
    @Override
    protected void done() {
    	try {
			firePropertyChange(NETWORK_TCP_TIME_AVAILABLE, null, get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
    }
		    
}
