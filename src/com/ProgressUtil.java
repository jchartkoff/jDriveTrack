package com;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressUtil { 
    static class MonitorListener implements ChangeListener, ActionListener { 
    	
        private ProgressMonitor monitor; 
        private Window owner; 
        private Timer decideTimer;
        
        public MonitorListener(ProgressMonitor monitor) { 
            this.owner = null; 
            this.monitor = monitor; 
        }
        
        public MonitorListener(Window owner, ProgressMonitor monitor) { 
            this.owner = owner; 
            this.monitor = monitor; 
        } 
 
        @Override
        public void stateChanged(ChangeEvent ce) { 
            ProgressMonitor monitor = (ProgressMonitor) ce.getSource(); 
            if (monitor.getProgress() != monitor.getMaximum()){ 
                if (decideTimer == null){ 
                    decideTimer = new Timer(monitor.getMillisToDecideToPopup(), this); 
                    decideTimer.setRepeats(false); 
                    decideTimer.start(); 
                } 
            } else { 
                if (decideTimer != null && decideTimer.isRunning()) 
                    decideTimer.stop(); 
                monitor.removeChangeListener(this); 
            } 
        } 
 
        @Override
        public void actionPerformed(ActionEvent e) { 
            monitor.removeChangeListener(this); 
            ProgressDialog dlg;
            if (owner != null) dlg = owner instanceof Frame ? new ProgressDialog((Frame)owner, monitor) 
                    : new ProgressDialog((Dialog)owner, monitor); 
            	else dlg = new ProgressDialog(monitor);
            dlg.pack(); 
            dlg.setLocationRelativeTo(null); 
            dlg.setVisible(true); 
        }

    } 

    public static ProgressMonitor createModelessProgressMonitor() {
    	return createModelessProgressMonitor("Progress...", SwingConstants.HORIZONTAL, 0, 100, false, 500, 2000);
    }
    
    public static ProgressMonitor createModelessProgressMonitor(int millisToDecideToPopup) {
    	return createModelessProgressMonitor("Progress...", SwingConstants.HORIZONTAL, 0, 100, false, millisToDecideToPopup, 2000);
    }
    
    public static ProgressMonitor createModelessProgressMonitor(boolean indeterminate, int millisToDecideToPopup) {
    	return createModelessProgressMonitor("Progress...", SwingConstants.HORIZONTAL, 0, 100, indeterminate, millisToDecideToPopup, 2000);
    }
    
    public static ProgressMonitor createModelessProgressMonitor(String title, int orient, int minimum, int maximum, boolean indeterminate, int millisToDecideToPopup, int millisToPopup) {
    	ProgressMonitor monitor = new ProgressMonitor(title, orient, minimum, maximum, indeterminate, millisToDecideToPopup, millisToPopup); 
        monitor.addChangeListener(new MonitorListener(monitor)); 
        return monitor; 
    }
    
    public static ProgressMonitor createModalProgressMonitor(Component owner) {
    	return createModalProgressMonitor(owner, "Progress...", SwingConstants.HORIZONTAL, 0, 100, false, 500, 2000);
    }
    
    public static ProgressMonitor createModalProgressMonitor(Component owner, boolean indeterminate, int millisToDecideToPopup) {
    	return createModalProgressMonitor(owner, "Progress...", SwingConstants.HORIZONTAL, 0, 100, indeterminate, millisToDecideToPopup, 2000);
    }
    
    public static ProgressMonitor createModalProgressMonitor(Component owner, String title, int orient, int minimum, int maximum, boolean indeterminate, int millisToDecideToPopup, int millisToPopup) { 
        ProgressMonitor monitor = new ProgressMonitor(title, orient, minimum, maximum, indeterminate, millisToDecideToPopup, millisToPopup); 
        Window window = owner instanceof Window ? (Window)owner : SwingUtilities.getWindowAncestor(owner); 
        monitor.addChangeListener(new MonitorListener(window, monitor)); 
        return monitor; 
    } 
}
