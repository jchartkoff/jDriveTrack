package jdrivetrack;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageMapSelector extends JDialog {

    private JButton addButton;
    private JButton cancelButton;
    private JButton okButton;
    private JLabel cityLabel;
    private JTextField cityTextField;
    private JButton deleteButton;
    private JTextField descriptionTextField;
    private JTextField eastEdgeLongitudeTextField;
    private JLabel descriptionLabel;
    private JLabel urlLabel;
    private JComboBox<Object> mapChooserComboBox;
    private JPanel mapSettingsPanel;
    private JTextField northEdgeLatitudeTextField;
    private JButton applyButton;
    private JLabel selectMapLabel;
    private JTextField southEdgeLatitudeTextField;
    private JLabel stateLabel;
    private JTextField stateTextField;
    private JButton urlBrowseButton;
    private JTextField urlTextField;
    private JTextField westEdgeLongitudeTextField;
    private Toolkit tk;
	private Dimension screenSize;
	private Preferences userPref;
	private int selectedMapIndex;
	private ReadCsvFile iniFile;
	private String lastMapFileDirectory;
	private JLabel verticalDistanceLabel;
    private JTextField verticalDistanceTextField;
    private JLabel horizontalDistanceLabel;
    private JTextField horizontalDistanceTextField;
    private JLabel scaleLabel;
    private JTextField scaleTextField;
    private Dimension mapPixels = new Dimension(800, 600);
    private double mapSizeFeetVertical;
    private double mapSizeFeetHorizontal;
    private double northEdgeLatitude;
    private double southEdgeLatitude;
    private double eastEdgeLongitude;
    private double westEdgeLongitude;
    private double mapScale;
    private NumberFormat coordinateFormat = new DecimalFormat("##0.000000###");
    private NumberFormat mapDistanceFormat = new DecimalFormat("#######000");
    private NumberFormat mapScaleFormat = new DecimalFormat("########00");
	private ArrayList<ChangeListener> propertiesChangedChangeListeners;
	private double screenResolution;
	private double horizontalMapInches;
	
	private static final long serialVersionUID = 1L;

    public ImageMapSelector() {
    	setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    	setTitle("Image Map Selector");
    	tk = Toolkit.getDefaultToolkit();
		screenSize = tk.getScreenSize();
        getSettingsFromRegistry();
		initComponents();
    }

    public void showSettingsDialog(boolean newShowSettingsDialog) {
		setVisible(newShowSettingsDialog);
	}
    
    private void getSettingsFromRegistry() {
    	userPref = Preferences.userRoot().node(this.getClass().getName());
		selectedMapIndex = userPref.getInt("SelectedMapIndex", -1);
		lastMapFileDirectory = userPref.get("LastMapFileDirectory", null);
	}
    
    private void initComponents() {
        mapSettingsPanel = new JPanel();
        mapChooserComboBox = new JComboBox<Object>();
        selectMapLabel = new JLabel();
        westEdgeLongitudeTextField = new JTextField();
        eastEdgeLongitudeTextField = new JTextField();
        northEdgeLatitudeTextField = new JTextField();
        southEdgeLatitudeTextField = new JTextField();
        cityTextField = new JTextField();
        cityLabel = new JLabel();
        stateLabel = new JLabel();
        stateTextField = new JTextField();
        descriptionTextField = new JTextField();
        descriptionLabel = new JLabel();
        urlTextField = new JTextField();
        urlLabel = new JLabel();
        urlBrowseButton = new JButton();
        applyButton = new JButton();
        cancelButton = new JButton();
        addButton = new JButton();
        deleteButton = new JButton();
        okButton = new JButton();
        verticalDistanceTextField = new JTextField();
        horizontalDistanceTextField = new JTextField();
        verticalDistanceLabel = new JLabel();
        horizontalDistanceLabel = new JLabel();
        scaleTextField = new JTextField();
        scaleLabel = new JLabel();
        
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        mapSettingsPanel.setBorder(BorderFactory.createTitledBorder("Map Settings"));

        mapChooserComboBox.setModel(new DefaultComboBoxModel<Object>());

        try {
        	String userDir = System.getProperty("user.dir");
			iniFile = new ReadCsvFile(userDir + File.separator + "maps" + File.separator + "map.ini");
			for(int i = 0; i < iniFile.getRowCount(); i++) {
				mapChooserComboBox.addItem(iniFile.getRow(i)[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        mapChooserComboBox.setSelectedIndex(selectedMapIndex);

        westEdgeLongitudeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        westEdgeLongitudeTextField.setToolTipText("Longitude at West Edge of Map");
        westEdgeLongitudeTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                westEdgeLongitudeTextFieldActionPerformed(evt);
            }
        });

        eastEdgeLongitudeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        eastEdgeLongitudeTextField.setToolTipText("Longitude at East Edge of Map");
        eastEdgeLongitudeTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                eastEdgeLongitudeTextFieldActionPerformed(evt);
            }
        });

        northEdgeLatitudeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        northEdgeLatitudeTextField.setToolTipText("Latitude at North Edge of Map");
        northEdgeLatitudeTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                northEdgeLatitudeTextFieldActionPerformed(evt);
            }
        });

        southEdgeLatitudeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        southEdgeLatitudeTextField.setToolTipText("Latitude at South Edge of Map");
        southEdgeLatitudeTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                southEdgeLatitudeTextFieldActionPerformed(evt);
            }
        });
        
        verticalDistanceTextField.setHorizontalAlignment(SwingConstants.CENTER);
        verticalDistanceTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                verticalDistanceTextFieldActionPerformed(evt);
            }
        });
        
        horizontalDistanceTextField.setHorizontalAlignment(SwingConstants.CENTER);
        horizontalDistanceTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                horizontalDistanceTextFieldActionPerformed(evt);
            }
        });

        scaleTextField.setHorizontalAlignment(SwingConstants.CENTER);
        scaleTextField.setToolTipText("Number of feet per inch on the screen");
        scaleTextField.setEditable(false);
        scaleTextField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                scaleTextFieldActionPerformed(evt);
            }
        });
        
        verticalDistanceLabel.setText("Vertical Distance Across Map in Feet");

        horizontalDistanceLabel.setText("Horizontal Distance Across Map in Feet");
        
        scaleLabel.setText("Scale Feet per Inch");

        GroupLayout mapSettingsPanelLayout = new GroupLayout(mapSettingsPanel);
        mapSettingsPanel.setLayout(mapSettingsPanelLayout);
        mapSettingsPanelLayout.setHorizontalGroup(
            mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(cityLabel)
                    .addComponent(selectMapLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(urlLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionTextField)
                            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                                .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, 437, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(urlBrowseButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(mapChooserComboBox, GroupLayout.Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(verticalDistanceLabel)
                            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                                .addComponent(cityTextField, GroupLayout.PREFERRED_SIZE, 148, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stateLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stateTextField, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5))
                            .addComponent(horizontalDistanceLabel)
                            .addComponent(scaleLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                                .addComponent(verticalDistanceTextField, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(northEdgeLatitudeTextField, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                .addGap(69, 69, 69))
                            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(scaleTextField, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                    .addComponent(horizontalDistanceTextField))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(GroupLayout.Alignment.TRAILING, mapSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(westEdgeLongitudeTextField, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(eastEdgeLongitudeTextField, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                        .addGap(22, 22, 22))
                                    .addGroup(GroupLayout.Alignment.TRAILING, mapSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(southEdgeLatitudeTextField, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                        .addGap(68, 68, 68)))))))));
        
        mapSettingsPanelLayout.setVerticalGroup(
            mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(selectMapLabel)
                    .addComponent(mapChooserComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(verticalDistanceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(northEdgeLatitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(verticalDistanceLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(eastEdgeLongitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(westEdgeLongitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(horizontalDistanceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(horizontalDistanceLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(southEdgeLatitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(scaleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(scaleLabel))
                        .addGap(58, 58, 58))
                    .addGroup(mapSettingsPanelLayout.createSequentialGroup()
                        .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cityTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(stateLabel)
                            .addComponent(stateTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(cityLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mapSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(urlLabel)
                    .addComponent(urlBrowseButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        selectMapLabel.setText("Select Map");

        cityLabel.setText("City");

        stateLabel.setText("State");

        descriptionLabel.setText("Description");

        urlLabel.setText("URL");

        mapChooserComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mapChooserComboBoxPropertyChange(evt);	
            }
        });

        urlBrowseButton.setText("Browse");
        
        urlBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                urlBrowseButtonMouseClicked(event);
            }
        });
        
        applyButton.setText("Apply");

        applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonMouseClicked(event);
			}
		});
        
        cancelButton.setText("Cancel");

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelButtonMouseClicked(event);
            }
        });
        
        addButton.setText("Add");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                addButtonMouseClicked(event);
            }
        });
        
        okButton.setText("OK");

        okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});
        
        deleteButton.setText("Delete");

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                deleteButtonMouseClicked(event);
            }
        });
        
        GroupLayout layout = new GroupLayout(getContentPane());
        
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(mapSettingsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGap(50, 130, Short.MAX_VALUE)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applyButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap()));
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapSettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                	.addComponent(okButton)
                	.addComponent(applyButton)
                    .addComponent(cancelButton)
                    .addComponent(addButton)
                    .addComponent(deleteButton))
                .addContainerGap(19, Short.MAX_VALUE)));

        pack();
        
        setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
    }

    protected void scaleTextFieldActionPerformed(ActionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	protected void horizontalDistanceTextFieldActionPerformed(ActionEvent evt) {
		mapSizeFeetHorizontal = Double.parseDouble(horizontalDistanceTextField.getText());
		mapSizeFeetVertical = mapSizeFeetHorizontal * mapPixels.height / mapPixels.width;
		verticalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetVertical)));
		eastEdgeLongitude = (Vincenty.metersToDegrees(1000.0, northEdgeLatitude).x *
				((mapSizeFeetHorizontal / Vincenty.metersToFeet(1.0)) / 1000.0)) +
				westEdgeLongitude;
		eastEdgeLongitudeTextField.setText(coordinateFormat.format(eastEdgeLongitude));
		mapScale = mapSizeFeetHorizontal / horizontalMapInches;
		scaleTextField.setText(mapScaleFormat.format(mapScale));
	}

	protected void verticalDistanceTextFieldActionPerformed(ActionEvent evt) {
		mapSizeFeetVertical = Double.parseDouble(verticalDistanceTextField.getText());
		mapSizeFeetHorizontal = mapSizeFeetVertical * mapPixels.width / mapPixels.height;
		horizontalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetHorizontal)));
		northEdgeLatitude = (Vincenty.metersToDegrees(1000.0, 0.0).y *
				((mapSizeFeetVertical / Vincenty.metersToFeet(1.0)) / 1000.0)) +
				southEdgeLatitude;
		northEdgeLatitudeTextField.setText(coordinateFormat.format(northEdgeLatitude));
		mapScale = mapSizeFeetHorizontal / horizontalMapInches;
		scaleTextField.setText(mapScaleFormat.format(mapScale));
	}

	protected void southEdgeLatitudeTextFieldActionPerformed(ActionEvent evt) {
		southEdgeLatitude = Double.parseDouble(southEdgeLatitudeTextField.getText());
	}

	protected void northEdgeLatitudeTextFieldActionPerformed(ActionEvent evt) {
		northEdgeLatitude = Double.parseDouble(northEdgeLatitudeTextField.getText());
		mapSizeFeetVertical = Utility.getGreatCircleDistance(southEdgeLatitude, eastEdgeLongitude,
				northEdgeLatitude, eastEdgeLongitude) * 1000.0 * Vincenty.metersToFeet(1.0);
		verticalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetVertical)));
	}

	protected void eastEdgeLongitudeTextFieldActionPerformed(ActionEvent evt) {
		eastEdgeLongitude = Double.parseDouble(eastEdgeLongitudeTextField.getText());
		mapSizeFeetHorizontal = Utility.getGreatCircleDistance(southEdgeLatitude, eastEdgeLongitude,
				southEdgeLatitude, westEdgeLongitude) * 1000.0 * Vincenty.metersToFeet(1.0);
		horizontalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetHorizontal)));
		mapScale = mapSizeFeetHorizontal / horizontalMapInches;
		scaleTextField.setText(mapScaleFormat.format(mapScale));
	}

	protected void westEdgeLongitudeTextFieldActionPerformed(ActionEvent evt) {
		westEdgeLongitude = Double.parseDouble(westEdgeLongitudeTextField.getText());
	}

	private void mapChooserComboBoxPropertyChange(PropertyChangeEvent evt) {
    	if (mapChooserComboBox.getSelectedIndex() > -1) {
			cityTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[2]);
			stateTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[3]);
			urlTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[4]);
			westEdgeLongitudeTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[5]);
			eastEdgeLongitudeTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[7]);
			northEdgeLatitudeTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[6]);
			southEdgeLatitudeTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[8]);
			descriptionTextField.setText(iniFile.getRow(mapChooserComboBox.getSelectedIndex())[1]);
			northEdgeLatitude = Double.parseDouble(northEdgeLatitudeTextField.getText());
			southEdgeLatitude = Double.parseDouble(southEdgeLatitudeTextField.getText());
			eastEdgeLongitude = Double.parseDouble(eastEdgeLongitudeTextField.getText());
			westEdgeLongitude = Double.parseDouble(westEdgeLongitudeTextField.getText());
			mapSizeFeetHorizontal = Utility.getGreatCircleDistance(northEdgeLatitude, westEdgeLongitude, 
					northEdgeLatitude, eastEdgeLongitude) * 1000.0 * Vincenty.metersToFeet(1.0);
			horizontalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetHorizontal)));
			mapSizeFeetVertical = Utility.getGreatCircleDistance(northEdgeLatitude, westEdgeLongitude, 
					southEdgeLatitude, westEdgeLongitude) * 1000.0 * Vincenty.metersToFeet(1.0);
			verticalDistanceTextField.setText(mapDistanceFormat.format(Math.round(mapSizeFeetVertical)));
			screenResolution = Utility.getScreenResolution(null);
			horizontalMapInches = mapPixels.width / screenResolution;
			mapScale = Math.round(mapSizeFeetHorizontal / horizontalMapInches);
			scaleTextField.setText(mapScaleFormat.format(mapScale));
    	}
	}

	private void urlBrowseButtonMouseClicked(ActionEvent event) {
		JFileChooser fileChooser = new JFileChooser();
		if (lastMapFileDirectory != null)
			fileChooser.setCurrentDirectory(new File(lastMapFileDirectory));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Map Files", "jpg");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle("Select Map File");
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			lastMapFileDirectory = fileChooser.getCurrentDirectory().getPath();
			userPref.put("LastMapFileDirectory", lastMapFileDirectory);
			urlTextField.setText(fileChooser.getCurrentDirectory().getPath()
					+ (char) 92 + fileChooser.getSelectedFile().getName());
			remove(fileChooser);
		} else if (returnVal == JFileChooser.CANCEL_OPTION) {
			remove(fileChooser);
		}
	}

	private void applyButtonMouseClicked(ActionEvent event) {
		userPref.putInt("SelectedMapIndex", mapChooserComboBox.getSelectedIndex());	
		try {
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 1, descriptionTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 2, cityTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 3, stateTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 4, urlTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 5, westEdgeLongitudeTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 6, northEdgeLatitudeTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 7, eastEdgeLongitudeTextField.getText());
			iniFile.setValue(mapChooserComboBox.getSelectedIndex(), 8, southEdgeLatitudeTextField.getText());
		} catch (IOException e) {
			e.printStackTrace();
		}
		propertyStateChanged();
	}

	private void cancelButtonMouseClicked(ActionEvent event) {
		setVisible(false);
	}

	private void addButtonMouseClicked(ActionEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void deleteButtonMouseClicked(ActionEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void addPropertiesChangedChangeListener(ChangeListener cl) {
		if (propertiesChangedChangeListeners == null) {
			propertiesChangedChangeListeners = new ArrayList<ChangeListener>();
		}
		propertiesChangedChangeListeners.add(cl);
	}

	private void propertyStateChanged() {
		if (propertiesChangedChangeListeners != null) {
			for (ChangeListener cl : propertiesChangedChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
	}
	
}

