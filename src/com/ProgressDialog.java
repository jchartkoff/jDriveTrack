package com;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class ProgressDialog extends JDialog implements ChangeListener { 
	private static final long serialVersionUID = 6634714265255983346L;
	
	private JLabel statusLabel = new JLabel(); 
	private JLabel noteLabel = new JLabel();
	private JButton cancelButton = new JButton("Cancel");
    private JProgressBar progressBar = new JProgressBar(); 
    private ProgressMonitor monitor; 
 
    public ProgressDialog(ProgressMonitor monitor) throws HeadlessException { 
        super(); 
        init(monitor); 
    }
    
    public ProgressDialog(Frame owner, ProgressMonitor monitor) throws HeadlessException { 
        super(owner, monitor.getTitle(), true); 
        init(monitor); 
    } 
 
    public ProgressDialog(Dialog owner, ProgressMonitor monitor) throws HeadlessException { 
        super(owner, monitor.getTitle()); 
        init(monitor); 
    } 

	private void init(final ProgressMonitor monitor) { 
        this.monitor = monitor; 
 
        progressBar = new JProgressBar(monitor.getOrientation(), monitor.getMinimum(), monitor.getMaximum()); 

        if(monitor.isIndeterminate()) 
            progressBar.setIndeterminate(true); 
        else 
            progressBar.setValue(monitor.getProgress()); 
        
        setTitle(monitor.getTitle());
        statusLabel.setText(monitor.getStatus()); 
        noteLabel.setText(monitor.getNote()); 
        
        initComponents();
        
        monitor.addChangeListener(this); 
        
    	cancelButton.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent event) {
    			monitor.notifyCancel();
    		}
    	});
    } 
 
	@Override
    public void stateChanged(final ChangeEvent ce) { 
        if(!SwingUtilities.isEventDispatchThread()) { 
            SwingUtilities.invokeLater(new Runnable() { 
                public void run() { 
                    stateChanged(ce); 
                } 
            }); 
            return; 
        } 
 
        if(monitor.getProgress() < monitor.getMaximum()) { 
            statusLabel.setText(monitor.getStatus()); 
            noteLabel.setText(monitor.getNote());
            if(!monitor.isIndeterminate()) progressBar.setValue(monitor.getProgress()); 
        } else {
        	setVisible(false);
            dispose();
        }
    } 
	
	private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        progressBar.setBorder(BorderFactory.createEtchedBorder());

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setText("");

        noteLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noteLabel.setText("");

        GroupLayout layout = new GroupLayout(getContentPane());
        
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(noteLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap()));
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(statusLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)));

        pack();
    }

} 
