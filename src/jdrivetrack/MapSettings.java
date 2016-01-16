package jdrivetrack;

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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout;
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
	private JCheckBox showStatusBarCheckBox;
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
	private boolean showStatusBar;

	public MapSettings() {
		getSettingsFromRegistry();
		initializeComponents();
		createGraphicalUserInterface();
	}

	private void getSettingsFromRegistry() {
		userPref = Preferences.userRoot().node(this.getClass().getName());
		selectedMapType = userPref.getInt("SelectedMapType", 0);
		showGrid = userPref.getBoolean("ShowGrid", false);
		displayShapes = userPref.getBoolean("DisplayShapes", true);
		gridColor = new Color(userPref.getInt("GridColor", Color.RED.getRGB()));
		showStatusBar = userPref.getBoolean("ShowStatusBar", true);
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
		jccApply.setMultiClickThreshhold(50L);
		
		jccCancel = new JButton("Cancel");
		jccCancel.setMultiClickThreshhold(50L);
		
		applyButton = new JButton("Apply");
		applyButton.setMultiClickThreshhold(50L);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMultiClickThreshhold(50L);
		
		okButton = new JButton("OK");
		okButton.setMultiClickThreshhold(50L);
		
		gridColorSelectorButton = new JButton("Color Selector");
		gridColorSelectorButton.setMultiClickThreshhold(50L);
		
		imageMapRadioButton = new JRadioButton("Use Image Map Graphical Image Viewer");
		worldWindMapRadioButton = new JRadioButton("Use NASA WorldWind Topographical Map");
		openStreetMapRadioButton = new JRadioButton("Use OpenStreetMap Road Map");
		showGridCheckBox = new JCheckBox(" Show Sample Grid");
		displayShapesCheckBox = new JCheckBox("Display Shapes");
		showStatusBarCheckBox = new JCheckBox("Show Status Bar");
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
		
		showStatusBarCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				showStatusBar = showStatusBarCheckBox.isSelected();
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
        
        mapTypeButtonGroupPanelLayout.setHorizontalGroup(mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapTypeButtonGroupPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(imageMapRadioButton)
                    .addComponent(worldWindMapRadioButton)
                    .addComponent(openStreetMapRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        mapTypeButtonGroupPanelLayout.setVerticalGroup(mapTypeButtonGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapTypeButtonGroupPanelLayout.createSequentialGroup()
                .addComponent(imageMapRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(worldWindMapRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openStreetMapRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout measurementGridPanelLayout = new GroupLayout(measurementGridPanel);
        measurementGridPanel.setLayout(measurementGridPanelLayout);
        
        measurementGridPanelLayout.setHorizontalGroup(measurementGridPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(measurementGridPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(measurementGridPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(measurementGridPanelLayout.createSequentialGroup()
                        .addComponent(showGridCheckBox))
                    .addGroup(measurementGridPanelLayout.createSequentialGroup()
                        .addComponent(gridColorSelectorButton,150,150,150)
                        .addGap(30,30,30)
                        .addComponent(gridColorSelectorPreview,45,45,45)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        measurementGridPanelLayout.setVerticalGroup(measurementGridPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(measurementGridPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(showGridCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(measurementGridPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(gridColorSelectorButton)
                    .addComponent(gridColorSelectorPreview,23,23,23))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        GroupLayout mapSettingsPanelLayout = new GroupLayout(mapSettingsPanel);
        mapSettingsPanel.setLayout(mapSettingsPanelLayout);
        
        mapSettingsPanelLayout.setHorizontalGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mapTypeButtonGroupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(measurementGridPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        mapSettingsPanelLayout.setVerticalGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(measurementGridPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(mapTypeButtonGroupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton,90,90,90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applyButton,90,90,90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton,90,90,90))
                    .addComponent(mapSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mapSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(applyButton)
                    .addComponent(okButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
		
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
		
		GroupLayout jccDialogLayout = new GroupLayout(jccDialog.getContentPane());
		jccDialog.getContentPane().setLayout(jccDialogLayout);
        
		jccDialogLayout.setHorizontalGroup(jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(GroupLayout.Alignment.TRAILING, jccDialogLayout.createSequentialGroup()
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jccApply, 90,90,90)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jccCancel,90,90,90)
	                .addGap(134, 134, 134))
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc,420,420,420)
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
	        jccDialogLayout.setVerticalGroup(
	            jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jcc, 150,150,150)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jccDialogLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(jccApply)
	                    .addComponent(jccCancel))
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
		userPref.putBoolean("ShowStatusBar", showStatusBar);
		firePropertyChange("PROPERTY_CHANGED", null, null);
	}

	public void setMapType(int newMapType) {
		selectedMapType = newMapType;
		userPref.putInt("SelectedMapType", selectedMapType);
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
