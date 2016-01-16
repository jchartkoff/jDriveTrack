package jdrivetrack;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class CoverageTestSettings extends JDialog {
	private static final long serialVersionUID = -8082024979568837913L;
	private static final Double DEFAULT_TEST_GRID_TOP_EDGE = 39.0;
	private static final Double DEFAULT_TEST_GRID_BOTTOM_EDGE = 32.0;
	private static final Double DEFAULT_TEST_GRID_LEFT_EDGE = -88.0;
	private static final Double DEFAULT_TEST_GRID_RIGHT_EDGE = -84.0;
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
	private JCheckBox alertOnMinimumSamplesPerTileAcquiredCheckBox;
	private JCheckBox showQuadsCheckBox;
	private JCheckBox showLinesCheckBox;
	private JCheckBox showRingsCheckBox;
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
	private int earthShapeModelIndex;
	private int tileSizeIndex;
	private int minSamplesPerTileIndex;
	private int maxSamplesPerTileIndex;
	private boolean alertOnMinimumSamplesPerTileAcquired;
	private boolean showGridSquareShading;
	private boolean showRings;
	private boolean showQuads;
	private boolean showLines;
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
	
    private JPanel mapImageModelPanel;
    private JToggleButton pickUpperLeftCornerButton;
    private JPanel testGridDimensionPanel;
    private JFormattedTextField gridEdgeHeight;
    private JLabel gridEdgeHeightLabel;
    private JPanel testGridPanel;
    private JFormattedTextField gridEdgeWidth;
    private JLabel gridEdgeWidthLabel;
    private JToggleButton tileSelectionModeButton;
    private JFormattedTextField gridEdgeBottom;
    private JLabel gridEdgeBottomLabel;   
    private JFormattedTextField gridEdgeRight;
    private JLabel gridEdgeRightLabel;    
    private JFormattedTextField gridEdgeTop;
    private JLabel gridEdgeTopLabel;   
    private JFormattedTextField gridEdgeLeft;
    private JLabel gridEdgeLeftLabel;
    
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
	private double signalMarkerRadius;
	private JLabel signalMarkerRadiusLabel;
	private JTextField tfSignalMarkerRadius;
	private NumberFormat signalMarkerRadiusFormat;
	private double edgeRefLeft;
	private double edgeRefRight;
	private double edgeRefTop;
	private double edgeRefBottom;
	private double gridHeight = 2.0;
	private double gridWidth = 2.0;
	private NumberFormat latFormat;
	private NumberFormat lonFormat;
	private NumberFormat heightFormat;
	private NumberFormat widthFormat;
	private double degreesLatitude = 35.0;
	
	private Preferences systemPrefs = Preferences.systemRoot().node("jdrivetrack/prefs/CoverageTestSettings");
	
	public CoverageTestSettings(boolean clearAllPrefs) {
		if (clearAllPrefs) {
			try {
				systemPrefs.clear();
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}
		}
		
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
	    	if (clsName.equals("colorchooser.ColorChooserPanel"))
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
		showRingsCheckBox = new JCheckBox();
		showLinesCheckBox = new JCheckBox();
		showQuadsCheckBox = new JCheckBox();
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
        signalMarkerRadiusLabel = new JLabel();
        tfSignalMarkerRadius = new JTextField();
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
        testGridPanel = new JPanel();
        testGridDimensionPanel = new JPanel();
        gridEdgeWidth = new JFormattedTextField();
        gridEdgeWidthLabel = new JLabel();
        mapImageModelPanel = new JPanel();
        gridEdgeTop = new JFormattedTextField();
        gridEdgeTopLabel = new JLabel();
        gridEdgeLeft = new JFormattedTextField();
        gridEdgeLeftLabel = new JLabel();
        gridEdgeBottomLabel = new JLabel();
        gridEdgeBottom = new JFormattedTextField();
        gridEdgeRightLabel = new JLabel();
        gridEdgeRight = new JFormattedTextField();
        gridEdgeHeight = new JFormattedTextField();
        gridEdgeHeightLabel = new JLabel();
        tileSelectionModeButton = new JToggleButton();
        pickUpperLeftCornerButton = new JToggleButton();
        signalMarkerRadiusFormat = new DecimalFormat("#0.0");
        latFormat = new DecimalFormat("00.00000");
        lonFormat = new DecimalFormat("000.00000");
        heightFormat = new DecimalFormat("000.0000");
        widthFormat = new DecimalFormat("000.0000");

		okButton = new JButton("OK");
		okButton.setMultiClickThreshhold(50L);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMultiClickThreshhold(50L);
		
		applyButton = new JButton("Apply");
		applyButton.setMultiClickThreshhold(50L);
		
		setTitle("Coverage Test Settings");

		tabbedPane.addTab(" Coverage Test Settings ", null, coverageTestPanel, null);
		tabbedPane.addTab(" Display Settings ", null, displaySettingsPanel, null);
		tabbedPane.addTab(" Manual Mode Settings ", null, manualModePanel, null);
		tabbedPane.addTab(" Test Grid Settings ", null, testGridPanel, null);

		signalMarkerRadiusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		signalMarkerRadiusLabel.setText("Signal Marker Radius (pixels)");
		
		tfSignalMarkerRadius.setText(signalMarkerRadiusFormat.format(signalMarkerRadius));
		
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
		showRingsCheckBox.setText("Show Rings");
		showQuadsCheckBox.setText("Show Quads");
		showLinesCheckBox.setText("Show Lines");
		sampleRSSICheckBox.setText("Sample RSSI Values");
		sampleBERCheckBox.setText("Sample Bit Error Rate");
		sampleSINADCheckBox.setText("Sample SINAD Values");
		
		updateComponents();
		
        testGridDimensionPanel.setBorder(BorderFactory.createTitledBorder("Test Grid Dimesnions"));

        gridEdgeWidth.setHorizontalAlignment(JFormattedTextField.CENTER);
        gridEdgeWidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeWidthLabel.setText("Width of Test Grid in Miles");

        mapImageModelPanel.setBackground(new java.awt.Color(153, 255, 153));
        mapImageModelPanel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        gridEdgeTopLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gridEdgeTopLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gridEdgeTopLabel.setText("Latitude of Upper Left Corner of Grid");
        gridEdgeTop.setHorizontalAlignment(JFormattedTextField.CENTER);

        gridEdgeLeftLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gridEdgeLeftLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gridEdgeLeftLabel.setText("Longitude of Upper Left Corner of Grid");
        gridEdgeLeft.setHorizontalAlignment(JFormattedTextField.CENTER);
        
        gridEdgeBottom.setHorizontalAlignment(JFormattedTextField.CENTER);
        gridEdgeBottomLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeBottomLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        gridEdgeBottomLabel.setText("Latitude of Lower Right Corner of Grid");

        gridEdgeRight.setHorizontalAlignment(JFormattedTextField.CENTER);
        gridEdgeRightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeRightLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        gridEdgeRightLabel.setText("Longitude of Lower Right Corner of Grid");

		sampleDistributionAcrossTilePanel.setBorder(BorderFactory.createTitledBorder
				("Sample Distribution Across Tile"));
		
        gridEdgeHeight.setHorizontalAlignment(JTextField.CENTER);

        gridEdgeHeightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeHeightLabel.setText("Height of Test Grid in Miles");

        tileSelectionModeButton.setText("Engage Tile Selection Mode");

        pickUpperLeftCornerButton.setText("Pick Upper Left Corner From Map");
		
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
		
		showLinesCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				showLinesCheckBoxItemStateChanged(event);
			}
		});
		
		showRingsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				showRingsCheckBoxItemStateChanged(event);
			}
		});
		
		showQuadsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				showQuadsCheckBoxItemStateChanged(event);
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
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField50dBmMouseClicked(event);
            }
        });
		
		jTextField60dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField60dBmMouseClicked(event);
            }
        });
		
		jTextField70dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField70dBmMouseClicked(event);
            }
        });
		
		jTextField80dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField80dBmMouseClicked(event);
            }
        });
		
		jTextField90dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField90dBmMouseClicked(event);
            }
        });
		
		jTextField100dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField100dBmMouseClicked(event);
            }
        });
		
		jTextField110dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField110dBmMouseClicked(event);
            }
        });
		
		jTextField120dBm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField120dBmMouseClicked(event);
            }
        });
		
		jTextField0sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField0sinadMouseClicked(event);
            }
        });
		
		jTextField5sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField5sinadMouseClicked(event);
            }
        });
		
		jTextField10sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField10sinadMouseClicked(event);
            }
        });
		
		jTextField12sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField12sinadMouseClicked(event);
            }
        });
		
		jTextField15sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField15sinadMouseClicked(event);
            }
        });
		
		jTextField20sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField20sinadMouseClicked(event);
            }
        });
		
		jTextField25sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField25sinadMouseClicked(event);
            }
        });
		
		jTextField30sinad.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField30sinadMouseClicked(event);
            }
        });
		
		jTextField0ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField0berMouseClicked(event);
            }
        });
		
		jTextField5ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField5berMouseClicked(event);
            }
        });
		
		jTextField10ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField10berMouseClicked(event);
            }
        });
		
		jTextField35ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField35berMouseClicked(event);
            }
        });
		
		jTextField15ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField15berMouseClicked(event);
            }
        });
		
		jTextField20ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField20berMouseClicked(event);
            }
        });
		
		jTextField25ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField25berMouseClicked(event);
            }
        });
		
		jTextField30ber.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                jTextField30berMouseClicked(event);
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

        tileSelectionModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tileSelectionModeButtonActionPerformed(event);
            }
        });

        pickUpperLeftCornerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                pickUpperLeftCornerButtonActionPerformed(event);
            }
        });

        gridEdgeWidth.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeWidth.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeWidth.setFont(new Font(gridEdgeWidth.getFont().getName(), Font.BOLD, gridEdgeWidth.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeWidth.setFont(new Font(gridEdgeWidth.getFont().getName(), Font.PLAIN, gridEdgeWidth.getFont().getSize())); 
				boolean valid = validateWidth();
				showValidWidth(valid);
				gridWidth = Double.parseDouble(gridEdgeWidth.getText());
				if (valid) {
					double gridWidthMeters = Vincenty.feetToMeters(gridWidth * Vincenty.FEET_PER_MILE);
					edgeRefRight = Vincenty.destinationPoint(new Point.Double(edgeRefLeft, edgeRefTop), 90.0, gridWidthMeters).x;
					gridEdgeRight.setText(lonFormat.format(edgeRefRight));
				}
			}
        });
        
        gridEdgeHeight.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeHeight.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeHeight.setFont(new Font(gridEdgeHeight.getFont().getName(), Font.BOLD, gridEdgeHeight.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeHeight.setFont(new Font(gridEdgeHeight.getFont().getName(), Font.PLAIN, gridEdgeHeight.getFont().getSize())); 
				boolean valid = validateHeight();
				showValidHeight(valid);
				gridHeight = Double.parseDouble(gridEdgeHeight.getText());
				if (valid) {
					double gridHeightMeters = Vincenty.feetToMeters(gridHeight * Vincenty.FEET_PER_MILE);
					edgeRefBottom = Vincenty.destinationPoint(new Point.Double(edgeRefLeft, edgeRefTop), 0, gridHeightMeters).y;
					gridEdgeBottom.setText(latFormat.format(edgeRefBottom));
				}
			}
        });
        
        gridEdgeTop.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}					
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeTop.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeTop.setFont(new Font(gridEdgeTop.getFont().getName(), Font.BOLD, gridEdgeTop.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeTop.setFont(new Font(gridEdgeTop.getFont().getName(), Font.PLAIN, gridEdgeTop.getFont().getSize())); 
				boolean valid = validateHeight();
				showValidHeight(valid);
				edgeRefTop = Double.parseDouble(gridEdgeTop.getText());
				if (valid) {
					double gridFieldHeightMeters = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefLeft,edgeRefBottom));
					gridHeight = Vincenty.metersToFeet(gridFieldHeightMeters) / Vincenty.FEET_PER_MILE;
					gridEdgeHeight.setText(heightFormat.format(gridHeight));
				}
			}
        });

        gridEdgeBottom.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}					
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeBottom.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeBottom.setFont(new Font(gridEdgeBottom.getFont().getName(), Font.BOLD, gridEdgeBottom.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeBottom.setFont(new Font(gridEdgeBottom.getFont().getName(), Font.PLAIN, gridEdgeBottom.getFont().getSize())); 
				boolean valid = validateHeight();
				showValidHeight(valid);
				edgeRefBottom = Double.parseDouble(gridEdgeBottom.getText());
				if (valid) {
					double gridFieldHeightMeters = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefLeft,edgeRefBottom));
					gridHeight = Vincenty.metersToFeet(gridFieldHeightMeters) / Vincenty.FEET_PER_MILE;
					gridEdgeHeight.setText(heightFormat.format(gridHeight));
				}
			}
        });
        
        gridEdgeLeft.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}					
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeLeft.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeLeft.setFont(new Font(gridEdgeLeft.getFont().getName(), Font.BOLD, gridEdgeLeft.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeLeft.setFont(new Font(gridEdgeLeft.getFont().getName(), Font.PLAIN, gridEdgeLeft.getFont().getSize())); 
				boolean valid = validateWidth();
				showValidWidth(valid);
				edgeRefLeft = Double.parseDouble(gridEdgeLeft.getText());
				if (valid) {
					double gridFieldWidthMeters = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefRight,edgeRefTop));
					gridWidth = Vincenty.metersToFeet(gridFieldWidthMeters) / Vincenty.FEET_PER_MILE;
					gridEdgeWidth.setText(widthFormat.format(gridWidth));
				}
			}
        });
        
        gridEdgeRight.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent kpe) {
				if (kpe.getKeyCode() == 10) {
					mapImageModelPanel.requestFocusInWindow();
				}					
			}
			@Override
			public void keyReleased(KeyEvent kre) {

			}
			@Override
			public void keyTyped(KeyEvent kte) {
				
			}
        });
        
        gridEdgeRight.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent fge) {
				gridEdgeRight.setFont(new Font(gridEdgeRight.getFont().getName(), Font.BOLD, gridEdgeRight.getFont().getSize())); 
			}
			@Override
			public void focusLost(FocusEvent fle) {
				gridEdgeRight.setFont(new Font(gridEdgeRight.getFont().getName(), Font.PLAIN, gridEdgeRight.getFont().getSize())); 
				boolean valid = validateWidth();
				showValidWidth(valid);
				edgeRefRight = Double.parseDouble(gridEdgeRight.getText());
				if (valid) {
					double gridFieldWidthMeters = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefRight,edgeRefTop));
					gridWidth = Vincenty.metersToFeet(gridFieldWidthMeters) / Vincenty.FEET_PER_MILE;
					gridEdgeWidth.setText(widthFormat.format(gridWidth));
				}
			}
        });
	}
     
	private void showValidWidth(boolean valid) {
		if (valid) {
			gridEdgeRight.setBackground(Color.WHITE); 
			gridEdgeLeft.setBackground(Color.WHITE);
			gridEdgeWidth.setBackground(Color.WHITE);
		} else {
			gridEdgeRight.setBackground(Color.RED);
			gridEdgeLeft.setBackground(Color.RED);
			gridEdgeWidth.setBackground(Color.RED);
		}
	}
	
	private void showValidHeight(boolean valid) {
		if (valid) {
			gridEdgeTop.setBackground(Color.WHITE); 
			gridEdgeBottom.setBackground(Color.WHITE);
			gridEdgeHeight.setBackground(Color.WHITE);
		} else {
			gridEdgeTop.setBackground(Color.RED);
			gridEdgeBottom.setBackground(Color.RED);
			gridEdgeHeight.setBackground(Color.RED);
		}
	}
	
    private void pickUpperLeftCornerButtonActionPerformed(ActionEvent event) {                                                          
        // TODO add your handling code here:
    }                                                         

    private void tileSelectionModeButtonActionPerformed(ActionEvent event) {                                                        
        // TODO add your handling code here:
    } 
    
    private void testGridWidthTextFieldActionPerformed(ActionEvent event) {                                                       
        // TODO add your handling code here:
    }                                                      

    private void testGridHeightTextFieldActionPerformed(ActionEvent event) {                                                        
        // TODO add your handling code here:
    }                                                     
    
    private boolean validateWidth() {
    	gridWidth = Double.parseDouble(gridEdgeWidth.getText());
		gridEdgeWidth.setText(widthFormat.format(gridWidth));
    	if (Math.abs(edgeRefLeft - edgeRefRight) < Vincenty.milesToDegrees(2.0, 90.0, degreesLatitude)) {
    		notifyError("Width of test field must be greater than 2 miles");
    		return false;
    	} else {
        	return true;
    	}
    }
    
    private boolean validateHeight() {
    	gridHeight = Double.parseDouble(gridEdgeHeight.getText());
		gridEdgeHeight.setText(heightFormat.format(gridHeight));
    	if (Math.abs(edgeRefTop - edgeRefBottom) < Vincenty.milesToDegrees(2.0, 0, degreesLatitude)) {
    		notifyError("Height of test field must be greater than 2 miles");
    		return false;
    	} else {
        	return true;
    	}
    }
    
    private void setGridEdgeWidthDimensions() {
    	double horizDelta = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefRight,edgeRefTop));
    	gridWidth = Vincenty.metersToFeet(horizDelta) / Vincenty.FEET_PER_MILE;
        gridEdgeWidth.setText(widthFormat.format((gridWidth)));
    }
    
    private void setGridEdgeHeightDimensions() {
    	double vertDelta = Vincenty.distanceToOnSurface(new Point.Double(edgeRefLeft,edgeRefTop), new Point.Double(edgeRefLeft,edgeRefBottom));
    	gridHeight = Vincenty.metersToFeet(vertDelta) / Vincenty.FEET_PER_MILE;
        gridEdgeHeight.setText(heightFormat.format((gridHeight)));
    }
    
    private void notifyError(String message) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(CoverageTestSettings.this, message, "Invalid Entry", JOptionPane.ERROR_MESSAGE);
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

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(okButton, 90,90,90)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(applyButton, 90,90,90)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cancelButton, 90,90,90)
				.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(applyButton)
					.addComponent(cancelButton)
					.addComponent(okButton))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		GroupLayout sampleDistributionAcrossTilePanelLayout = new GroupLayout(sampleDistributionAcrossTilePanel);
	    sampleDistributionAcrossTilePanel.setLayout(sampleDistributionAcrossTilePanelLayout);
	    
	    sampleDistributionAcrossTilePanelLayout.setHorizontalGroup(sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        .addGroup(sampleDistributionAcrossTilePanelLayout.createSequentialGroup()
	            .addContainerGap()
	            .addGroup(sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                .addComponent(timingSetBySpeedRadioButton)
	            	.addComponent(timingEvenAcrossTileRadioButton)
	                .addComponent(timingFrontWeightedRadioButton))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	    
	    sampleDistributionAcrossTilePanelLayout.setVerticalGroup(sampleDistributionAcrossTilePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        .addGroup(sampleDistributionAcrossTilePanelLayout.createSequentialGroup()
	            .addComponent(timingSetBySpeedRadioButton)
	        	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        	.addComponent(timingEvenAcrossTileRadioButton)
	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	            .addComponent(timingFrontWeightedRadioButton)));
	
	    GroupLayout earthShapeModelPanelLayout = new GroupLayout(earthShapeModelPanel);
	    earthShapeModelPanel.setLayout(earthShapeModelPanelLayout);
	    
	    earthShapeModelPanelLayout.setHorizontalGroup(earthShapeModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        .addGroup(earthShapeModelPanelLayout.createSequentialGroup()
	            .addContainerGap()
	            .addGroup(earthShapeModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
		            .addComponent(earthShapeModelFlatRadioButton)
		        	.addComponent(earthShapeModelEllipseRadioButton))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	    
	    earthShapeModelPanelLayout.setVerticalGroup(earthShapeModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        .addGroup(earthShapeModelPanelLayout.createSequentialGroup()
	            .addComponent(earthShapeModelFlatRadioButton)
	        	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        	.addComponent(earthShapeModelEllipseRadioButton)));
	    	    
	    GroupLayout samplingOptionsPanelLayout = new GroupLayout(samplingOptionsPanel);
	    samplingOptionsPanel.setLayout(samplingOptionsPanelLayout);
        
	    samplingOptionsPanelLayout.setHorizontalGroup(samplingOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(samplingOptionsPanelLayout.createSequentialGroup()
                .addGroup(samplingOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sampleSINADCheckBox)
                    .addComponent(sampleRSSICheckBox)
                    .addComponent(sampleBERCheckBox))));
	    
	        samplingOptionsPanelLayout.setVerticalGroup(samplingOptionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(samplingOptionsPanelLayout.createSequentialGroup()
	                .addComponent(sampleSINADCheckBox)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(sampleRSSICheckBox)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(sampleBERCheckBox)));
	    
	    GroupLayout coverageTestPanelLayout = new GroupLayout(coverageTestPanel);
	    coverageTestPanel.setLayout(coverageTestPanelLayout);

	    coverageTestPanelLayout.setHorizontalGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(coverageTestPanelLayout.createSequentialGroup()
            	.addContainerGap()
                .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(alertOnMinimumSamplesPerTileAcquiredCheckBox)
                    .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(sampleDistributionAcrossTilePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(coverageTestPanelLayout.createSequentialGroup()
                            .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tileSizeArcSecondsComboBoxLabel)
                                .addComponent(maxSamplesPerTileComboBoxLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(tileSizeArcSecondsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(maxSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(20,20,20)
                            .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(minSamplesPerTileComboBoxLabel)
                                .addComponent(dotsPerTileComboBoxLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(minSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(dotsPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addGroup(coverageTestPanelLayout.createSequentialGroup()
                            .addComponent(samplingOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(20,20,20)
                            .addComponent(earthShapeModelPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	    
        coverageTestPanelLayout.setVerticalGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(coverageTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(coverageTestPanelLayout.createSequentialGroup()
                        .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(dotsPerTileComboBoxLabel)
                            .addComponent(dotsPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(minSamplesPerTileComboBoxLabel)
                            .addComponent(minSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(coverageTestPanelLayout.createSequentialGroup()
                        .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(tileSizeArcSecondsComboBoxLabel)
                            .addComponent(tileSizeArcSecondsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(maxSamplesPerTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxSamplesPerTileComboBoxLabel))))
                .addGap(20,20,20)
                .addComponent(sampleDistributionAcrossTilePanel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(coverageTestPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(samplingOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(20,20,20)
                    .addComponent(earthShapeModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(alertOnMinimumSamplesPerTileAcquiredCheckBox)
                .addContainerGap()));
	    
        GroupLayout manualDataCollectionModeSelectorPanelLayout = new GroupLayout(manualDataCollectionModeSelectorPanel);
        manualDataCollectionModeSelectorPanel.setLayout(manualDataCollectionModeSelectorPanelLayout);
        
        manualDataCollectionModeSelectorPanelLayout.setHorizontalGroup(manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(manualDataCollectionModeSelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(manualDataCollectionContinuousModeRadioButton)
                    .addComponent(manualDataCollectionManualModeRadioButton))
                .addContainerGap()));
        
        manualDataCollectionModeSelectorPanelLayout.setVerticalGroup(manualDataCollectionModeSelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(manualDataCollectionModeSelectorPanelLayout.createSequentialGroup()
                .addComponent(manualDataCollectionContinuousModeRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(manualDataCollectionManualModeRadioButton)));

        GroupLayout manualModePanelLayout = new GroupLayout(manualModePanel);
        manualModePanel.setLayout(manualModePanelLayout);
        
        manualModePanelLayout.setHorizontalGroup(manualModePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(manualModePanelLayout.createSequentialGroup()
            	.addContainerGap()
                .addComponent(manualDataCollectionModeSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap()));
        
        manualModePanelLayout.setVerticalGroup(manualModePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(manualModePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manualDataCollectionModeSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap()));
        
        GroupLayout signalQualityDisplaySelectorPanelLayout = new GroupLayout(signalQualityDisplaySelectorPanel);
        
        signalQualityDisplaySelectorPanel.setLayout(signalQualityDisplaySelectorPanelLayout);
        
        signalQualityDisplaySelectorPanelLayout.setHorizontalGroup(
            signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(displaySinadRadioButton)
                    .addComponent(displayRssiRadioButton)
                    .addComponent(displayBerRadioButton))
                .addContainerGap()));
        
        signalQualityDisplaySelectorPanelLayout.setVerticalGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                .addComponent(displaySinadRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(displayRssiRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(displayBerRadioButton)));

        GroupLayout displaySettingsPanelLayout = new GroupLayout(displaySettingsPanel);
        displaySettingsPanel.setLayout(displaySettingsPanelLayout);
        
        displaySettingsPanelLayout.setHorizontalGroup(displaySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displaySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                        .addComponent(signalQualityDisplaySelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(20,20,20)
                        .addComponent(showGridSquareShadingCheckBox))
                    .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                        .addComponent(RSSIDataPointColoringPanel, 240,240,240)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BERDataPointColoringPanel, 210,210,210)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SINADDataPointColoringPanel, 210,210,210)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        displaySettingsPanelLayout.setVerticalGroup(displaySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                .addGroup(displaySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(signalQualityDisplaySelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(displaySettingsPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(showGridSquareShadingCheckBox)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displaySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(RSSIDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(BERDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(SINADDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout RSSIDataPointColoringPanelLayout = new GroupLayout(RSSIDataPointColoringPanel);
        RSSIDataPointColoringPanel.setLayout(RSSIDataPointColoringPanelLayout);
        
        RSSIDataPointColoringPanelLayout.setHorizontalGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel90dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField90dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel80dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField80dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel70dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField70dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel60dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField60dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel50dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField50dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel100dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField100dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel110dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField110dBm, 30,30,30))
                    .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel120dBm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField120dBm, 30,30,30)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        RSSIDataPointColoringPanelLayout.setVerticalGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(RSSIDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50dBm)
                    .addComponent(jTextField50dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel60dBm)
                    .addComponent(jTextField60dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel70dBm)
                    .addComponent(jTextField70dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80dBm)
                    .addComponent(jTextField80dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel90dBm)
                    .addComponent(jTextField90dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel100dBm)
                    .addComponent(jTextField100dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel110dBm)
                    .addComponent(jTextField110dBm, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSSIDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel120dBm)
                    .addComponent(jTextField120dBm, 14,14,14))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout BERDataPointColoringPanelLayout = new GroupLayout(BERDataPointColoringPanel);
        BERDataPointColoringPanel.setLayout(BERDataPointColoringPanelLayout);
        
        BERDataPointColoringPanelLayout.setHorizontalGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField20ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField15ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel10ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField10ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel0ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField0ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel25ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField25ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel30ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField30ber, 30,30,30))
                    .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel35ber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField35ber, 30,30,30)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        BERDataPointColoringPanelLayout.setVerticalGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(BERDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel0ber)
                    .addComponent(jTextField0ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5ber)
                    .addComponent(jTextField5ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10ber)
                    .addComponent(jTextField10ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15ber)
                    .addComponent(jTextField15ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20ber)
                    .addComponent(jTextField20ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25ber)
                    .addComponent(jTextField25ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30ber)
                    .addComponent(jTextField30ber, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BERDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35ber)
                    .addComponent(jTextField35ber, 14,14,14))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout SINADDataPointColoringPanelLayout = new GroupLayout(SINADDataPointColoringPanel);
        SINADDataPointColoringPanel.setLayout(SINADDataPointColoringPanelLayout);
        
        SINADDataPointColoringPanelLayout.setHorizontalGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField20sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField15sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel10sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField10sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel0sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField0sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel25sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField25sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel30sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField30sinad, 30,30,30))
                    .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                        .addComponent(jLabel12sinad)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField12sinad, 30,30,30)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        SINADDataPointColoringPanelLayout.setVerticalGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(SINADDataPointColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel0sinad)
                    .addComponent(jTextField0sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5sinad)
                    .addComponent(jTextField5sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10sinad)
                    .addComponent(jTextField10sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12sinad)
                    .addComponent(jTextField12sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15sinad)
                    .addComponent(jTextField15sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20sinad)
                    .addComponent(jTextField20sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25sinad)
                    .addComponent(jTextField25sinad, 14,14,14))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SINADDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30sinad)
                    .addComponent(jTextField30sinad, 14,14,14))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout jccDialogLayout = new GroupLayout(jccDialog.getContentPane());
		jccDialog.getContentPane().setLayout(jccDialogLayout);
        
		jccDialogLayout.setHorizontalGroup(jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, jccDialogLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jccApply, 90,90,90)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jccCancel, 90,90,90)
                .addGap(134, 134, 134))
            .addGroup(jccDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jcc, 450,450,450)
                .addContainerGap()));
		
	        jccDialogLayout.setVerticalGroup(
	            jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jccDialogLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(jccApply)
	                    .addComponent(jccCancel))
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	        
	        GroupLayout testGridPanelLayout = new GroupLayout(testGridPanel);
	        testGridPanel.setLayout(testGridPanelLayout);
	        
	        testGridPanelLayout.setHorizontalGroup(manualModePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(testGridPanelLayout.createSequentialGroup()
	            	.addContainerGap()
	                .addComponent(testGridDimensionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addContainerGap()));
	        
	        testGridPanelLayout.setVerticalGroup(testGridPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(testGridPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(testGridDimensionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addContainerGap()));
	        
	        GroupLayout mapImageModelPanelLayout = new GroupLayout(mapImageModelPanel);
	        mapImageModelPanel.setLayout(mapImageModelPanelLayout);
	        
	        mapImageModelPanelLayout.setHorizontalGroup(
	                mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                    .addContainerGap()
	                    .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                            .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
	                                .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                                    .addComponent(gridEdgeRightLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                                    .addComponent(gridEdgeRight, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
	                                .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                                    .addComponent(gridEdgeBottomLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                                    .addComponent(gridEdgeBottom, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)))
	                            .addContainerGap())
	                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                            .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                                .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                                    .addComponent(gridEdgeLeft, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                                    .addComponent(gridEdgeLeftLabel, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
	                                .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                                    .addComponent(gridEdgeTop, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                                    .addComponent(gridEdgeTopLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
	                            .addContainerGap(10, Short.MAX_VALUE)))));
	        
	        mapImageModelPanelLayout.setVerticalGroup(
	            mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(mapImageModelPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeTop, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeTopLabel))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeLeft, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeLeftLabel))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeBottom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeBottomLabel))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeRight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeRightLabel))
	                .addContainerGap()));

	        GroupLayout testGridDimensionPanelLayout = new GroupLayout(testGridDimensionPanel);
	        testGridDimensionPanel.setLayout(testGridDimensionPanelLayout);
	        
	        testGridDimensionPanelLayout.setHorizontalGroup(
	                testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
	                    .addContainerGap()
	                    .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
	                        .addComponent(pickUpperLeftCornerButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(tileSelectionModeButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
	                    .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                        .addGroup(GroupLayout.Alignment.TRAILING, testGridDimensionPanelLayout.createSequentialGroup()
	                            .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
	                                .addComponent(gridEdgeWidthLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                                .addComponent(gridEdgeHeightLabel, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
	                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
	                                .addComponent(gridEdgeHeight)
	                                .addComponent(gridEdgeWidth, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
	                            .addContainerGap(70, Short.MAX_VALUE))
	                        .addComponent(mapImageModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
	        
	        testGridDimensionPanelLayout.setVerticalGroup(
	            testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
	                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                    .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
	                        .addGap(40, 40, 40)
	                        .addComponent(pickUpperLeftCornerButton)
	                        .addGap(20, 20, 20)
	                        .addComponent(tileSelectionModeButton))
	                    .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
	                        .addGap(20, 20, 20)
	                        .addComponent(mapImageModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
	                .addGap(20, 20, 20)
	                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeWidthLabel))
	                .addGap(8, 8, 8)
	                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(gridEdgeHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addComponent(gridEdgeHeightLabel))
	                .addContainerGap(35, Short.MAX_VALUE)));    
	            
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();

		jccDialog.pack();
		jccDialog.setLocation((screenSize.width / 2) - (jccDialog.getWidth() / 2),
				(screenSize.height / 2) - (jccDialog.getHeight() / 2));
		
		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
	}

	private void getSettingsFromRegistry() {
		earthShapeModelIndex = systemPrefs.getInt("EarthShapeModelIndex", 1);
		dotsPerTileIndex = systemPrefs.getInt("DotsPerTileIndex", 5);
		tileSizeIndex = systemPrefs.getInt("SizeOfTileArcSeconds", 6);
		minSamplesPerTileIndex = systemPrefs.getInt("MinSamplesPerTile", 6);
		maxSamplesPerTileIndex = systemPrefs.getInt("MaxSamplesPerTile", 6);
		alertOnMinimumSamplesPerTileAcquired = systemPrefs.getBoolean("AlertOnMinimumSamplesPerTileAcquired", false);
		showGridSquareShading = systemPrefs.getBoolean("ShowGridSquareShading", true);
		showRings = systemPrefs.getBoolean("ShowRings", true);
		showQuads = systemPrefs.getBoolean("ShowQuads", true);
		showLines = systemPrefs.getBoolean("ShowLines", true);
		sampleTimingMode = systemPrefs.getInt("SampleTimingMode", 1);
		signalQualityDisplayMode = systemPrefs.getInt("SignalQualityDisplayMode", 1);
		manualDataCollectionMode = systemPrefs.getInt("ManualDataCollectionMode", 0);
		sampleRSSI = systemPrefs.getBoolean("SampleRSSI", false);
		sampleBER = systemPrefs.getBoolean("SampleBER", false);
		sampleSINAD = systemPrefs.getBoolean("SampleSINAD", false);
		signalMarkerRadius = systemPrefs.getDouble("SignalMarkerRadius", 3d);
		edgeRefTop = systemPrefs.getDouble("GridEdgeTop", DEFAULT_TEST_GRID_TOP_EDGE);
		edgeRefLeft = systemPrefs.getDouble("GridEdgeLeft", DEFAULT_TEST_GRID_LEFT_EDGE);
		edgeRefBottom = systemPrefs.getDouble("GridEdgeBottom", DEFAULT_TEST_GRID_BOTTOM_EDGE);
		edgeRefRight = systemPrefs.getDouble("GridEdgeRight", DEFAULT_TEST_GRID_RIGHT_EDGE);
		color50dBm = new Color(systemPrefs.getInt("Color50dBm", new Color(255, 255, 255).getRGB()));
		color60dBm = new Color(systemPrefs.getInt("Color60dBm", new Color(223, 223, 255).getRGB()));
		color70dBm = new Color(systemPrefs.getInt("Color70dBm", new Color(191, 191, 255).getRGB()));
		color80dBm = new Color(systemPrefs.getInt("Color80dBm", new Color(159, 159, 255).getRGB()));
		color90dBm = new Color(systemPrefs.getInt("Color90dBm", new Color(127, 127, 255).getRGB()));
		color100dBm = new Color(systemPrefs.getInt("Color100dBm", new Color(95, 95, 255).getRGB()));
		color110dBm = new Color(systemPrefs.getInt("Color110dBm", new Color(63, 63, 255).getRGB()));
		color120dBm = new Color(systemPrefs.getInt("Color120dBm", new Color(31, 31, 255).getRGB()));
		color0sinad = new Color(systemPrefs.getInt("Color0sinad", new Color(255, 0, 0).getRGB()));
		color5sinad = new Color(systemPrefs.getInt("Color5sinad", new Color(255, 0, 0).getRGB()));
		color10sinad = new Color(systemPrefs.getInt("Color10sinad", new Color(255, 0, 0).getRGB()));
		color12sinad = new Color(systemPrefs.getInt("Color12sinad", new Color(255, 0, 0).getRGB()));
		color15sinad = new Color(systemPrefs.getInt("Color15sinad", new Color(255, 0, 0).getRGB()));
		color20sinad = new Color(systemPrefs.getInt("Color20sinad", new Color(255, 0, 0).getRGB()));
		color25sinad = new Color(systemPrefs.getInt("Color25sinad", new Color(255, 0, 0).getRGB()));
		color30sinad = new Color(systemPrefs.getInt("Color30sinad", new Color(255, 0, 0).getRGB()));
		color0ber = new Color(systemPrefs.getInt("Color0ber", new Color(255, 0, 0).getRGB()));
		color5ber = new Color(systemPrefs.getInt("Color5ber", new Color(255, 0, 0).getRGB()));
		color10ber = new Color(systemPrefs.getInt("Color10ber", new Color(255, 0, 0).getRGB()));
		color15ber = new Color(systemPrefs.getInt("Color15ber", new Color(255, 0, 0).getRGB()));
		color20ber = new Color(systemPrefs.getInt("Color20ber", new Color(255, 0, 0).getRGB()));
		color25ber = new Color(systemPrefs.getInt("Color25ber", new Color(255, 0, 0).getRGB()));
		color30ber = new Color(systemPrefs.getInt("Color30ber", new Color(255, 0, 0).getRGB()));
		color35ber = new Color(systemPrefs.getInt("Color35ber", new Color(255, 0, 0).getRGB()));
	}

	public void showSettingsDialog(boolean newShowSettingsDialog) {
		setVisible(newShowSettingsDialog);
	}

	private void applyButtonActionListenerEvent(ActionEvent event) {
		
		signalMarkerRadius = Double.parseDouble(tfSignalMarkerRadius.getText());
		
		systemPrefs.putInt("EarthShapeModelIndex", earthShapeModelIndex);
		systemPrefs.putInt("DotsPerTileIndex", dotsPerTileIndex);
		systemPrefs.putInt("SizeOfTileArcSeconds", tileSizeIndex);
		systemPrefs.putInt("MinSamplesPerTile", minSamplesPerTileIndex);
		systemPrefs.putInt("MaxSamplesPerTile", maxSamplesPerTileIndex);
		systemPrefs.putBoolean("AlertOnMinimumSamplesPerTileAcquired",	alertOnMinimumSamplesPerTileAcquired);
		systemPrefs.putBoolean("ShowGridSquareShading", showGridSquareShading);
		systemPrefs.putBoolean("ShowRings", showRings);
		systemPrefs.putBoolean("ShowQuads", showQuads);
		systemPrefs.putBoolean("ShowLines", showLines);
		systemPrefs.putBoolean("SampleRSSI", sampleRSSI);
		systemPrefs.putBoolean("SampleBER", sampleBER);
		systemPrefs.putBoolean("SampleSINAD", sampleSINAD);
		systemPrefs.putInt("SampleTimingMode", sampleTimingMode);
		systemPrefs.putInt("SignalQualityDisplayMode", signalQualityDisplayMode);
		systemPrefs.putInt("ManualDataCollectionMode", manualDataCollectionMode);
		systemPrefs.putInt("Color50dBm", color50dBm.getRGB());
		systemPrefs.putInt("Color60dBm", color60dBm.getRGB());
		systemPrefs.putInt("Color70dBm", color70dBm.getRGB());
		systemPrefs.putInt("Color80dBm", color80dBm.getRGB());
		systemPrefs.putInt("Color90dBm", color90dBm.getRGB());
		systemPrefs.putInt("Color100dBm", color100dBm.getRGB());
		systemPrefs.putInt("Color110dBm", color110dBm.getRGB());
		systemPrefs.putInt("Color120dBm", color120dBm.getRGB());
		systemPrefs.putInt("Color0sinad", color0sinad.getRGB());
		systemPrefs.putInt("Color5sinad", color5sinad.getRGB());
		systemPrefs.putInt("Color10sinad", color10sinad.getRGB());
		systemPrefs.putInt("Color12sinad", color12sinad.getRGB());
		systemPrefs.putInt("Color15sinad", color15sinad.getRGB());
		systemPrefs.putInt("Color20sinad", color20sinad.getRGB());
		systemPrefs.putInt("Color25sinad", color25sinad.getRGB());
		systemPrefs.putInt("Color30sinad", color30sinad.getRGB());
		systemPrefs.putInt("Color0ber", color0ber.getRGB());
		systemPrefs.putInt("Color5ber", color5ber.getRGB());
		systemPrefs.putInt("Color10ber", color10ber.getRGB());
		systemPrefs.putInt("Color15ber", color15ber.getRGB());
		systemPrefs.putInt("Color20ber", color20ber.getRGB());
		systemPrefs.putInt("Color25ber", color25ber.getRGB());
		systemPrefs.putInt("Color30ber", color30ber.getRGB());
		systemPrefs.putInt("Color35ber", color35ber.getRGB());
		systemPrefs.putDouble("SignalMarkerRadius", signalMarkerRadius);
		systemPrefs.putDouble("GridEdgeBottom", edgeRefBottom);
		systemPrefs.putDouble("GridEdgeRight", edgeRefRight);
		systemPrefs.putDouble("GridEdgeTop", edgeRefTop);
		systemPrefs.putDouble("GridEdgeLeft", edgeRefLeft);
		
		updateComponents();
		
		firePropertyChange("PROPERTY_CHANGE", null, null);
	}

	private void setTileShapeValues(double degreesLatitude) {
		this.degreesLatitude = degreesLatitude;
		double requestedLatitudeFeet = 0;
		double requestedArcSeconds = 0;
		switch (tileSizeIndex) {
			case 0:
				requestedLatitudeFeet = 10;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 1:
				requestedLatitudeFeet = 20;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 2: 
				requestedLatitudeFeet = 40;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 3:
				requestedLatitudeFeet = 50;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 4:
				requestedLatitudeFeet = 100;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 5:
				requestedLatitudeFeet = 5280.0 / 4.0;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 6:
				requestedLatitudeFeet = 5280.0 / 2.0;
				requestedArcSeconds = Vincenty.feetToArcSeconds(requestedLatitudeFeet, 0, degreesLatitude);
				break;
			case 7:
				requestedArcSeconds = 1.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 8:
				requestedArcSeconds = 3.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 9:
				requestedArcSeconds = 5.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 10:
				requestedArcSeconds = 10.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 11:
				requestedArcSeconds = 15.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 12:
				requestedArcSeconds = 30.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 13:
				requestedArcSeconds = 60.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 14:
				requestedArcSeconds = 90.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 15:
				requestedArcSeconds = 120.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
				break;
			case 16:
				requestedArcSeconds = 180.0;
				requestedLatitudeFeet = Vincenty.arcSecondsToFeet(requestedArcSeconds, 0, degreesLatitude);
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

	private void updateComponents() {	
		
		dotsPerTileComboBox.setSelectedIndex(dotsPerTileIndex);
		
		tileSizeArcSecondsComboBox.setSelectedIndex(tileSizeIndex);
		
		minSamplesPerTileComboBox.setSelectedIndex(minSamplesPerTileIndex);
		
		maxSamplesPerTileComboBox.setSelectedIndex(maxSamplesPerTileIndex);
		
		alertOnMinimumSamplesPerTileAcquiredCheckBox.setSelected(alertOnMinimumSamplesPerTileAcquired);
		
		showGridSquareShadingCheckBox.setSelected(showGridSquareShading);
		showRingsCheckBox.setSelected(showRings);
		showQuadsCheckBox.setSelected(showQuads);
		showLinesCheckBox.setSelected(showLines);
		
		sampleRSSICheckBox.setSelected(sampleRSSI);
		sampleBERCheckBox.setSelected(sampleBER);
		sampleSINADCheckBox.setSelected(sampleSINAD);
		
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

		gridEdgeTop.setText(latFormat.format(edgeRefTop));
		gridEdgeLeft.setText(lonFormat.format(edgeRefLeft));
		gridEdgeBottom.setText(latFormat.format(edgeRefBottom));
		gridEdgeRight.setText(latFormat.format(edgeRefRight));
		
		setGridEdgeHeightDimensions();
		setGridEdgeWidthDimensions();
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
	
	private void showRingsCheckBoxItemStateChanged(ItemEvent event) {
		showRings = showRingsCheckBox.isSelected();
	}
	
	private void showQuadsCheckBoxItemStateChanged(ItemEvent event) {
		showQuads = showQuadsCheckBox.isSelected();
	}
	
	private void showLinesCheckBoxItemStateChanged(ItemEvent event) {
		showLines = showLinesCheckBox.isSelected();
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
	
	public Point.Double getGridReference() {
		return new Point.Double(edgeRefRight, edgeRefTop);
	}
	
	public void setControlsEnabled(boolean enabled) {
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
	
	public boolean isRSSIMode() {
		return sampleRSSI;
	}
	
	public int getSampleTimingMode() {
		return sampleTimingMode;
	}
	
	public double getSignalMarkerRadius() {
		return signalMarkerRadius;
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
		this.degreesLatitude = degreesLatitude;
		setTileShapeValues(degreesLatitude);
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
	
	public boolean isShowRings() {
		return showRings;
	}
	
	public boolean isShowQuads() {
		return showQuads;
	}
	
	public boolean isShowLines() {
		return showLines;
	}
	
	public int getMinTimePerTile() {
		return minTimePerTile;
	}
	
	public void setDegreesLatitude(double degreesLatitude) {
		this.degreesLatitude = degreesLatitude;
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