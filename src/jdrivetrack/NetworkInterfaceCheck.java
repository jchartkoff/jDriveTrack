package jdrivetrack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NetworkInterfaceCheck {
	
	private ScheduledFuture<?> interfaceHandle = null;
	private ScheduledFuture<?> serverHandle = null;
	private Interface networkInterfaceAvailability = Interface.UNKNOWN;
	private Server serverAvailability = Server.UNKNOWN;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public static final String ONLINE = "ONLINE";
	public static final String OFFLINE = "OFFLINE";
	public static final String WAITING = "WAITING";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String AVAILABLE = "AVAILABLE";
	public static final String UNAVAILABLE = "UNAVAILABLE";
	public static final String READY_TO_EXIT = "READY_TO_EXIT";
	
	public enum Interface {ONLINE, OFFLINE, WAITING, UNKNOWN}
	public enum Server {AVAILABLE, UNAVAILABLE, WAITING, UNKNOWN}

	private ScheduledExecutorService networkInterfaceAvailabilityScheduler = Executors.newScheduledThreadPool(1);
	final ScheduledExecutorService serverAvailabilityScheduler = Executors.newScheduledThreadPool(1);
	
	public NetworkInterfaceCheck() { }	

	public Interface getNetworkInterfaceAvailability() {
		return networkInterfaceAvailability;
	}
	
	public Server getServerAvailability() {
		return serverAvailability;
	}
    
	public void cancel() {
		networkInterfaceAvailabilityScheduler.shutdownNow();
		if (interfaceHandle != null && interfaceHandle.isDone()) interfaceHandle.cancel(true);
		serverAvailabilityScheduler.shutdownNow();
		if (serverHandle != null && serverHandle.isDone()) serverHandle.cancel(true);
		pcs.firePropertyChange(READY_TO_EXIT, null, true);
	}
	
	public void adviseOnNetworkInterfaceAvailability() {
		final Runnable checkNetworkInterface = new Runnable() {@Override
		public void run() {isNetworkInterfaceAvailable();}};
		interfaceHandle = networkInterfaceAvailabilityScheduler.scheduleAtFixedRate(checkNetworkInterface, 10, 10, TimeUnit.SECONDS);
		networkInterfaceAvailabilityScheduler.execute(checkNetworkInterface);
	}

	public void adviseOnServerAvailability(final String urlString) {
		final Runnable checkServer = new Runnable() {@Override
		public void run() {isServerAvailable(urlString);}};
		serverHandle = serverAvailabilityScheduler.scheduleAtFixedRate(checkServer, 10, 10, TimeUnit.SECONDS);
		serverAvailabilityScheduler.execute(checkServer);
	}
	
	private void isServerAvailable(String urlString) {
        try {
            URL url = new URL(urlString);
            url.getContent();
            serverHandle.cancel(true);
            serverAvailability = Server.AVAILABLE;
            pcs.firePropertyChange(AVAILABLE, null, urlString);
            pcs.firePropertyChange(READY_TO_EXIT, null, true);
        } catch (Exception e) {
        	e.printStackTrace();
        	if (!serverHandle.isCancelled()) {
	        	serverAvailability = Server.UNAVAILABLE;
	        	pcs.firePropertyChange(WAITING, null, urlString);
        	}
        }
	}

	private void isNetworkInterfaceAvailable() {
		Enumeration<NetworkInterface> interfaces = null;
		NetworkInterface interf = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
			    interf = interfaces.nextElement();
			    if (interf.isUp() && !interf.isLoopback()) {
				    List<InterfaceAddress> adrs = interf.getInterfaceAddresses();
				    for (Iterator<InterfaceAddress> iter = adrs.iterator(); iter.hasNext();) {
				        InterfaceAddress adr = iter.next();
				        InetAddress inadr = adr.getAddress();
				        if (inadr instanceof Inet4Address) {
				        	interfaceHandle.cancel(false);
				        	networkInterfaceAvailability = Interface.ONLINE;
				        	pcs.firePropertyChange(ONLINE, null, interf.getDisplayName());
				        	pcs.firePropertyChange(READY_TO_EXIT, null, true);
				        }
		            }
			    }
			}
		} catch (SocketException e) {
			if (!interfaceHandle.isCancelled()) {
				networkInterfaceAvailability = Interface.OFFLINE;
				pcs.firePropertyChange(WAITING, null, interf.getDisplayName());
			}
		}
	}	
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}

