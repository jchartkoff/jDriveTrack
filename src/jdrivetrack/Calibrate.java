package jdrivetrack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

public class Calibrate {
	private JPanel chartPanel;
	private JComboBox<Object> dBmComboBox;
	private JComboBox<Object> calFileEditorComboBox;
	private JComboBox<Object> modelNumberComboBox;
	private SortedComboBoxModel calFileEditorComboBoxModel;
	private SortedComboBoxModel modelNumberComboBoxModel;
    private JLabel calFileEditorComboBoxLabel;
    private JPanel calibrationGraphic;
    private JPanel dBmComboBoxPanel;
    private JLabel equalSignLabel;
    private JButton fitToCurveButton;
    private JLabel manufacturerLabel;
    private JLabel manufacturerLabelLabel;
    private JLabel modelNumberComboBoxLabel;  
    private JButton newFileButton;
    private JFormattedTextField rssiTextField;
    private JPanel rssiTextFieldPanel;
    private JFormattedTextField snTextField;
    private JLabel snTextFieldLabel;
	private Chart chart;
	private JLabel rssiCurrentLabelLabel;
	private JLabel rssiCurrentLabel;
	private JButton rssiSetButton;
	private int rssiSetValue;
	private File calDir = null;
	private CalibrationDataObject cdo = null;
    private int[] rssiArray;
    private String calFileDir;
    private String[][] devices;
    private boolean calFileOpen = false;
    
	private List<String> fileList = new ArrayList<String>(128);
	
	private static final String[] DEFAULT_DBM_VALUES = {"-40","-50","-60","-70","-80","-90","-100","-110","-120","-130"};
	
	
	public Calibrate(String calFileDir, String[][] devices) {
		this.calFileDir = calFileDir;
		this.devices = devices;
		initializeComponents();
	}
	
	public boolean openCalFile(String fileName) {
		cdo = new CalibrationDataObject(calFileDir, fileName);
		loadDbmComboBox();
		updatePanel();
		setCalFileEditorComboBox(fileName);
		configureListeners();
		return calFileOpen;
	}
	
	public boolean isCalFileOpen() {
		return calFileOpen;
	}
	
	private void setCalFileEditorComboBox(String fileName) {
		for (int i = 0; i < calFileEditorComboBox.getItemCount(); i++) {
			if (calFileEditorComboBox.getItemAt(i).toString().contains(fileName)) {
				calFileEditorComboBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public CalibrationDataObject getCalibrationDataObject() {
		return cdo;
	}
	
	private void  buildNewCalibrationFile() {
		String file = modelNumberComboBox.getSelectedItem().toString() + "_" + snTextField.getText() + ".cal";
		String fileName = calFileDir + File.separator + file;
		cdo = new CalibrationDataObject(fileName, manufacturerLabel.getText(),
			modelNumberComboBox.getSelectedItem().toString(), snTextField.getText());
		calFileEditorComboBoxModel.addElement(file);
		calFileEditorComboBox.validate();
		calFileEditorComboBox.setSelectedItem(file);
		loadDbmComboBox();
		updatePanel();
	}

	private void loadDbmComboBox() {
		dBmComboBox.removeAllItems();
		for (int i = 0; i < cdo.getdBmArray().length; i++) {
			dBmComboBox.addItem(cdo.getdBmStringArray()[i]);
		}
		dBmComboBox.validate();
		dBmComboBox.setSelectedIndex(0);
	}

	private void updatePanel() {
		rssiArray = cdo.getRssiArray();
		int[] dBmArray = cdo.getdBmArray();
		
		rssiTextField.setText(String.valueOf(rssiArray[dBmComboBox.getSelectedIndex()]));
		manufacturerLabel.setText(cdo.getManufacturer());
		snTextField.setText(cdo.getSerialString());
		
		for (int m = 0; m < modelNumberComboBox.getItemCount(); m++) {
			if (modelNumberComboBox.getItemAt(m).toString().contains(cdo.getModelString())) {
				modelNumberComboBox.setSelectedIndex(m);
				break;
			}
		}

		snTextField.setEnabled(false);
		manufacturerLabel.setEnabled(true);
		modelNumberComboBox.setEnabled(false);
		
		int[][] iData = new int[91][2];
		
		for (int i = 0; i < iData.length; i++) {
			iData[i][0] = rssiArray[i];
			iData[i][1] = dBmArray[i];
		}
		
		if (chart != null) chartPanel.remove(chart);
		chart = new Chart(iData, "dBm", "dBm vs RSSI", "RSSI", "dBm");
	    chartPanel.add(chart, BorderLayout.CENTER);
	}

	private void configureListeners() {
		rssiTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				rssiTextField.setFont(new Font("Calabri", Font.BOLD, 12));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});
		
		rssiTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					rssiTextField.setFont(new Font("Calabri", Font.PLAIN, 12));
					cdo.updateRssiElement(Integer.parseInt(dBmComboBox.getSelectedItem().toString()), 
						Integer.parseInt(rssiTextField.getText()));
					rssiArray = cdo.getRssiArray();
					updatePanel();
			        rssiTextField.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {
	
			}
		});
		
		snTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				snTextField.setFont(new Font("Calabri", Font.BOLD, 12));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});
	
		snTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					snTextField.setFont(new Font("Calabri", Font.PLAIN, 12));
					snTextField.transferFocus();
					if (modelNumberComboBox.getSelectedItem().toString().length() > 0) buildNewCalibrationFile();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {
	
			}
		});
		
	    newFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				modelNumberComboBox.setEnabled(true);
				snTextField.setEnabled(true);
				snTextField.setText("");
				manufacturerLabel.setText(devices[0][modelNumberComboBox.getSelectedIndex()]);
			}
		});	    

		rssiSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				rssiTextField.setText(String.valueOf(rssiSetValue));
				rssiArray[dBmComboBox.getSelectedIndex()] = rssiSetValue;
				cdo.updateRssiElement(Integer.parseInt(dBmComboBox.getSelectedItem().toString()), 
					rssiSetValue);
				rssiArray = cdo.getRssiArray();
			}
		});
		
		calFileEditorComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					cdo = new CalibrationDataObject(calFileDir,
						calFileEditorComboBox.getSelectedItem().toString());
					loadDbmComboBox();
					updatePanel();
				}
			}
		});

		dBmComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					JComboBox<?> cb = (JComboBox<?>) event.getSource();
					rssiTextField.setText(String.valueOf(rssiArray[cb.getSelectedIndex()]));
				}
			}
		});
		
		modelNumberComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					JComboBox<?> cb = (JComboBox<?>) event.getSource();
					manufacturerLabel.setText(devices[0][cb.getSelectedIndex()]);
					if (snTextField.getText().length() > 0) buildNewCalibrationFile();
				}
			}
		});
    }

	public void setCurrentRSSILevel(int rssiCurrent) {
		rssiCurrentLabel.setText(String.valueOf(rssiCurrent));
		rssiSetValue = rssiCurrent;
	}

	private void initializeComponents() {
		try {
			calDir = new File(calFileDir);
			fileList = Arrays.asList(calDir.list());
			
			calibrationGraphic = new JPanel();
			calibrationGraphic.setBorder(BorderFactory.createTitledBorder("RSSI x dBm"));
			
		    chartPanel = new JPanel();
		    
		    rssiTextFieldPanel = new JPanel();
		    rssiTextFieldPanel.setFont(new Font("Calabri", Font.PLAIN, 8));
	        rssiTextFieldPanel.setBorder(BorderFactory.createTitledBorder("RSSI"));
	        
	        dBmComboBoxPanel = new JPanel();
	        dBmComboBoxPanel.setFont(new Font("Calabri", Font.PLAIN, 8));
	        dBmComboBoxPanel.setBorder(BorderFactory.createTitledBorder("dBm"));
	
			rssiTextField = new JFormattedTextField();
			rssiTextField.setFont(new Font("Calabri", Font.PLAIN, 12));
			rssiTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
			rssiTextField.setAlignmentY(Component.CENTER_ALIGNMENT);
			rssiTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			
	        fitToCurveButton = new JButton("Fit to Curve");
	        fitToCurveButton.setFont(new Font("Calabri", Font.BOLD, 12));
	        fitToCurveButton.setMultiClickThreshhold(50L);
	        
			newFileButton = new JButton("New File");
			newFileButton.setFont(new Font("Calabri", Font.BOLD, 12));
			newFileButton.setMultiClickThreshhold(50L);
			
	        rssiSetButton = new JButton("Set RSSI");
	        rssiSetButton.setFont(new Font("Calabri", Font.BOLD, 12));
	        rssiSetButton.setMultiClickThreshhold(50L);
	
	        equalSignLabel = new JLabel("=");
	        equalSignLabel.setFont(new java.awt.Font("Tahoma", 1, 18));
	        equalSignLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        equalSignLabel.setHorizontalTextPosition(SwingConstants.CENTER);
	
	        rssiCurrentLabel = new JLabel("");
	        rssiCurrentLabel.setBackground(Color.BLACK);
	        rssiCurrentLabel.setFont(new Font("Calabri", Font.PLAIN, 12));
	        rssiCurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        rssiCurrentLabel.setToolTipText("The current RSSI reported by the radio");
	        rssiCurrentLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	        
	        rssiCurrentLabelLabel = new JLabel("Measured RSSI =");
	        rssiCurrentLabelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
	        
	        snTextField = new JFormattedTextField();
	        snTextField.setFont(new Font("Calabri", Font.PLAIN, 12));
	        snTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	        snTextField.setHorizontalAlignment(SwingConstants.RIGHT);
	        snTextField.setToolTipText("Serial number of selected or new radio");
	        snTextField.setDisabledTextColor(Color.BLACK);
	        snTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
			snTextField.setAlignmentY(Component.CENTER_ALIGNMENT);
	        snTextFieldLabel = new JLabel("Serial Number");
	        snTextFieldLabel.setFont(new Font("Calabri", Font.PLAIN, 12));
	
	        manufacturerLabel = new JLabel();
	        manufacturerLabel.setFont(new Font("Calabri", Font.BOLD, 12));
	        manufacturerLabel.setForeground(Color.BLUE);
	        manufacturerLabel.setBackground(Color.LIGHT_GRAY);
	        manufacturerLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	        manufacturerLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        manufacturerLabel.setToolTipText("Manufacturer of selected or new radio");
	        manufacturerLabelLabel = new JLabel("Manufacturer");
	        manufacturerLabelLabel.setFont(new Font("Calabri", Font.PLAIN, 12));
	        
			calFileEditorComboBoxModel = new SortedComboBoxModel(fileList.toArray(new String[fileList.size()]));
			calFileEditorComboBox = new JComboBox<Object>(calFileEditorComboBoxModel);
			calFileEditorComboBox.setEditable(false);
			calFileEditorComboBox.setForeground(Color.BLACK);
			calFileEditorComboBox.setFont(new Font("Calabri", Font.BOLD, 12));
			calFileEditorComboBox.setToolTipText("Select calibration file to view or edit");
			calFileEditorComboBoxLabel = new JLabel("Cal File");
			calFileEditorComboBoxLabel.setFont(new Font("Calabri", Font.PLAIN, 12));
			
			modelNumberComboBoxModel = new SortedComboBoxModel(devices[1]);
			modelNumberComboBox = new JComboBox<Object>(modelNumberComboBoxModel);
			modelNumberComboBox.setForeground(Color.BLACK);
			modelNumberComboBox.setEditable(false);
			modelNumberComboBox.setFont(new Font("Calabri", Font.BOLD, 12));
	        modelNumberComboBox.setToolTipText("Model number of selected or new radio");
	        modelNumberComboBoxLabel = new JLabel("Model Number");
	        modelNumberComboBoxLabel.setFont(new Font("Calabri", Font.PLAIN, 12));
	
			dBmComboBox = new JComboBox<Object>(DEFAULT_DBM_VALUES);
			dBmComboBox.setEditable(false);	
			dBmComboBox.setForeground(Color.BLACK);
			dBmComboBox.setFont(new Font("Calabri", Font.BOLD, 12));
			dBmComboBox.setToolTipText("dBm reading that equates to corresponding RSSI value");	
		} catch (NullPointerException ex) {
			radioEquipmentNotConfiguredMessage("Please configure a radio in the menu settings.");
		}
	}
	
	private void radioEquipmentNotConfiguredMessage(String message) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                        message, "Radio Equipment Not Configured", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	public JPanel getCalibrationPanelGui() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
	
		panel.setLayout(layout);
		
        GroupLayout rssiTextFieldPanelLayout = new GroupLayout(rssiTextFieldPanel);
        rssiTextFieldPanel.setLayout(rssiTextFieldPanelLayout);
        
        rssiTextFieldPanelLayout.setHorizontalGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
        	.addComponent(rssiTextField, 80,80,80));
        
        rssiTextFieldPanelLayout.setVerticalGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(rssiTextFieldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rssiTextField)
                .addContainerGap()));

        GroupLayout dBmComboBoxPanelLayout = new GroupLayout(dBmComboBoxPanel);
        dBmComboBoxPanel.setLayout(dBmComboBoxPanelLayout);
        
        dBmComboBoxPanelLayout.setHorizontalGroup(dBmComboBoxPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
        	.addComponent(dBmComboBox, 80,80,80));
        
        dBmComboBoxPanelLayout.setVerticalGroup(dBmComboBoxPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(dBmComboBoxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dBmComboBox)
                .addContainerGap()));
		
        GroupLayout calibrationGraphicLayout = new GroupLayout(calibrationGraphic);
        calibrationGraphic.setLayout(calibrationGraphicLayout);
        
        calibrationGraphicLayout.setHorizontalGroup(calibrationGraphicLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel));

        calibrationGraphicLayout.setVerticalGroup(calibrationGraphicLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel));
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                    	.addComponent(rssiSetButton, 100,100,100)
                    	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rssiCurrentLabelLabel, 130,130,130)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rssiCurrentLabel, 40,40,40))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(calFileEditorComboBoxLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(calFileEditorComboBox, 240,240,240))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(fitToCurveButton, 120,120,120))
                                    .addComponent(rssiTextFieldPanel, 130,130,130))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(equalSignLabel, 30,30,30)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addComponent(newFileButton, 120,120,120)
                                    .addComponent(dBmComboBoxPanel, 130,130,130)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addComponent(snTextFieldLabel)
                                    .addComponent(manufacturerLabelLabel)
                                    .addComponent(modelNumberComboBoxLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                	.addComponent(modelNumberComboBox, 170,170,170)
                                    .addComponent(manufacturerLabel, 140,140,140)
                                    .addComponent(snTextField, 130,130,130)))
                            .addGroup(layout.createSequentialGroup()))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(calibrationGraphic)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
            		.addContainerGap()
                	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                    		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)	
                    			.addComponent(calFileEditorComboBoxLabel, 20,20,20)
                    			.addComponent(calFileEditorComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addGroup(layout.createSequentialGroup()
                                	.addPreferredGap(ComponentPlacement.UNRELATED)
                            		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            			.addComponent(snTextFieldLabel, 20,20,20)
                            			.addComponent(snTextField, 20,20,20))
                            		.addPreferredGap(ComponentPlacement.UNRELATED)
                            		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                                .addComponent(manufacturerLabel, 24,24,24)
		                                .addComponent(manufacturerLabelLabel, 20,20,20))
		                            .addPreferredGap(ComponentPlacement.UNRELATED)
		                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				                        .addComponent(modelNumberComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				                        .addComponent(modelNumberComboBoxLabel, 20,20,20))
				                    .addPreferredGap(ComponentPlacement.UNRELATED)
				                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)    
				                    .addComponent(rssiTextFieldPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)   	
				                    .addComponent(dBmComboBoxPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				                .addPreferredGap(ComponentPlacement.UNRELATED)
				                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			                    	.addComponent(rssiSetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			                    	.addComponent(rssiCurrentLabelLabel, 20,20,20)
			                    	.addComponent(rssiCurrentLabel, 20,20,20))
			                    .addPreferredGap(ComponentPlacement.UNRELATED)
			                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			                        .addComponent(fitToCurveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			                        .addComponent(newFileButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
			                .addGroup(layout.createSequentialGroup()
			                	.addGap(150,150,150)	
			                	.addComponent(equalSignLabel)
			                	.addGap(0, 0, Short.MAX_VALUE))))
			        .addComponent(calibrationGraphic, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			    .addContainerGap()));
        return panel;
	}

}
