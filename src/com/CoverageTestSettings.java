package com;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;

import javax.swing.colorchooser.AbstractColorChooserPanel;

public class CoverageTestSettings extends JDialog {
	private static final long serialVersionUID = -8082024979568837913L;
	
	private JPanel coverageTestPanel;
	private JPanel displaySettingsPanel;
	private JPanel manualModePanel;
	private JPanel sampleDistributionAcrossTilePanel;
	private JPanel signalQualityDisplaySelectorPanel;
	private JPanel manualDataCollectionModeSelectorPanel;
	private JPanel samplingOptionsPanel;
	private JPanel earthShapeModelPanel;
    private JPanel RSSIDataPointColoringPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	private Preferences userPref;
	private JCheckBox alertOnMinimumSamplesPerTileAcquiredCheckBox;
	private JCheckBox showGridSquareShadingCheckBox;
	private JCheckBox sampleRSSICheckBox;
	private JCheckBox sampleBERCheckBox;
	private JCheckBox sampleSINADCheckBox;
	private JComboBox<?> dotsPerTileComboBox;
	private JLabel dotsPerTileComboBoxLabel;
	private JComboBox<?> maxSamplesPerTileComboBox;
	private JLabel maxSamplesPerTileComboBoxLabel;
	private JComboBox<?> minSamplesPerTileComboBox;
	private JLabel minSamplesPerTileComboBoxLabel;
	private JComboBox<?> tileSizeArcSecondsComboBox;
	private JLabel tileSizeArcSecondsComboBoxLabel;
	private ButtonGroup sampleDistributionTimingRadioButtonGroup;
	private JRadioButton timingSetBySpeedRadioButton;
	private JRadioButton timingEvenAcrossTileRadioButton;
	private JRadioButton timingFrontWeightedRadioButton;
	private ButtonGroup signalQualityDisplaySelectorButtonGroup;
	private JRadioButton displaySinadRadioButton;
	private JRadioButton displayRssiRadioButton;
	private JRadioButton displayBerRadioButton;
	private ButtonGroup manualDataCollectionModeRadioButtonGroup;
	private JRadioButton manualDataCollectionContinuousModeRadioButton;
	private JRadioButton manualDataCollectionManualModeRadioButton;
	private ButtonGroup earthShapeModelRadioButtonGroup;
	private JRadioButton earthShapeModelFlatRadioButton;
	private JRadioButton earthShapeModelEllipseRadioButton;
	private int dotsPerTile;
	private Point.Double tileSizeArcSeconds;
	private int minSamplesPerTile;
	private int maxSamplesPerTile;
	private int dotsPerTileIndex;
	private int earthShapeModelIndex = 0;
	private int tileSizeIndex;
	private int minSamplesPerTileIndex;
	private int maxSamplesPerTileIndex;
	private boolean alertOnMinimumSamplesPerTileAcquired;
	private boolean showGridSquareShading;
	private boolean sampleRSSI;
	private boolean sampleBER;
	private boolean sampleSINAD;
	private int sampleTimingMode;
	private ButtonModel sampleTimingModeModel;
	private int manualDataCollectionMode;
	private ButtonModel manualDataCollectionModeModel;
	private int signalQualityDisplayMode;
	private ButtonModel signalQualityDisplayModeModel;
	private ButtonModel earthShapeModel;
    private JTextField jTextField100dBm;
    private JTextField jTextField110dBm;
    private JTextField jTextField120dBm;
    private JTextField jTextField50dBm;
    private JTextField jTextField60dBm;
    private JTextField jTextField70dBm;
    private JTextField jTextField80dBm;
    private JTextField jTextField90dBm;
    private JLabel jLabel100dBm;
    private JLabel jLabel110dBm;
    private JLabel jLabel120dBm;
    private JLabel jLabel50dBm;
    private JLabel jLabel60dBm;
    private JLabel jLabel70dBm;
    private JLabel jLabel80dBm;
    private JLabel jLabel90dBm;
    private Color color50dBm = new Color(0,255,0);
    private Color color60dBm = new Color(0,255,0);
    private Color color70dBm = new Color(0,255,0);
    private Color color80dBm = new Color(255,255,0);
    private Color color90dBm = new Color(255,255,0);
    private Color color100dBm = new Color(255,255,0);
    private Color color110dBm = new Color(255,0,0);
    private Color color120dBm = new Color(255,0,0);
    private JTextField jTextField0sinad;
    private JTextField jTextField5sinad;
    private JTextField jTextField10sinad;
    private JTextField jTextField12sinad;
    private JTextField jTextField15sinad;
    private JTextField jTextField20sinad;
    private JTextField jTextField25sinad;
    private JTextField jTextField30sinad;
    private JLabel jLabel0sinad;
    private JLabel jLabel5sinad;
    private JLabel jLabel10sinad;
    private JLabel jLabel12sinad;
    private JLabel jLabel15sinad;
    private JLabel jLabel20sinad;
    private JLabel jLabel25sinad;
    private JLabel jLabel30sinad;
    private JPanel SINADDataPointColoringPanel;
    private Color color0sinad = new Color(0,255,0);
    private Color color5sinad = new Color(0,255,0);
    private Color color10sinad = new Color(0,255,0);
    private Color color12sinad = new Color(255,255,0);
    private Color color15sinad = new Color(255,255,0);
    private Color color20sinad = new Color(255,255,0);
    private Color color25sinad = new Color(255,0,0);
    private Color color30sinad = new Color(255,0,0);
    private JTextField jTextField0ber;
    private JTextField jTextField5ber;
    private JTextField jTextField10ber;
    private JTextField jTextField15ber;
    private JTextField jTextField20ber;
    private JTextField jTextField25ber;
    private JTextField jTextField30ber;
    private JTextField jTextField35ber;
    private JLabel jLabel0ber;
    private JLabel jLabel5ber;
    private JLabel jLabel10ber;
    private JLabel jLabel15ber;
    private JLabel jLabel20ber;
    private JLabel jLabel25ber;
    private JLabel jLabel30ber;
    private JLabel jLabel35ber;
    private JPanel BERDataPointColoringPanel;
    private Color color0ber = new Color(0,255,0);
    private Color color5ber = new Color(0,255,0);
    private Color color10ber = new Color(0,255,0);
    private Color color15ber = new Color(255,255,0);
    private Color color20ber = new Color(255,255,0);
    private Color color25ber = new Color(255,255,0);
    private Color color30ber = new Color(255,0,0);
    private Color color35ber = new Color(255,0,0);
    private JColorChooser jcc;
	private JDialog jccDialog;
	private JButton jccApply;
	private JButton jccCancel;
	private int colorIndex;
	private int minTimePerTile = 0;

	public CoverageTestSettings() {
		getSettingsFromRegistry();
		initializeComponents();
		drawGraphicalUserInterface();
	}

	private void initializeComponents() {
		
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		jccDialog = new JDialog();
		jccApply = new JButton("Apply");
		jccCancel = new JButton("Cancel");
		
		jcc = new JColorChooser(Color.RED);
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

		String[] tileSizeArcSecondss = new String[] { "10 Feet of Latitude", "20 Feet of Latitude", 
				"40 Feet of Latitude", "50 Feet of Latitude", 
				"100 Feet of Latitude", "1/4 Mile of Latitude", "1/2 Mile of Latitude", 
				"1 Second", "3 Seconds", "5 Seconds", "10 Seconds", "15 Seconds", 
				"30 Seconds", "60 Seconds", "90 Seconds", 
				"120 Seconds", "180 Seconds" };
		
		String[] maxSamplesPerTile = new String[] { "5", "10", "15", "20",
				"25", "30", "60", "90", "120", "180", "240", "300", "360", "420", "480", "540", "600", "900", "1200", "1800", "2700", "3600", "Unlimited"};
		
		String[] dotsPerTile = new String[] { "1", "3", "5", "10", "15", "30", "50", "100", "200", "500", "1000", "Continuous"};
		
		String[] minSamplesPerTile = new String[] { "1", "5", "10", "15", "20",
				"25", "30", "60", "90", "120", "180", "240", "300", "360", "420", "480", "540", "600", "900", "1200", "1800", "2700", "3600", "1 Minute", "2.5 Minutes", "3 Minutes", "5 Minutes"};
		
		tileSizeArcSeconds = new Point.Double();
		earthShapeModelPanel = new JPanel();
		coverageTestPanel = new JPanel();
		displaySettingsPanel = new JPanel();
		manualModePanel = new JPanel();
		tabbedPane = new JTabbedPane();
		tileSizeArcSecondsComboBox = new JComboBox<Object>(tileSizeArcSecondss);
		tileSizeArcSecondsComboBoxLabel = new JLabel();
		maxSamplesPerTileComboBoxLabel = new JLabel();
		maxSamplesPerTileComboBox = new JComboBox<Object>(maxSamplesPerTile);
		dotsPerTileComboBoxLabel = new JLabel();
		dotsPerTileComboBox = new JComboBox<Object>(dotsPerTile);
		minSamplesPerTileComboBoxLabel = new JLabel();
		minSamplesPerTileComboBox = new JComboBox<Object>(minSamplesPerTile);
		alertOnMinimumSamplesPerTileAcquiredCheckBox = new JCheckBox();
		showGridSquareShadingCheckBox = new JCheckBox();
		sampleRSSICheckBox = new JCheckBox();
		sampleBERCheckBox = new JCheckBox();
		sampleSINADCheckBox = new JCheckBox();
		timingSetBySpeedRadioButton = new JRadioButton();
		timingEvenAcrossTileRadioButton = new JRadioButton();
		timingFrontWeightedRadioButton = new JRadioButton();
		sampleDistributionTimingRadioButtonGroup = new ButtonGroup();
		sampleDistributionAcrossTilePanel = new JPanel();
		signalQualityDisplaySelectorPanel = new JPanel();
		samplingOptionsPanel = new JPanel();
		manualDataCollectionModeSelectorPanel = new JPanel();
		signalQualityDisplaySelectorButtonGroup = new ButtonGroup();
		displaySinadRadioButton = new JRadioButton();
		displayRssiRadioButton = new JRadioButton();
		displayBerRadioButton = new JRadioButton();
		manualDataCollectionModeRadioButtonGroup = new ButtonGroup();
		manualDataCollectionContinuousModeRadioButton = new JRadioButton();
		manualDataCollectionManualModeRadioButton = new JRadioButton();
		earthShapeModelRadioButtonGroup = new ButtonGroup();
		earthShapeModelFlatRadioButton = new JRadioButton();
		earthShapeModelEllipseRadioButton = new JRadioButton();
		jLabel50dBm = new JLabel();
        jTextField50dBm = new JTextField();
        jLabel60dBm = new JLabel();
        jTextField60dBm = new JTextField();
        jLabel70dBm = new JLabel();
        jTextField70dBm = new JTextField();
        jLabel80dBm = new JLabel();
        jTextField80dBm = new JTextField();
        jLabel90dBm = new JLabel();
        jTextField90dBm = new JTextField();
        jLabel100dBm = new JLabel();
        jTextField100dBm = new JTextField();
        jLabel110dBm = new JLabel();
        jTextField110dBm = new JTextField();
        jLabel120dBm = new JLabel();
        jTextField120dBm = new JTextField();
        RSSIDataPointColoringPanel = new JPanel();
        jTextField50dBm.setBackground(color50dBm);
        jTextField60dBm.setBackground(color60dBm);
        jTextField70dBm.setBackground(color70dBm);
        jTextField80dBm.setBackground(color80dBm);
        jTextField90dBm.setBackground(color90dBm);
        jTextField100dBm.setBackground(color100dBm);
        jTextField110dBm.setBackground(color110dBm);
        jTextField120dBm.setBackground(color120dBm);
        jTextField50dBm.setEditable(false);
        jTextField50dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField60dBm.setEditable(false);
        jTextField60dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField70dBm.setEditable(false);
        jTextField70dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField80dBm.setEditable(false);
        jTextField80dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField90dBm.setEditable(false);
        jTextField90dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField100dBm.setEditable(false);
        jTextField100dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField110dBm.setEditable(false);
        jTextField110dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField120dBm.setEditable(false);
        jTextField120dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jLabel0sinad = new JLabel();
        jTextField0sinad = new JTextField();
        jLabel5sinad = new JLabel();
        jTextField5sinad = new JTextField();
        jLabel10sinad = new JLabel();
        jTextField10sinad = new JTextField();
        jLabel12sinad = new JLabel();
        jTextField12sinad = new JTextField();
        jLabel15sinad = new JLabel();
        jTextField15sinad = new JTextField();
        jLabel20sinad = new JLabel();
        jTextField20sinad = new JTextField();
        jLabel25sinad = new JLabel();
        jTextField25sinad = new JTextField();
        jLabel30sinad = new JLabel();
        jTextField30sinad = new JTextField();
        SINADDataPointColoringPanel = new JPanel();
        jTextField0sinad.setBackground(color0sinad);
        jTextField5sinad.setBackground(color5sinad);
        jTextField10sinad.setBackground(color10sinad);
        jTextField12sinad.setBackground(color12sinad);
        jTextField15sinad.setBackground(color15sinad);
        jTextField20sinad.setBackground(color20sinad);
        jTextField25sinad.setBackground(color25sinad);
        jTextField30sinad.setBackground(color30sinad);
        jTextField0sinad.setEditable(false);
        jTextField0sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField5sinad.setEditable(false);
        jTextField5sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField10sinad.setEditable(false);
        jTextField10sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField12sinad.setEditable(false);
        jTextField12sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField15sinad.setEditable(false);
        jTextField15sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField20sinad.setEditable(false);
        jTextField20sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField25sinad.setEditable(false);
        jTextField25sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField30sinad.setEditable(false);
        jTextField30sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jLabel0ber = new JLabel();
        jTextField0ber = new JTextField();
        jLabel5ber = new JLabel();
        jTextField5ber = new JTextField();
        jLabel10ber = new JLabel();
        jTextField10ber = new JTextField();
        jLabel35ber = new JLabel();
        jTextField35ber = new JTextField();
        jLabel15ber = new JLabel();
        jTextField15ber = new JTextField();
        jLabel20ber = new JLabel();
        jTextField20ber = new JTextField();
        jLabel25ber = new JLabel();
        jTextField25ber = new JTextField();
        jLabel30ber = new JLabel();
        jTextField30ber = new JTextField();
        BERDataPointColoringPanel = new JPanel();
        jTextField0ber.setBackground(color0ber);
        jTextField5ber.setBackground(color5ber);
        jTextField10ber.setBackground(color10ber);
        jTextField35ber.setBackground(color35ber);
        jTextField15ber.setBackground(color15ber);
        jTextField20ber.setBackground(color20ber);
        jTextField25ber.setBackground(color25ber);
        jTextField30ber.setBackground(color30ber);
        jTextField0ber.setEditable(false);
        jTextField0ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField5ber.setEditable(false);
        jTextField5ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField10ber.setEditable(false);
        jTextField10ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField35ber.setEditable(false);
        jTextField35ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField15ber.setEditable(false);
        jTextField15ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField20ber.setEditable(false);
        jTextField20ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField25ber.setEditable(false);
        jTextField25ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField30ber.setEditable(false);
        jTextField30ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		applyButton = new JButton("Apply");

		setTitle("Coverage Test Settings");

		tabbedPane.addTab(" Coverage Test Settings ", null, coverageTestPanel, null);
		tabbedPane.addTab(" Display Settings ", null, displaySettingsPanel, null);
		tabbedPane.addTab(" Manual Mode Settings", null, manualModePanel, null);
		
		tileSizeArcSecondsComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		tileSizeArcSecondsComboBoxLabel.setText("Size of Tile");

		maxSamplesPerTileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		maxSamplesPerTileComboBoxLabel.setText("Max Samples per Tile");

		dotsPerTileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dotsPerTileComboBoxLabel.setText("Dots per Tile");

		minSamplesPerTileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		minSamplesPerTileComboBoxLabel.setText("Min Samples per Tile");

		alertOnMinimumSamplesPerTileAcquiredCheckBox.setText("Alert on Minimum Samples per Tile Acqured");
		showGridSquareShadingCheckBox.setText("Show Multi-Colored Shading Over Grid Squares");
		sampleRSSICheckBox.setText("Sample RSSI Values");
		sampleBERCheckBox.setText("Sample Bit Error Rate");
		sampleSINADCheckBox.setText("Sample SINAD Values");
		
		dotsPerTileComboBox.setSelectedIndex(dotsPerTileIndex);
		
		tileSizeArcSecondsComboBox.setSelectedIndex(tileSizeIndex);
		
		minSamplesPerTileComboBox.setSelectedIndex(minSamplesPerTileIndex);
		
		maxSamplesPerTileComboBox.setSelectedIndex(maxSamplesPerTileIndex);
		
		alertOnMinimumSamplesPerTileAcquiredCheckBox.setSelected(alertOnMinimumSamplesPerTileAcquired);
		
		showGridSquareShadingCheckBox.setSelected(showGridSquareShading);
		
		sampleRSSICheckBox.setSelected(sampleRSSI);
		sampleBERCheckBox.setSelected(sampleBER);
		sampleSINADCheckBox.setSelected(sampleSINAD);
		
		setValues();
		
		sampleDistributionAcrossTilePanel.setBorder(BorderFactory.createTitledBorder
				("Sample Distribution Across Tile"));
		
		signalQualityDisplaySelectorPanel.setBorder(BorderFactory.createTitledBorder
				("Signal Quality Display"));
		
		manualDataCollectionModeSelectorPanel.setBorder(BorderFactory.createTitledBorder
				("Manual Data Collection Mode"));
		
		samplingOptionsPanel.setBorder(BorderFactory.createTitledBorder
				("Sampling Options"));

		timingSetBySpeedRadioButton.setText
			("Even Across Tile - Measurement Timing Based on Speed Only  ");
		
		earthShapeModelPanel.setBorder(BorderFactory.createTitledBorder
				("Earth Shape Model"));
		
		timingEvenAcrossTileRadioButton.setText
			("Even Across Tile - Measurement Timing Based on Speed and Size of Tile  ");
		
		timingFrontWeightedRadioButton.setText
			("Front Weighted - Measurement Timing Fixed at 200 mS");
		
	    displaySinadRadioButton.setText
	    	("Display Dot Color Based on SINAD Mesurement");
	    
	    displayRssiRadioButton.setText
	    	("Display Dot Color Based on RSSI");
	    
	    displayBerRadioButton.setText
    		("Display Dot Color Based on Bit Error Rate");

	    earthShapeModelFlatRadioButton.setText
    		("Flat Earth Shape Model");
	    
	    earthShapeModelEllipseRadioButton.setText
			("Elliptical Earth Shape Model");
	    
	    manualDataCollectionContinuousModeRadioButton.setText
	    	("Continuous Rate Data Sample Collection");
	    
	    manualDataCollectionManualModeRadioButton.setText
	    	("Data Sample Collection on Mouse Button Press");
	    
	    RSSIDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("RSSI Data Point Coloring"));

        jLabel50dBm.setText("-50 dBm -> -59 dBm");
        jLabel60dBm.setText("-60 dBm -> -69 dBm");
        jLabel70dBm.setText("-70 dBm -> -79 dBm");
        jLabel80dBm.setText("-80 dBm -> -89 dBm");
        jLabel90dBm.setText("-90 dBm -> -99 dBm");
        jLabel100dBm.setText("-100 dBm -> -109 dBm");
        jLabel110dBm.setText("-110 dBm -> -119 dBm");
        jLabel120dBm.setText("-120 dBm -> -129 dBm");
        
        SINADDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("SINAD Data Point Coloring"));

        jLabel0sinad.setText("0 dB -> 4 dB");
        jLabel5sinad.setText("5 dB -> 9 dB");
        jLabel10sinad.setText("10 dB -> 11 dB");
        jLabel12sinad.setText("12 dB -> 14 dB");
        jLabel15sinad.setText("15 dB -> 19 dB");
        jLabel20sinad.setText("20 dB -> 24 dB");
        jLabel25sinad.setText("25 dB -> 29 dB");
        jLabel30sinad.setText("30 dB -> 34 dB");
        
        BERDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("BER Data Point Coloring"));

        jLabel0ber.setText("0 % -> 4 %");
        jLabel5ber.setText("5 % -> 9 %");
        jLabel10ber.setText("10 % -> 14 %");
        jLabel15ber.setText("15 % -> 19 %");
        jLabel20ber.setText("20 % -> 24 %");
        jLabel25ber.setText("25 % -> 29 %");
        jLabel30ber.setText("30 % -> 34 %");
        jLabel35ber.setText("35 % -> 39 %");
	    
		RadioButtonHandler rbh = new RadioButtonHandler();

		sampleDistributionTimingRadioButtonGroup.add(timingSetBySpeedRadioButton);
		sampleDistributionTimingRadioButtonGroup.add(timingEvenAcrossTileRadioButton);
		sampleDistributionTimingRadioButtonGroup.add(timingFrontWeightedRadioButton);
		
		signalQualityDisplaySelectorButtonGroup.add(displaySinadRadioButton);
		signalQualityDisplaySelectorButtonGroup.add(displayRssiRadioButton);
		signalQualityDisplaySelectorButtonGroup.add(displayBerRadioButton);
		
		manualDataCollectionModeRadioButtonGroup.add(manualDataCollectionContinuousModeRadioButton);
		manualDataCollectionModeRadioButtonGroup.add(manualDataCollectionManualModeRadioButton);
		
		earthShapeModelRadioButtonGroup.add(earthShapeModelFlatRadioButton);
		earthShapeModelRadioButtonGroup.add(earthShapeModelEllipseRadioButton);
		earthShapeModelFlatRadioButton.addItemListener(rbh);
		earthShapeModelEllipseRadioButton.addItemListener(rbh);
		
		timingSetBySpeedRadioButton.addItemListener(rbh);
		timingEvenAcrossTileRadioButton.addItemListener(rbh);
		timingFrontWeightedRadioButton.addItemListener(rbh);
		
		manualDataCollectionContinuousModeRadioButton.addItemListener(rbh);
		manualDataCollectionManualModeRadioButton.addItemListener(rbh);
		
		displaySinadRadioButton.addItemListener(rbh);
		displayRssiRadioButton.addItemListener(rbh);
		displayBerRadioButton.addItemListener(rbh);

		switch (earthShapeModelIndex) {
			case 0:
				earthShapeModel = earthShapeModelFlatRadioButton.getModel();
				break;
			case 1:
				earthShapeModel = earthShapeModelEllipseRadioButton.getModel();
				break;
		}
		
		switch (sampleTimingMode) {
			case 0:
				sampleTimingModeModel = timingSetBySpeedRadioButton.getModel();
				break;
			case 1:
				sampleTimingModeModel = timingEvenAcrossTileRadioButton.getModel();
				break;
			case 2:
				sampleTimingModeModel = timingFrontWeightedRadioButton.getModel();
				break;
		}

		switch (signalQualityDisplayMode) {
			case 0:
				signalQualityDisplayModeModel = displaySinadRadioButton.getModel();
				break;
			case 1:
				signalQualityDisplayModeModel = displayRssiRadioButton.getModel();
				break;
			case 2:
				signalQualityDisplayModeModel = displayBerRadioButton.getModel();
				break;	
		}

		switch (manualDataCollectionMode) {
			case 0:
				manualDataCollectionModeModel = manualDataCollectionContinuousModeRadioButton.getModel();
				break;
			case 1:
				manualDataCollectionModeModel = manualDataCollectionManualModeRadioButton.getModel();
				break;
		}
		
		sampleDistributionTimingRadioButtonGroup.setSelected(sampleTimingModeModel, true);
		signalQualityDisplaySelectorButtonGroup.setSelected(signalQualityDisplayModeModel, true);
		manualDataCollectionModeRadioButtonGroup.setSelected(manualDataCollectionModeModel, true);
		earthShapeModelRadioButtonGroup.setSelected(earthShapeModel, true);
		
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
				applyButtonActionListenerEvent(event);
			}
		});

		tileSizeArcSecondsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				tileSizeArcSecondsComboBoxActionPerformed(event);
			}
		});

		maxSamplesPerTileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				maxSamplesPerTileComboBoxActionPerformed(event);
			}
		});

		dotsPerTileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dotsPerTileComboBoxActionPerformed(event);
			}
		});

		minSamplesPerTileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				minSamplesPerTileComboBoxActionPerformed(event);
			}
		});

		alertOnMinimumSamplesPerTileAcquiredCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				alertOnMinimumSamplesPerTileAcquiredCheckBoxItemStateChanged(event);
			}
		});
		
		showGridSquareShadingCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				showGridSquareShadingCheckBoxItemStateChanged(event);
			}
		});
		
		sampleRSSICheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				sampleRSSICheckBoxItemStateChanged(event);
			}
		});
		
		sampleSINADCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				sampleSINADCheckBoxItemStateChanged(event);
			}
		});
		
		sampleBERCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				sampleBERCheckBoxItemStateChanged(event);
			}
		});
		
		jTextField50dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField50dBmMouseClicked(evt);
            }
        });
		
		jTextField60dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField60dBmMouseClicked(evt);
            }
        });
		
		jTextField70dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField70dBmMouseClicked(evt);
            }
        });
		
		jTextField80dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField80dBmMouseClicked(evt);
            }
        });
		
		jTextField90dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField90dBmMouseClicked(evt);
            }
        });
		
		jTextField100dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField100dBmMouseClicked(evt);
            }
        });
		
		jTextField110dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField110dBmMouseClicked(evt);
            }
        });
		
		jTextField120dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField120dBmMouseClicked(evt);
            }
        });
		
		jTextField0sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField0sinadMouseClicked(evt);
            }
        });
		
		jTextField5sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField5sinadMouseClicked(evt);
            }
        });
		
		jTextField10sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField10sinadMouseClicked(evt);
            }
        });
		
		jTextField12sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField12sinadMouseClicked(evt);
            }
        });
		
		jTextField15sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField15sinadMouseClicked(evt);
            }
        });
		
		jTextField20sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField20sinadMouseClicked(evt);
            }
        });
		
		jTextField25sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField25sinadMouseClicked(evt);
            }
        });
		
		jTextField30sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField30sinadMouseClicked(evt);
            }
        });
		
		jTextField0ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField0berMouseClicked(evt);
            }
        });
		
		jTextField5ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField5berMouseClicked(evt);
            }
        });
		
		jTextField10ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField10berMouseClicked(evt);
            }
        });
		
		jTextField35ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField35berMouseClicked(evt);
            }
        });
		
		jTextField15ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField15berMouseClicked(evt);
            }
        });
		
		jTextField20ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField20berMouseClicked(evt);
            }
        });
		
		jTextField25ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField25berMouseClicked(evt);
            }
        });
		
		jTextField30ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField30berMouseClicked(evt);
            }
        });
		
		jccApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Color selectedColor = jcc.getColor();
				jccDialog.setVisible(false);
				switch (colorIndex) {
				case 0:
					jTextField50dBm.setBackground(selectedColor);
					color50dBm = selectedColor;
					break;
				case 1:
					jTextField60dBm.setBackground(selectedColor);
					color60dBm = selectedColor;
					break;
				case 2:
					jTextField70dBm.setBackground(selectedColor);
					color70dBm = selectedColor;
					break;
				case 3:
					jTextField80dBm.setBackground(selectedColor);
					color80dBm = selectedColor;
					break;
				case 4:
					jTextField90dBm.setBackground(selectedColor);
					color90dBm = selectedColor;
					break;
				case 5:
					jTextField100dBm.setBackground(selectedColor);
					color100dBm = selectedColor;
					break;
				case 6:
					jTextField110dBm.setBackground(selectedColor);
					color110dBm = selectedColor;
					break;
				case 7:
					jTextField120dBm.setBackground(selectedColor);
					color120dBm = selectedColor;
					break;
				case 8:
					jTextField0sinad.setBackground(selectedColor);
					color0sinad = selectedColor;
					break;
				case 9:
					jTextField5sinad.setBackground(selectedColor);
					color5sinad = selectedColor;
					break;
				case 10:
					jTextField10sinad.setBackground(selectedColor);
					color10sinad = selectedColor;
					break;
				case 11:
					jTextField12sinad.setBackground(selectedColor);
					color12sinad = selectedColor;
					break;
				case 12:
					jTextField15sinad.setBackground(selectedColor);
					color15sinad = selectedColor;
					break;
				case 13:
					jTextField20sinad.setBackground(selectedColor);
					color20sinad = selectedColor;
					break;
				case 14:
					jTextField25sinad.setBackground(selectedColor);
					color25sinad = selectedColor;
					break;
				case 15:
					jTextField30sinad.setBackground(selectedColor);
					color30sinad = selectedColor;
					break;
				case 16:
					jTextField0ber.setBackground(selectedColor);
					color0ber = selectedColor;
					break;
				case 17:
					jTextField5ber.setBackground(selectedColor);
					color5ber = selectedColor;
					break;
				case 18:
					jTextField10ber.setBackground(selectedColor);
					color10ber = selectedColor;
					break;
				case 19:
					jTextField15ber.setBackground(selectedColor);
					color15ber = selectedColor;
					break;
				case 20:
					jTextField20ber.setBackground(selectedColor);
					color20ber = selectedColor;
					break;
				case 21:
					jTextField25ber.setBackground(selectedColor);
					color25ber = selectedColor;
					break;
				case 22:
					jTextField30ber.setBackground(selectedColor);
					color30ber = selectedColor;
					break;
				case 23:
					jTextField35ber.setBackground(selectedColor);
					color35ber = selectedColor;
					break;
				}
			}
		});
		
		jccCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jccDialog.setVisible(false);
			}
		});
	}

	private void jTextField120dBmMouseClicked(MouseEvent event) {
		colorIndex = 7;
		jccDialog.setVisible(true);
	}

	private void jTextField110dBmMouseClicked(MouseEvent event) {
		colorIndex = 6;
		jccDialog.setVisible(true);
	}

	private void jTextField100dBmMouseClicked(MouseEvent event) {
		colorIndex = 5;
		jccDialog.setVisible(true);
	}

	private void jTextField90dBmMouseClicked(MouseEvent event) {
		colorIndex = 4;
		jccDialog.setVisible(true);
	}

	private void jTextField80dBmMouseClicked(MouseEvent event) {
		colorIndex = 3;
		jccDialog.setVisible(true);
	}

	private void jTextField70dBmMouseClicked(MouseEvent event) {
		colorIndex = 2;
		jccDialog.setVisible(true);
	}

	private void jTextField60dBmMouseClicked(MouseEvent event) {
		colorIndex = 1;
		jccDialog.setVisible(true);
	}

	private void jTextField50dBmMouseClicked(MouseEvent event) {
		colorIndex = 0;
		jccDialog.setVisible(true);
	}

	private void jTextField30sinadMouseClicked(MouseEvent event) {
		colorIndex = 15;
		jccDialog.setVisible(true);
	}

	private void jTextField25sinadMouseClicked(MouseEvent event) {
		colorIndex = 14;
		jccDialog.setVisible(true);
	}

	private void jTextField20sinadMouseClicked(MouseEvent event) {
		colorIndex = 13;
		jccDialog.setVisible(true);
	}

	private void jTextField15sinadMouseClicked(MouseEvent event) {
		colorIndex = 12;
		jccDialog.setVisible(true);
	}

	private void jTextField12sinadMouseClicked(MouseEvent event) {
		colorIndex = 11;
		jccDialog.setVisible(true);
	}

	private void jTextField10sinadMouseClicked(MouseEvent event) {
		colorIndex = 10;
		jccDialog.setVisible(true);
	}

	private void jTextField5sinadMouseClicked(MouseEvent event) {
		colorIndex = 9;
		jccDialog.setVisible(true);
	}

	private void jTextField0sinadMouseClicked(MouseEvent event) {
		colorIndex = 8;
		jccDialog.setVisible(true);
	}
	
	private void jTextField35berMouseClicked(MouseEvent event) {
		colorIndex = 23;
		jccDialog.setVisible(true);
	}

	private void jTextField30berMouseClicked(MouseEvent event) {
		colorIndex = 22;
		jccDialog.setVisible(true);
	}

	private void jTextField25berMouseClicked(MouseEvent event) {
		colorIndex = 21;
		jccDialog.setVisible(true);
	}

	private void jTextField20berMouseClicked(MouseEvent event) {
		colorIndex = 20;
		jccDialog.setVisible(true);
	}

	private void jTextField15berMouseClicked(MouseEvent event) {
		colorIndex = 19;
		jccDialog.setVisible(true);
	}

	private void jTextField10berMouseClicked(MouseEvent event) {
		colorIndex = 18;
		jccDialog.setVisible(true);
	}

	private void jTextField5berMouseClicked(MouseEvent event) {
		colorIndex = 17;
		jccDialog.setVisible(true);
	}

	private void jTextField0berMouseClicked(MouseEvent event) {
		colorIndex = 16;
		jccDialog.setVisible(true);
	}
	
	private void drawGraphicalUserInterface() {
		GroupLayout layout = new GroupLayout(getContentPane());

		getContentPane().setLayout(layout);

		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
						.add(tabbedPane, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE))
				.add(GroupLayout.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap(240, Short.MAX_VALUE)
								.add(okButton, GroupLayout.PREFERRED_SIZE, 72,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.RELATED)
								.add(applyButton, GroupLayout.PREFERRED_SIZE,
										72, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.RELATED)
								.add(cancelButton, GroupLayout.PREFERRED_SIZE,
										72, GroupLayout.PREFERRED_SIZE)
								.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.add(tabbedPane, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.RELATED)
								.add(layout
										.createParallelGroup(
												GroupLayout.BASELINE)
										.add(applyButton).add(cancelButton)
										.add(okButton)).add(16, 16, 16)));

		GroupLayout sampleDistributionAcrossTilePanelLayout = new GroupLayout(sampleDistributionAcrossTilePanel);
	
	    sampleDistributionAcrossTilePanel.setLayout(sampleDistributionAcrossTilePanelLayout);
	    
	    sampleDistributionAcrossTilePanelLayout.setHorizontalGroup(
	        sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.LEADING)
	        .add(sampleDistributionAcrossTilePanelLayout.createSequentialGroup()
	            .addContainerGap()
	            .add(sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.LEADING)
	                .add(timingSetBySpeedRadioButton)
	            	.add(timingEvenAcrossTileRadioButton)
	                .add(timingFrontWeightedRadioButton))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	    
	    sampleDistributionAcrossTilePanelLayout.setVerticalGroup(
	        sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.LEADING)
	        .add(sampleDistributionAcrossTilePanelLayout.createSequentialGroup()
	            .add(timingSetBySpeedRadioButton)
	        	.addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        	.add(timingEvenAcrossTileRadioButton)
	            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	            .add(timingFrontWeightedRadioButton)));
	
	    GroupLayout earthShapeModelPanelLayout = new GroupLayout(earthShapeModelPanel);
		
	    earthShapeModelPanel.setLayout(earthShapeModelPanelLayout);
	    
	    earthShapeModelPanelLayout.setHorizontalGroup(
	    		earthShapeModelPanelLayout.createParallelGroup(GroupLayout.LEADING)
	        .add(earthShapeModelPanelLayout.createSequentialGroup()
	            .addContainerGap()
	            .add(earthShapeModelPanelLayout.createParallelGroup(GroupLayout.LEADING)
	                .add(earthShapeModelFlatRadioButton)
	            	.add(earthShapeModelEllipseRadioButton))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	    
	    earthShapeModelPanelLayout.setVerticalGroup(
	    		earthShapeModelPanelLayout.createParallelGroup(GroupLayout.LEADING)
	        .add(earthShapeModelPanelLayout.createSequentialGroup()
	            .add(earthShapeModelFlatRadioButton)
	        	.addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        	.add(earthShapeModelEllipseRadioButton)));
	    	    
	    GroupLayout samplingOptionsPanelLayout = new GroupLayout(samplingOptionsPanel);
        
	    samplingOptionsPanel.setLayout(samplingOptionsPanelLayout);
        
	    samplingOptionsPanelLayout.setHorizontalGroup(
	            samplingOptionsPanelLayout.createParallelGroup(GroupLayout.LEADING)
	            .add(samplingOptionsPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .add(samplingOptionsPanelLayout.createParallelGroup(GroupLayout.LEADING)
	                    .add(sampleSINADCheckBox)
	                    .add(sampleRSSICheckBox)
	                    .add(sampleBERCheckBox))
	                .addContainerGap(42, Short.MAX_VALUE)));
	    
	        samplingOptionsPanelLayout.setVerticalGroup(
	            samplingOptionsPanelLayout.createParallelGroup(GroupLayout.LEADING)
	            .add(samplingOptionsPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .add(sampleSINADCheckBox)
	                .addPreferredGap(LayoutStyle.RELATED)
	                .add(sampleRSSICheckBox)
	                .addPreferredGap(LayoutStyle.RELATED)
	                .add(sampleBERCheckBox)
	                .addContainerGap(9, Short.MAX_VALUE)));
	    
	    GroupLayout coverageTestPanelLayout = new GroupLayout(coverageTestPanel);
	    
	    coverageTestPanel.setLayout(coverageTestPanelLayout);

	    coverageTestPanelLayout.setHorizontalGroup(
	        coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(coverageTestPanelLayout.createSequentialGroup()
            	.addContainerGap()
                .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(alertOnMinimumSamplesPerTileAcquiredCheckBox)
                    .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(sampleDistributionAcrossTilePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(coverageTestPanelLayout.createSequentialGroup()
                            .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                                .add(tileSizeArcSecondsComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(maxSamplesPerTileComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                                .add(tileSizeArcSecondsComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(maxSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
                            .add(18, 18, 18)
                            .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.TRAILING, false)
                                .add(minSamplesPerTileComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(dotsPerTileComboBoxLabel, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                .add(minSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .add(dotsPerTileComboBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)))
                        .add(coverageTestPanelLayout.createSequentialGroup()
                            .add(samplingOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(18, 18, 18)
                            .add(earthShapeModelPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
	    
        coverageTestPanelLayout.setVerticalGroup(
            coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(coverageTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(coverageTestPanelLayout.createSequentialGroup()
                        .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                            .add(dotsPerTileComboBoxLabel)
                            .add(dotsPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                            .add(minSamplesPerTileComboBoxLabel)
                            .add(minSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .add(coverageTestPanelLayout.createSequentialGroup()
                        .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                            .add(tileSizeArcSecondsComboBoxLabel)
                            .add(tileSizeArcSecondsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                            .add(maxSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(maxSamplesPerTileComboBoxLabel))))
                .add(18, 18, 18)
                .add(sampleDistributionAcrossTilePanel, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(coverageTestPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(samplingOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(18, 18, 18)
                    .add(earthShapeModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(alertOnMinimumSamplesPerTileAcquiredCheckBox)
                .addContainerGap()));
	    
        GroupLayout manualDataCollectionModeSelectorPanelLayout = new GroupLayout(manualDataCollectionModeSelectorPanel);
        
        manualDataCollectionModeSelectorPanel.setLayout(manualDataCollectionModeSelectorPanelLayout);
        
        manualDataCollectionModeSelectorPanelLayout.setHorizontalGroup(
            manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(manualDataCollectionModeSelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(manualDataCollectionContinuousModeRadioButton)
                    .add(manualDataCollectionManualModeRadioButton))
                .addContainerGap(104, Short.MAX_VALUE)));
        
        manualDataCollectionModeSelectorPanelLayout.setVerticalGroup(
            manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(manualDataCollectionModeSelectorPanelLayout.createSequentialGroup()
                .add(manualDataCollectionContinuousModeRadioButton)
                .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(manualDataCollectionManualModeRadioButton)));

        GroupLayout manualModePanelLayout = new GroupLayout(manualModePanel);
        
        manualModePanel.setLayout(manualModePanelLayout);
        
        manualModePanelLayout.setHorizontalGroup(
            manualModePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(manualModePanelLayout.createSequentialGroup()
            	.addContainerGap()
                .add(manualDataCollectionModeSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(0, 27, Short.MAX_VALUE)));
        
        manualModePanelLayout.setVerticalGroup(
            manualModePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(manualModePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(manualDataCollectionModeSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(144, Short.MAX_VALUE)));
        
        GroupLayout signalQualityDisplaySelectorPanelLayout = new GroupLayout(signalQualityDisplaySelectorPanel);
        
        signalQualityDisplaySelectorPanel.setLayout(signalQualityDisplaySelectorPanelLayout);
        
        signalQualityDisplaySelectorPanelLayout.setHorizontalGroup(
            signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(displaySinadRadioButton)
                    .add(displayRssiRadioButton)
                    .add(displayBerRadioButton))
                .addContainerGap(117, Short.MAX_VALUE)));
        
        signalQualityDisplaySelectorPanelLayout.setVerticalGroup(
            signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                .add(displaySinadRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(displayRssiRadioButton)
                .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(displayBerRadioButton)
                .addContainerGap()));

        GroupLayout displaySettingsPanelLayout = new GroupLayout(displaySettingsPanel);
        
        displaySettingsPanel.setLayout(displaySettingsPanelLayout);
        
        displaySettingsPanelLayout.setHorizontalGroup(
            displaySettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(displaySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(displaySettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(displaySettingsPanelLayout.createSequentialGroup()
                        .add(signalQualityDisplaySelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(showGridSquareShadingCheckBox))
                    .add(displaySettingsPanelLayout.createSequentialGroup()
                        .add(RSSIDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(BERDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(SINADDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        displaySettingsPanelLayout.setVerticalGroup(
            displaySettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(displaySettingsPanelLayout.createSequentialGroup()
                .add(displaySettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(displaySettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(signalQualityDisplaySelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .add(displaySettingsPanelLayout.createSequentialGroup()
                        .add(25, 25, 25)
                        .add(showGridSquareShadingCheckBox)))
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(displaySettingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(RSSIDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(BERDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(SINADDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        GroupLayout RSSIDataPointColoringPanelLayout = new GroupLayout(RSSIDataPointColoringPanel);
        
        RSSIDataPointColoringPanel.setLayout(RSSIDataPointColoringPanelLayout);
        
        RSSIDataPointColoringPanelLayout.setHorizontalGroup(
            RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel90dBm)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField90dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel80dBm)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField80dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel70dBm)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField70dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel60dBm)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField60dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                            .add(jLabel50dBm)
                            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jTextField50dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                        .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                            .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel100dBm)
                                    .add(18, 18, 18)
                                    .add(jTextField100dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel110dBm)
                                    .add(18, 18, 18)
                                    .add(jTextField110dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel120dBm)
                                    .add(18, 18, 18)
                                    .add(jTextField120dBm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
                            .add(0, 0, Short.MAX_VALUE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        RSSIDataPointColoringPanelLayout.setVerticalGroup(
            RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel50dBm)
                    .add(jTextField50dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel60dBm)
                    .add(jTextField60dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel70dBm)
                    .add(jTextField70dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel80dBm)
                    .add(jTextField80dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel90dBm)
                    .add(jTextField90dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel100dBm)
                    .add(jTextField100dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel110dBm)
                    .add(jTextField110dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel120dBm)
                    .add(jTextField120dBm, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE)));
        
        GroupLayout BERDataPointColoringPanelLayout = new GroupLayout(BERDataPointColoringPanel);
        
        BERDataPointColoringPanel.setLayout(BERDataPointColoringPanelLayout);
        
        BERDataPointColoringPanelLayout.setHorizontalGroup(
            BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel20ber)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField20ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel15ber)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField15ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel10ber)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField10ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel5ber)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField5ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                            .add(jLabel0ber)
                            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jTextField0ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                        .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                            .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel25ber)
                                    .add(18, 18, 18)
                                    .add(jTextField25ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel30ber)
                                    .add(18, 18, 18)
                                    .add(jTextField30ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel35ber)
                                    .add(18, 18, 18)
                                    .add(jTextField35ber, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
                            .add(0, 0, Short.MAX_VALUE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        BERDataPointColoringPanelLayout.setVerticalGroup(
            BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(BERDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel0ber)
                    .add(jTextField0ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel5ber)
                    .add(jTextField5ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel10ber)
                    .add(jTextField10ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel15ber)
                    .add(jTextField15ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel20ber)
                    .add(jTextField20ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel25ber)
                    .add(jTextField25ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel30ber)
                    .add(jTextField30ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel35ber)
                    .add(jTextField35ber, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE)));
        
        GroupLayout SINADDataPointColoringPanelLayout = new GroupLayout(SINADDataPointColoringPanel);
        
        SINADDataPointColoringPanel.setLayout(SINADDataPointColoringPanelLayout);
        
        SINADDataPointColoringPanelLayout.setHorizontalGroup(
            SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel15sinad)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField15sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel12sinad)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField12sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel10sinad)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField10sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .add(jLabel5sinad)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jTextField5sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                            .add(jLabel0sinad)
                            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jTextField0sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                        .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                            .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel20sinad)
                                    .add(18, 18, 18)
                                    .add(jTextField20sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel25sinad)
                                    .add(18, 18, 18)
                                    .add(jTextField25sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                                    .add(jLabel30sinad)
                                    .add(18, 18, 18)
                                    .add(jTextField30sinad, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
                            .add(0, 0, Short.MAX_VALUE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        SINADDataPointColoringPanelLayout.setVerticalGroup(
            SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(SINADDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel0sinad)
                    .add(jTextField0sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel5sinad)
                    .add(jTextField5sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel10sinad)
                    .add(jTextField10sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel12sinad)
                    .add(jTextField12sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel15sinad)
                    .add(jTextField15sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel20sinad)
                    .add(jTextField20sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel25sinad)
                    .add(jTextField25sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(jLabel30sinad)
                    .add(jTextField30sinad, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE)));
        
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
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
	        jccDialogLayout.setVerticalGroup(
	            jccDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jccDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jccApply)
	                    .addComponent(jccCancel))
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	        
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();

		jccDialog.pack();
		jccDialog.setLocation((screenSize.width / 2) - (jccDialog.getWidth() / 2),
				(screenSize.height / 2) - (jccDialog.getHeight() / 2));
		
		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
	}

	private void getSettingsFromRegistry() {
		userPref = Preferences.userRoot();
		earthShapeModelIndex = userPref.getInt("EarthShapeModelIndex", 0);
		dotsPerTileIndex = userPref.getInt("DotsPerTileIndex", 0);
		tileSizeIndex = userPref.getInt("SizeOfTileArcSeconds", 0);
		minSamplesPerTileIndex = userPref.getInt("MinSamplesPerTile", 0);
		maxSamplesPerTileIndex = userPref.getInt("MaxSamplesPerTile", 0);
		alertOnMinimumSamplesPerTileAcquired = userPref.getBoolean(
				"AlertOnMinimumSamplesPerTileAcquired", false);
		showGridSquareShading = userPref.getBoolean("ShowGridSquareShading", true);
		sampleTimingMode = userPref.getInt("SampleTimingMode", 0);
		signalQualityDisplayMode = userPref.getInt("SignalQualityDisplayMode", 1);
		manualDataCollectionMode = userPref.getInt("ManualDataCollectionMode", 0);
		sampleRSSI = userPref.getBoolean("SampleRSSI", false);
		sampleBER = userPref.getBoolean("SampleBER", false);
		sampleSINAD = userPref.getBoolean("SampleSINAD", false);
		color50dBm = new Color(userPref.getInt("Color50dBm", 0));
		color60dBm = new Color(userPref.getInt("Color60dBm", 0));
		color70dBm = new Color(userPref.getInt("Color70dBm", 0));
		color80dBm = new Color(userPref.getInt("Color80dBm", 0));
		color90dBm = new Color(userPref.getInt("Color90dBm", 0));
		color100dBm = new Color(userPref.getInt("Color100dBm", 0));
		color110dBm = new Color(userPref.getInt("Color110dBm", 0));
		color120dBm = new Color(userPref.getInt("Color120dBm", 0));
		color0sinad = new Color(userPref.getInt("Color0sinad", 0));
		color5sinad = new Color(userPref.getInt("Color5sinad", 0));
		color10sinad = new Color(userPref.getInt("Color10sinad", 0));
		color12sinad = new Color(userPref.getInt("Color12sinad", 0));
		color15sinad = new Color(userPref.getInt("Color15sinad", 0));
		color20sinad = new Color(userPref.getInt("Color20sinad", 0));
		color25sinad = new Color(userPref.getInt("Color25sinad", 0));
		color30sinad = new Color(userPref.getInt("Color30sinad", 0));
		color0ber = new Color(userPref.getInt("Color0ber", 0));
		color5ber = new Color(userPref.getInt("Color5ber", 0));
		color10ber = new Color(userPref.getInt("Color10ber", 0));
		color15ber = new Color(userPref.getInt("Color15ber", 0));
		color20ber = new Color(userPref.getInt("Color20ber", 0));
		color25ber = new Color(userPref.getInt("Color25ber", 0));
		color30ber = new Color(userPref.getInt("Color30ber", 0));
		color35ber = new Color(userPref.getInt("Color35ber", 0));
	}

	public void showSettingsDialog(boolean newShowSettingsDialog) {
		setVisible(newShowSettingsDialog);
	}

	private void applyButtonActionListenerEvent(ActionEvent event) {
		userPref.putInt("EarthShapeModelIndex", earthShapeModelIndex);
		userPref.putInt("DotsPerTileIndex", dotsPerTileIndex);
		userPref.putInt("SizeOfTileArcSeconds", tileSizeIndex);
		userPref.putInt("MinSamplesPerTile", minSamplesPerTileIndex);
		userPref.putInt("MaxSamplesPerTile", maxSamplesPerTileIndex);
		userPref.putBoolean("AlertOnMinimumSamplesPerTileAcquired",	alertOnMinimumSamplesPerTileAcquired);
		userPref.putBoolean("ShowGridSquareShading", showGridSquareShading);
		userPref.putBoolean("SampleRSSI", sampleRSSI);
		userPref.putBoolean("SampleBER", sampleBER);
		userPref.putBoolean("SampleSINAD", sampleSINAD);
		userPref.putInt("SampleTimingMode", sampleTimingMode);
		userPref.putInt("SignalQualityDisplayMode", signalQualityDisplayMode);
		userPref.putInt("ManualDataCollectionMode", manualDataCollectionMode);
		userPref.putInt("Color50dBm", color50dBm.getRGB());
		userPref.putInt("Color60dBm", color60dBm.getRGB());
		userPref.putInt("Color70dBm", color70dBm.getRGB());
		userPref.putInt("Color80dBm", color80dBm.getRGB());
		userPref.putInt("Color90dBm", color90dBm.getRGB());
		userPref.putInt("Color100dBm", color100dBm.getRGB());
		userPref.putInt("Color110dBm", color110dBm.getRGB());
		userPref.putInt("Color120dBm", color120dBm.getRGB());
		userPref.putInt("Color0sinad", color0sinad.getRGB());
		userPref.putInt("Color5sinad", color5sinad.getRGB());
		userPref.putInt("Color10sinad", color10sinad.getRGB());
		userPref.putInt("Color12sinad", color12sinad.getRGB());
		userPref.putInt("Color15sinad", color15sinad.getRGB());
		userPref.putInt("Color20sinad", color20sinad.getRGB());
		userPref.putInt("Color25sinad", color25sinad.getRGB());
		userPref.putInt("Color30sinad", color30sinad.getRGB());
		userPref.putInt("Color0ber", color0ber.getRGB());
		userPref.putInt("Color5ber", color5ber.getRGB());
		userPref.putInt("Color10ber", color10ber.getRGB());
		userPref.putInt("Color15ber", color15ber.getRGB());
		userPref.putInt("Color20ber", color20ber.getRGB());
		userPref.putInt("Color25ber", color25ber.getRGB());
		userPref.putInt("Color30ber", color30ber.getRGB());
		userPref.putInt("Color35ber", color35ber.getRGB());
		
		setValues();
		
		firePropertyChange("PROPERTY_CHANGE", null, null);
	}

	private void setTileSizeValues(double degreesLatitude) {
		double requestedLatitudeFeet = 0;
		double requestedArcSeconds = 0;
		switch (tileSizeIndex) {
			case 0:
				requestedLatitudeFeet = 10;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 1:
				requestedLatitudeFeet = 20;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 2: 
				requestedLatitudeFeet = 40;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 3:
				requestedLatitudeFeet = 50;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 4:
				requestedLatitudeFeet = 100;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 5:
				requestedLatitudeFeet = 5280.0 / 4.0;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 6:
				requestedLatitudeFeet = 5280.0 / 2.0;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude).y;
				break;
			case 7:
				requestedArcSeconds = 1.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 8:
				requestedArcSeconds = 3.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 9:
				requestedArcSeconds = 5.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 10:
				requestedArcSeconds = 10.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 11:
				requestedArcSeconds = 15.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 12:
				requestedArcSeconds = 30.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 13:
				requestedArcSeconds = 60.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 14:
				requestedArcSeconds = 90.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 15:
				requestedArcSeconds = 120.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
			case 16:
				requestedArcSeconds = 180.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, degreesLatitude).y;
				break;
		}

		switch (earthShapeModelIndex) {
			case 0:
				tileSizeArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, degreesLatitude);
				break;
			case 1:
				tileSizeArcSeconds = new Point.Double(requestedArcSeconds, requestedArcSeconds);
				break;	
		}
	}

	private void setValues() {	
		switch (maxSamplesPerTileIndex) {
			case 0:
				maxSamplesPerTile = 5;
				break;
			case 1:
				maxSamplesPerTile = 10;
				break;
			case 2:
				maxSamplesPerTile = 15;
				break;
			case 3:
				maxSamplesPerTile = 20;
				break;
			case 4:
				maxSamplesPerTile = 25;
				break;
			case 5:
				maxSamplesPerTile = 30;
				break;
			case 6:
				maxSamplesPerTile = 60;
				break;
			case 7:
				maxSamplesPerTile = 90;
				break;
			case 8:
				maxSamplesPerTile = 120;
				break;
			case 9:
				maxSamplesPerTile = 180;
				break;
			case 10:
				maxSamplesPerTile = 240;
				break;
			case 11:
				maxSamplesPerTile = 300;
				break;
			case 12:
				maxSamplesPerTile = 360;
				break;
			case 13:
				maxSamplesPerTile = 420;
				break;
			case 14:
				maxSamplesPerTile = 480;
				break;
			case 15:
				maxSamplesPerTile = 540;
				break;
			case 16:
				maxSamplesPerTile = 600;
				break;
			case 17:
				maxSamplesPerTile = 900;
				break;
			case 18:
				maxSamplesPerTile = 1200;
				break;
			case 19:
				maxSamplesPerTile = 1800;
				break;
			case 20:
				maxSamplesPerTile = 2700;
				break;
			case 21:
				maxSamplesPerTile = 3600;
				break;
			case 22:
				maxSamplesPerTile = Integer.MAX_VALUE;
				break;
		}

		minTimePerTile = 0;
		
		switch (minSamplesPerTileIndex) {
			case 0:
				minSamplesPerTile = 1;
				break;
			case 1:
				minSamplesPerTile = 5;
				break;
			case 2:
				minSamplesPerTile = 10;
				break;
			case 3:
				minSamplesPerTile = 15;
				break;
			case 4:
				minSamplesPerTile = 20;
				break;
			case 5:
				minSamplesPerTile = 25;
				break;
			case 6:
				minSamplesPerTile = 30;
				break;
			case 7:
				minSamplesPerTile = 60;
				break;
			case 8:
				minSamplesPerTile = 90;
				break;
			case 9:
				minSamplesPerTile = 120;
				break;
			case 10:
				minSamplesPerTile = 180;
				break;
			case 11:
				minSamplesPerTile = 240;
				break;
			case 12:
				minSamplesPerTile = 300;
				break;
			case 13:
				minSamplesPerTile = 360;
				break;
			case 14:
				minSamplesPerTile = 420;
				break;
			case 15:
				minSamplesPerTile = 480;
				break;
			case 16:
				minSamplesPerTile = 540;
				break;
			case 17:
				minSamplesPerTile = 600;
				break;
			case 18:
				minSamplesPerTile = 900;
				break;
			case 19:
				minSamplesPerTile = 1200;
				break;
			case 20:
				minSamplesPerTile = 1800;
				break;
			case 21:
				minSamplesPerTile = 2700;
				break;
			case 22:
				minSamplesPerTile = 3600;
				break;
			case 23:
				minSamplesPerTile = -1;
				minTimePerTile = 60;
				break;
			case 24:
				minSamplesPerTile = -1;
				minTimePerTile = 150;
				break;
			case 25:
				minSamplesPerTile = -1;
				minTimePerTile = 180;
				break;
			case 26:
				minSamplesPerTile = -1;
				minTimePerTile = 300;
				break;
		}
		
		switch (dotsPerTileIndex) {
			case 0:
				dotsPerTile = 1;
				break;
			case 1:
				dotsPerTile = 3;
				break;
			case 2:
				dotsPerTile = 5;
				break;
			case 3:
				dotsPerTile = 10;
				break;
			case 4:
				dotsPerTile = 15;
				break;
			case 5:
				dotsPerTile = 30;
				break;
			case 6:
				dotsPerTile = 50;
				break;
			case 7:
				dotsPerTile = 100;
				break;
			case 8:
				dotsPerTile = 200;
				break;
			case 9:
				dotsPerTile = 500;
				break;
			case 10:
				dotsPerTile = 1000;
				break;
			case 11:
				dotsPerTile = Integer.MAX_VALUE;
				break;
		}
	}

	private void tileSizeArcSecondsComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		tileSizeIndex = cb.getSelectedIndex();
	}

	private void maxSamplesPerTileComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		maxSamplesPerTileIndex = cb.getSelectedIndex();
	}

	private void minSamplesPerTileComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		minSamplesPerTileIndex = cb.getSelectedIndex();
	}

	private void dotsPerTileComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		dotsPerTileIndex = cb.getSelectedIndex();
	}

	private void alertOnMinimumSamplesPerTileAcquiredCheckBoxItemStateChanged(
			ItemEvent event) {
		alertOnMinimumSamplesPerTileAcquired = alertOnMinimumSamplesPerTileAcquiredCheckBox
				.isSelected();
	}
	
	private void showGridSquareShadingCheckBoxItemStateChanged(ItemEvent event) {
		showGridSquareShading = showGridSquareShadingCheckBox.isSelected();
	}
	
	private void sampleRSSICheckBoxItemStateChanged(ItemEvent event) {
		sampleRSSI = sampleRSSICheckBox.isSelected();
	}
	
	private void sampleBERCheckBoxItemStateChanged(ItemEvent event) {
		sampleBER = sampleBERCheckBox.isSelected();
		if (sampleBERCheckBox.isSelected()) {
			sampleSINADCheckBox.setSelected(false);
			sampleSINAD = false;
		}
	}
	
	private void sampleSINADCheckBoxItemStateChanged(ItemEvent event) {
		sampleSINAD = sampleSINADCheckBox.isSelected();
		if (sampleSINADCheckBox.isSelected()) {
			sampleBERCheckBox.setSelected(false);
			sampleBER = false;
		}
	}
	
	public void tileSizeArcSecondsComboBoxEnabled(boolean enabled) {
		tileSizeArcSecondsComboBox.setEnabled(enabled);
	}
	
	public void setTileSize(Point.Double tileSize) {
		firePropertyChange("TILE_SIZE", this.tileSizeArcSeconds, tileSize);
		this.tileSizeArcSeconds = tileSize;
	}

	public void setMinSamplesPerTile(int minSamplesPerTile) {
		this.minSamplesPerTile = minSamplesPerTile;
	}
	
	public void setMaxSamplesPerTile(int maxSamplesPerTile) {
		this.maxSamplesPerTile = maxSamplesPerTile;
	}
	
	public boolean isSinadSamplingInEffect() {
		return sampleSINAD;
	}
	
	public boolean isBERSamplingInEffect() {
		return sampleBER;
	}
	
	public boolean isRSSISamplingInEffect() {
		return sampleRSSI;
	}
	
	public int getSampleTimingMode() {
		return sampleTimingMode;
	}
	
	public int getManualDataCollectionMode() {
		return manualDataCollectionMode;
	}
	
	public int getSignalQualityDisplayMode() {
		return signalQualityDisplayMode;
	}
	
	public int getDotsPerTile() {
		return dotsPerTile;
	}

	public Point.Double getTileSizeArcSeconds(double degreesLatitude) {
		setTileSizeValues(degreesLatitude);
		return tileSizeArcSeconds;
	}

	public int getMinSamplesPerTile() {
		return minSamplesPerTile;
	}

	public int getMaxSamplesPerTile() {
		return maxSamplesPerTile;
	}

	public boolean isAlertOnMinimumSamplesPerTileAcquired() {
		return alertOnMinimumSamplesPerTileAcquired;
	}

	public boolean isShowGridSquareShading() {
		return showGridSquareShading;
	}
	
	public int getMinTimePerTile() {
		return minTimePerTile;
	}
	
	private class RadioButtonHandler implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent ie) {
			if (ie.getSource() == timingSetBySpeedRadioButton)
				sampleTimingMode = 0;
			else if (ie.getSource() == timingEvenAcrossTileRadioButton)
				sampleTimingMode = 1;
			else if (ie.getSource() == timingFrontWeightedRadioButton)
				sampleTimingMode = 2;
			else if (ie.getSource() == displaySinadRadioButton)
				signalQualityDisplayMode = 0;
			else if (ie.getSource() == displayRssiRadioButton)
				signalQualityDisplayMode = 1;
			else if (ie.getSource() == displayBerRadioButton)
				signalQualityDisplayMode = 2;
			else if (ie.getSource() == manualDataCollectionContinuousModeRadioButton)
				manualDataCollectionMode = 0;
			else if (ie.getSource() == manualDataCollectionManualModeRadioButton)
				manualDataCollectionMode = 1;
			else if (ie.getSource() == earthShapeModelFlatRadioButton)
				earthShapeModelIndex= 0;
			else if (ie.getSource() == earthShapeModelEllipseRadioButton)
				earthShapeModelIndex = 1;
		}
	}

	public Color getdBmColor(double dBm) {
		Color color = Color.BLACK;
		if (dBm <= -120) color = color120dBm;
		else if (dBm <= -110) color = color110dBm;
		else if (dBm <= -100) color = color100dBm;
		else if (dBm <= -90) color = color90dBm;
		else if (dBm <= -80) color = color80dBm;
		else if (dBm <= -70) color = color70dBm;
		else if (dBm <= -60) color = color60dBm;
		else if (dBm <= 0) color = color50dBm;
		return color;
	}
	
	public Color getSinadColor(double sinad) {
		Color color = Color.BLACK;
		if (sinad <= 4) color = color0sinad;
		else if (sinad <= 9) color = color5sinad;
		else if (sinad <= 11) color = color10sinad;
		else if (sinad <= 14) color = color12sinad;
		else if (sinad <= 19) color = color15sinad;
		else if (sinad <= 24) color = color20sinad;
		else if (sinad <= 29) color = color25sinad;
		else if (sinad <= 99) color = color30sinad;
		return color;
	}
	
	public Color getBerColor(double ber) {
		Color color = Color.BLACK;
		if (ber <= 4) color = color0ber;
		else if (ber <= 9) color = color5ber;
		else if (ber <= 14) color = color10ber;
		else if (ber <= 19) color = color15ber;
		else if (ber <= 24) color = color20ber;
		else if (ber <= 29) color = color25ber;
		else if (ber <= 34) color = color30ber;
		else if (ber <= 99) color = color35ber;
		return color;
	}
}