package com;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class MapSettings extends JDialog {
	private static final long serialVersionUID = 723124187347686074L;
	
	private ButtonModel model;
	private JButton jccApply;
	private JButton jccCancel;
	private JButton applyButton;
	private JButton cancelButton;
	private JButton okButton;
	private JButton gridColorSelectorButton;
	private JLabel gridColorSelectorPreview;
	private JPanel mapSettingsPanel;
	private JPanel measurementGridPanel;
	private JPanel mapTypeButtonGroupPanel;
	private JRadioButton worldWindMapRadioButton;
	private JRadioButton openStreetMapRadioButton;
	private JRadioButton imageMapRadioButton;
	private JCheckBox showGridCheckBox;
	private JCheckBox displayShapesCheckBox;
	private ButtonGroup selectedMapTypeButtonGroup;
	private Preferences userPref;	
	private boolean showGrid;
	private boolean displayShapes;
	private int selectedMapType;
	private Color gridColor;
	private JColorChooser jcc;
	private JDialog jccDialog;
	private Toolkit tk;
	private Dimension screenSize;
	private boolean selectedMapTypeChanged = false;

	public MapSettings() {
		getSettingsFromRegistry();
		initializeComponents();
		createGraphicalUserInterface();
	}

	private void getSettingsFromRegistry() {
		userPref = Preferences.userRoot();
		selectedMapType = userPref.getInt("SelectedMapType", 0);
		showGrid = userPref.getBoolean("ShowGrid", false);
		displayShapes = userPref.getBoolean("DisplayShapes", true);
		gridColor = new Color(userPref.getInt("GridColor", Color.RED.getRGB()));
	}

	public void showSettingsDialog(boolean newShowSettingsDialog) {
		setVisible(newShowSettingsDialog);
	}

	private void initializeComponents() {
		jccDialog = new JDialog();
		
		tk = Toolkit.getDefaultToolkit();
		screenSize = tk.getScreenSize();
		
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle("Map Settings");
		
		jccApply = new JButton("Apply");
		jccCancel = new JButton("Cancel");
		applyButton = new JButton("Apply");
		cancelButton = new JButton("Cancel");
		okButton = new JButton("OK");
		gridColorSelectorButton = new JButton("Color Selector");
		imageMapRadioButton = new JRadioButton("Use Image Map Graphical Image Viewer");
		worldWindMapRadioButton = new JRadioButton("Use NASA WorldWind Topographical Map");
		openStreetMapRadioButton = new JRadioButton("Use OpenStreetMap Road Map");
		showGridCheckBox = new JCheckBox("Show Sample Grid");
		displayShapesCheckBox = new JCheckBox("Display Shapes");
		selectedMapTypeButtonGroup = new ButtonGroup();
		mapTypeButtonGroupPanel = new JPanel();
		gridColorSelectorPreview = new JLabel();
		
		jcc = new JColorChooser(gridColor);
		jcc.setPreviewPanel(new JPanel());
		AbstractColorChooserPanel[] oldPanels = jcc.getChooserPanels();
	    for (int i = 0; i < oldPanels.length; i++) {
	    	String clsName = oldPanels[i].getClass().getName();
	    	if (clsName.equals("javax.swing.colorchooser.ColorChooserPanel"))
	    		jcc.removeChooserPanel(oldPanels[i]);
	    }
	    
		jccDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		jccDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jccDialog.setTitle("Grid Color Chooser");
		
		selectedMapTypeButtonGroup.add(imageMapRadioButton);
		selectedMapTypeButtonGroup.add(worldWindMapRadioButton);
		selectedMapTypeButtonGroup.add(openStreetMapRadioButton);

		mapSettingsPanel = new JPanel();

		mapSettingsPanel.setBorder(BorderFactory.createTitledBorder("Map Settings"));
		mapTypeButtonGroupPanel.setBorder(BorderFactory.createTitledBorder("Map Type"));
		
		measurementGridPanel = new JPanel();
		
		measurementGridPanel.setBorder(BorderFactory.createTitledBorder("Grid Settings"));
		
		RadioButtonHandler rbh = new RadioButtonHandler();

		imageMapRadioButton.addItemListener(rbh);
		worldWindMapRadioButton.addItemListener(rbh);
		openStreetMapRadioButton.addItemListener(rbh);
		
		showGridCheckBox.setSelected(showGrid);
		displayShapesCheckBox.setSelected(displayShapes);
		gridColorSelectorPreview.setBackground(gridColor);
		gridColorSelectorPreview.setOpaque(true);

		switch (selectedMapType) {
			case 0:
				model = imageMapRadioButton.getModel();
				break;
			case 1:
				model = worldWindMapRadioButton.getModel();
				break;
			case 2:
				model = openStreetMapRadioButton.getModel();
				break;
		}

		selectedMapTypeButtonGroup.setSelected(model, true);

		showGridCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				showGrid = showGridCheckBox.isSelected();
			}
		});

		displayShapesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				displayShapes = displayShapesCheckBox.isSelected();
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonActionEvent(event);
			}
		});

		gridColorSelectorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jccDialog.setVisible(true);
			}
		});
		
		jccApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				gridColor = jcc.getColor();
				gridColorSelectorPreview.setBackground(gridColor);
				jccDialog.setVisible(false);
			}
		});
		
		jccCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jccDialog.setVisible(false);
			}
		});
	}

	private class RadioButtonHandler implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent ie) {
			int oldMapType = selectedMapType;
			if (ie.getSource() == imageMapRadioButton)
				selectedMapType = 0;
			else if (ie.getSource() == worldWindMapRadioButton)
				selectedMapType = 1;
			else if (ie.getSource() == openStreetMapRadioButton)
				selectedMapType = 2;
			if (selectedMapType != oldMapType) selectedMapTypeChanged = true;
		}
	}

	private void createGraphicalUserInterface() {

        GroupLayout mapTypeButtonGroupPanelLayout = new GroupLayout(mapTypeButtonGroupPanel);
        
        mapTypeButtonGroupPanel.setLayout(mapTypeButtonGroupPanelLayout);
        
        mapTypeButtonGroupPanelLayout.setHorizontalGroup(
            mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(mapTypeButtonGroupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(imageMapRadioButton)
                    .add(worldWindMapRadioButton)
                    .add(openStreetMapRadioButton))
                .addContainerGap(31, Short.MAX_VALUE)));
        
        mapTypeButtonGroupPanelLayout.setVerticalGroup(
            mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(mapTypeButtonGroupPanelLayout.createSequentialGroup()
                .add(imageMapRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(worldWindMapRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(openStreetMapRadioButton)
                .addContainerGap(144, Short.MAX_VALUE)));
        
        GroupLayout measurementGridPanelLayout = new GroupLayout(measurementGridPanel);
        
        measurementGridPanel.setLayout(measurementGridPanelLayout);
        
        measurementGridPanelLayout.setHorizontalGroup(
            measurementGridPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(measurementGridPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(measurementGridPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(measurementGridPanelLayout.createSequentialGroup()
                        .add(showGridCheckBox, GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(measurementGridPanelLayout.createSequentialGroup()
                        .add(gridColorSelectorButton, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                        .add(gridColorSelectorPreview, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap()));
        
        measurementGridPanelLayout.setVerticalGroup(
            measurementGridPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(measurementGridPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(showGridCheckBox)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(measurementGridPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(gridColorSelectorButton)
                    .add(gridColorSelectorPreview, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        GroupLayout mapSettingsPanelLayout = new GroupLayout(mapSettingsPanel);
        
        mapSettingsPanel.setLayout(mapSettingsPanelLayout);
        
        mapSettingsPanelLayout.setHorizontalGroup(
            mapSettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(mapSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mapTypeButtonGroupPanel, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(measurementGridPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap()));
        
        mapSettingsPanelLayout.setVerticalGroup(
            mapSettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(mapSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mapSettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(measurementGridPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(mapTypeButtonGroupPanel, GroupLayout.PREFERRED_SIZE, 236, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE)));
        
        GroupLayout layout = new GroupLayout(getContentPane());
        
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(okButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(applyButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(cancelButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                    .add(GroupLayout.TRAILING, mapSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap()));
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(mapSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(applyButton)
                    .add(okButton))
                .addContainerGap()));

		pack();
		
		setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
		
		javax.swing.GroupLayout jccDialogLayout = new javax.swing.GroupLayout(jccDialog.getContentPane());
        
		jccDialog.getContentPane().setLayout(jccDialogLayout);
        
		jccDialogLayout.setHorizontalGroup(
	            jccDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jccDialogLayout.createSequentialGroup()
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jccApply, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(jccCancel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addGap(134, 134, 134))
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );
	        jccDialogLayout.setVerticalGroup(
	            jccDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jccDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jccApply)
	                    .addComponent(jccCancel))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

		jccDialog.pack();
		
		jccDialog.setLocation((screenSize.width / 2) - (jccDialog.getWidth() / 2),
				(screenSize.height / 2) - (jccDialog.getHeight() / 2));
	}

	private void applyButtonActionEvent(ActionEvent event) {
		if (selectedMapTypeChanged) firePropertyChange("NEW_MAP_TYPE", null, selectedMapType);
		selectedMapTypeChanged = false;
		userPref.putInt("SelectedMapType", selectedMapType);
		userPref.putBoolean("ShowGrid", showGrid);
		userPref.putInt("GridColor", gridColor.getRGB());
		userPref.putBoolean("DisplayShapes", displayShapes);
		firePropertyChange("PROPERTY_CHANGED", null, null);
		setVisible(false);
	}

	public void setMapType(int newMapType) {
		selectedMapType = newMapType;
	}
	
	public int getMapType() {
		return selectedMapType;
	}

	public boolean isShowGrid() {
		return showGrid;
	}
	
	public Color getGridColor() {
		return gridColor;
	}
	
	public boolean isDisplayShapes() {
		return displayShapes;
	}

}
