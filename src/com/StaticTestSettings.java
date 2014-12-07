package com;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.jdesktop.layout.GroupLayout;

public class StaticTestSettings extends JDialog {
	private static final long serialVersionUID = 4676114275202217003L;
	
	private JPanel staticSignalLocationSettingsPanel;
    private JPanel flightColoringPanel;
	private JTabbedPane tabbedPane;
	private Preferences userPref;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JLabel maxCattLabel;
	private JLabel cursorDiameterLabel;
	
	private double maxCatt;
	private int cursorDiameter;
	
	private JTextField tfMaxCatt;
    private JTextField tfCursorDiameter;
	
	private JCheckBox cbShowArcs;
	private JCheckBox cbShowAsymptotes;
	private JCheckBox cbShowCursors;
	private JCheckBox cbShowTrails;
	private JCheckBox cbTrailEqualsFlightColor;;
	private JCheckBox cbShowTargetRing;
	private JCheckBox cbShowIntersectPoints;
	
	private boolean isShowArcs;
	private boolean isShowAsymptotes;
	private boolean isShowCursors;
	private boolean isShowTrails;
	private boolean isTrailEqualsFlightColor;
	private boolean isShowTargetRing;
	private boolean isShowIntersectPoints;
	
	private Color asymptoteColor;
	private Color cursorColor;
	private Color trailColor;
	private Color targetRingColor;
	private Color intersectPointColor;

	private JTextField tfAsymptoteColor;
	private JTextField tfTrailColor;
	private JTextField tfCursorColor;
	private JTextField tfTargetRingColor;
	private JTextField tfIntersectPointColor;
	
	private JTextField[] tfFlight = new JTextField[14];

    private JLabel[] flightLabel = new JLabel[14];

    private Color[] flightColor = new Color[14];

    private JColorChooser jcc;
	private JDialog jccDialog;
	private JButton jccApply;
	private JButton jccCancel;
	private int colorIndex;

	public StaticTestSettings() {
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
		jccDialog.setTitle("Flight Arc Color Chooser");

		cbShowArcs = new JCheckBox();
		cbShowAsymptotes = new JCheckBox();
		cbShowTrails = new JCheckBox();
		cbTrailEqualsFlightColor = new JCheckBox();
		cbShowCursors = new JCheckBox();
		cbShowTargetRing = new JCheckBox();
		cbShowIntersectPoints = new JCheckBox();
		
		tfMaxCatt = new JTextField();
		tfCursorDiameter = new JTextField();

        tfAsymptoteColor = new JTextField();
        tfAsymptoteColor.setEditable(false);
        tfAsymptoteColor.setBackground(asymptoteColor);
        tfAsymptoteColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfTrailColor = new JTextField();
        tfTrailColor.setEditable(false);
        tfTrailColor.setBackground(trailColor);
        tfTrailColor.setVisible(!isTrailEqualsFlightColor);
        tfTrailColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfCursorColor = new JTextField();
        tfCursorColor.setEditable(false);
        tfCursorColor.setBackground(cursorColor);
        tfCursorColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfTargetRingColor = new JTextField();
        tfTargetRingColor.setEditable(false);
        tfTargetRingColor.setBackground(targetRingColor);
        tfTargetRingColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfIntersectPointColor = new JTextField();
        tfIntersectPointColor.setEditable(false);
        tfIntersectPointColor.setBackground(intersectPointColor);
        tfIntersectPointColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        for (int i = 0; i < tfFlight.length; i++) {
        	flightLabel[i] = new JLabel();
        	flightLabel[i].setText("Flight " + String.valueOf(i+1));
        	tfFlight[i] = new JTextField();
        	tfFlight[i].setBackground(flightColor[i]);
        	tfFlight[i].setEditable(false);
        	tfFlight[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		applyButton = new JButton("Apply");

		setTitle("Static Signal Location Settings");

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(" Static Signal Location Settings ", null, staticSignalLocationSettingsPanel, null);
		
		maxCattLabel = new JLabel();
		maxCattLabel.setText("Maximum Allowed Conic Angle to Target (degrees) ");
		
		cursorDiameterLabel = new JLabel();
		cursorDiameterLabel.setText("Cursor Diameter (pixels) ");

		cbShowArcs.setSelected(isShowArcs);
		cbShowAsymptotes.setSelected(isShowAsymptotes);
		cbShowTrails.setSelected(isShowTrails);
		cbTrailEqualsFlightColor.setSelected(isTrailEqualsFlightColor);
		cbShowCursors.setSelected(isShowCursors);
		cbShowTargetRing.setSelected(isShowTargetRing);
		cbShowIntersectPoints.setSelected(isShowIntersectPoints);
		
		tfMaxCatt.setText(String.valueOf(maxCatt));
		tfCursorDiameter.setText(String.valueOf(cursorDiameter));
		
		staticSignalLocationSettingsPanel = new JPanel();
		staticSignalLocationSettingsPanel.setBorder(BorderFactory.createTitledBorder("Static Signal Location Settings"));
		
		flightColoringPanel = new JPanel();
		flightColoringPanel.setBorder(BorderFactory.createTitledBorder("Flight Arc Colors"));
		
		cbShowArcs.setText("Show Hyperbolic Arcs  ");
		cbShowAsymptotes.setText("Show Asymptotes  ");
		cbShowCursors.setText("Show Cursors  ");
		cbShowTrails.setText("Show Flight Trails  ");
		cbTrailEqualsFlightColor.setText("Trail Color = Flight Color  ");
		cbShowTargetRing.setText("Show Target Ring  ");
		cbShowIntersectPoints.setText("Show Intersect Points  ");

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

		tfFlight[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight0MouseClicked(event);
            }
        });
		
		tfFlight[1].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight1MouseClicked(event);
            }
        });
		
		tfFlight[2].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight2MouseClicked(event);
            }
        });
		
		tfFlight[3].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight3MouseClicked(event);
            }
        });
		
		tfFlight[4].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight4MouseClicked(event);
            }
        });
		
		tfFlight[5].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight5MouseClicked(event);
            }
        });
		
		tfFlight[6].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight6MouseClicked(event);
            }
        });
		
		tfFlight[7].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight7MouseClicked(event);
            }
        });
		
		tfFlight[8].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight8MouseClicked(event);
            }
        });
		
		tfFlight[9].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight9MouseClicked(event);
            }
        });
		
		tfFlight[10].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight10MouseClicked(event);
            }
        });
		
		tfFlight[11].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight11MouseClicked(event);
            }
        });
		
		tfFlight[12].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight12MouseClicked(event);
            }
        });
		
		tfFlight[13].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfFlight13MouseClicked(event);
            }
        });
		
		tfAsymptoteColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfAsymptoteColorMouseClicked(event);
            }
        });
		
		tfCursorColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfCursorColorMouseClicked(event);
            }
        });
		
		tfIntersectPointColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfIntersectPointColorMouseClicked(event);
            }
        });
		
		tfTargetRingColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfTargetRingColorMouseClicked(event);
            }
        });
		
		tfTrailColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfTrailColorMouseClicked(event);
            }
        });
		
		cbShowArcs.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowArcsCheckBoxItemStateChanged(event);
			}
		});
		
		cbShowAsymptotes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowAsymptotesCheckBoxItemStateChanged(event);
			}
		});
		
		cbShowCursors.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowCursorsCheckBoxItemStateChanged(event);
			}
		});
		
		cbShowTrails.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowTrailsCheckBoxItemStateChanged(event);
			}
		});
		
		cbTrailEqualsFlightColor.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbTrailEqualsFlightColorCheckBoxItemStateChanged(event);
			}
		});
		
		cbShowTargetRing.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowTargetRingCheckBoxItemStateChanged(event);
			}
		});
		
		cbShowIntersectPoints.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowIntersectPointsCheckBoxItemStateChanged(event);
			}
		});
		
		jccCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jccDialog.setVisible(false);
			}
		});
		
		jccApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Color selectedColor = jcc.getColor();
				jccDialog.setVisible(false);
				if (colorIndex >= 0 && colorIndex <= 13) {
					flightColor[colorIndex] = selectedColor;
					tfFlight[colorIndex].setBackground(selectedColor);
				}
				if (colorIndex == 14) {
					asymptoteColor = selectedColor;
					tfAsymptoteColor.setBackground(selectedColor);
				}
				if (colorIndex == 15) {
					trailColor = selectedColor;
					tfTrailColor.setBackground(selectedColor);
				}
				if (colorIndex == 16) {
					cursorColor = selectedColor;
					tfCursorColor.setBackground(selectedColor);
				}
				if (colorIndex == 17) {
					targetRingColor = selectedColor;
					tfTargetRingColor.setBackground(selectedColor);
				}
				if (colorIndex == 18) {
					intersectPointColor = selectedColor;
					tfIntersectPointColor.setBackground(selectedColor);
				}
			}
		
		});
	}

	protected void cbShowIntersectPointsCheckBoxItemStateChanged(ItemEvent event) {
		isShowIntersectPoints = cbShowIntersectPoints.isSelected();
	}

	protected void cbShowTargetRingCheckBoxItemStateChanged(ItemEvent event) {
		isShowTargetRing = cbShowTargetRing.isSelected();
	}

	protected void cbShowTrailsCheckBoxItemStateChanged(ItemEvent event) {
		isShowTrails = cbShowTrails.isSelected();
	}
	
	protected void cbTrailEqualsFlightColorCheckBoxItemStateChanged(ItemEvent event) {
		isTrailEqualsFlightColor = cbTrailEqualsFlightColor.isSelected();
		tfTrailColor.setVisible(!isTrailEqualsFlightColor);
		repaint();
	}
	
	protected void cbShowCursorsCheckBoxItemStateChanged(ItemEvent event) {
		isShowCursors = cbShowCursors.isSelected();
	}

	protected void cbShowAsymptotesCheckBoxItemStateChanged(ItemEvent event) {
		isShowAsymptotes = cbShowAsymptotes.isSelected();
	}

	protected void cbShowArcsCheckBoxItemStateChanged(ItemEvent event) {
		isShowArcs = cbShowArcs.isSelected();
	}

	protected void tfTrailColorMouseClicked(MouseEvent event) {
		colorIndex = 15;
		jccDialog.setVisible(true);
	}

	protected void tfTargetRingColorMouseClicked(MouseEvent event) {
		colorIndex = 17;
		jccDialog.setVisible(true);
	}

	protected void tfIntersectPointColorMouseClicked(MouseEvent event) {
		colorIndex = 18;
		jccDialog.setVisible(true);
	}

	protected void tfCursorColorMouseClicked(MouseEvent event) {
		colorIndex = 16;
		jccDialog.setVisible(true);
	}

	protected void tfAsymptoteColorMouseClicked(MouseEvent event) {
		colorIndex = 14;
		jccDialog.setVisible(true);
	}

	private void tfFlight0MouseClicked(MouseEvent event) {
		colorIndex = 0;
		jccDialog.setVisible(true);
	}

	private void tfFlight1MouseClicked(MouseEvent event) {
		colorIndex = 1;
		jccDialog.setVisible(true);
	}

	private void tfFlight2MouseClicked(MouseEvent event) {
		colorIndex = 2;
		jccDialog.setVisible(true);
	}

	private void tfFlight3MouseClicked(MouseEvent event) {
		colorIndex = 3;
		jccDialog.setVisible(true);
	}

	private void tfFlight4MouseClicked(MouseEvent event) {
		colorIndex = 4;
		jccDialog.setVisible(true);
	}

	private void tfFlight5MouseClicked(MouseEvent event) {
		colorIndex = 5;
		jccDialog.setVisible(true);
	}

	private void tfFlight6MouseClicked(MouseEvent event) {
		colorIndex = 6;
		jccDialog.setVisible(true);
	}

	private void tfFlight7MouseClicked(MouseEvent event) {
		colorIndex = 7;
		jccDialog.setVisible(true);
	}

	private void tfFlight8MouseClicked(MouseEvent event) {
		colorIndex = 8;
		jccDialog.setVisible(true);
	}

	private void tfFlight9MouseClicked(MouseEvent event) {
		colorIndex = 9;
		jccDialog.setVisible(true);
	}

	private void tfFlight10MouseClicked(MouseEvent event) {
		colorIndex = 10;
		jccDialog.setVisible(true);
	}

	private void tfFlight11MouseClicked(MouseEvent event) {
		colorIndex = 11;
		jccDialog.setVisible(true);
	}

	private void tfFlight12MouseClicked(MouseEvent event) {
		colorIndex = 12;
		jccDialog.setVisible(true);
	}

	private void tfFlight13MouseClicked(MouseEvent event) {
		colorIndex = 13;
		jccDialog.setVisible(true);
	}
	
	private void drawGraphicalUserInterface() {
	   org.jdesktop.layout.GroupLayout flightColoringPanelLayout = new org.jdesktop.layout.GroupLayout(flightColoringPanel);
        flightColoringPanel.setLayout(flightColoringPanelLayout);
        flightColoringPanelLayout.setHorizontalGroup(
            flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(flightColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[0], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[0], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[1], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[1], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[2], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[2], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[3], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[3], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[4], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[4], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[5], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[5], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[6], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[6], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[7], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[7], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[8], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[8], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[9], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[9], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[10], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[10], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[11], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[11], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[12], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[12], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(flightColoringPanelLayout.createSequentialGroup()
                        .add(flightLabel[13], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFlight[13], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        
        flightColoringPanelLayout.setVerticalGroup(
            flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, flightColoringPanelLayout.createSequentialGroup()
                .add(0, 11, Short.MAX_VALUE)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[0])
                	.add(tfFlight[0], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[1])
                	.add(tfFlight[1], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[2])
                    .add(tfFlight[2], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[3])
                    .add(tfFlight[3], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[4])
                    .add(tfFlight[4], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[5])
                    .add(tfFlight[5], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[6])
                    .add(tfFlight[6], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[7])
                    .add(tfFlight[7], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[8])
                    .add(tfFlight[8], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[9])
                    .add(tfFlight[9], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[10])
                    .add(tfFlight[10], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[11])
                    .add(tfFlight[11], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[12])
                    .add(tfFlight[12], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(flightColoringPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flightLabel[13])
                    .add(tfFlight[13], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    
        org.jdesktop.layout.GroupLayout staticSignalLocationSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(staticSignalLocationSettingsPanel);
        staticSignalLocationSettingsPanel.setLayout(staticSignalLocationSettingsPanelLayout);
        staticSignalLocationSettingsPanelLayout.setHorizontalGroup(
            staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbShowArcs)
                            .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                    .add(cbShowTrails)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(tfTrailColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                    .add(cbShowCursors)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(tfCursorColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                    .add(cbShowAsymptotes)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(tfAsymptoteColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                    .add(cbShowTargetRing)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(tfTargetRingColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                    .add(cbShowIntersectPoints)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(tfIntersectPointColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(cbTrailEqualsFlightColor))))
                    .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(cursorDiameterLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(maxCattLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(tfMaxCatt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(tfCursorDiameter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 186, Short.MAX_VALUE)
                .add(flightColoringPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        
        staticSignalLocationSettingsPanelLayout.setVerticalGroup(
            staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .add(16, 16, 16)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxCattLabel)
                    .add(tfMaxCatt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cursorDiameterLabel)
                    .add(tfCursorDiameter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(35, 35, 35)
                .add(cbShowArcs)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbShowAsymptotes)
                    .add(tfAsymptoteColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbShowCursors)
                    .add(tfCursorColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbShowTrails)
                    .add(tfTrailColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbTrailEqualsFlightColor)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbShowTargetRing)
                    .add(tfTargetRingColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(staticSignalLocationSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfIntersectPointColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cbShowIntersectPoints))
                .add(20, 20, 20))
            .add(flightColoringPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(applyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(staticSignalLocationSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(staticSignalLocationSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(applyButton)
                    .add(okButton))
                .addContainerGap())
        );

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
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
	}

	private void getSettingsFromRegistry() {
		userPref = Preferences.userRoot();
		
		maxCatt = userPref.getDouble("MaxCatt", 60.0);
		cursorDiameter = userPref.getInt("CursorDiameter", 6);
		
		isShowArcs = userPref.getBoolean("ShowArcs", false);
		isShowAsymptotes = userPref.getBoolean("ShowAsymptotes", false);
		isShowCursors = userPref.getBoolean("ShowCursors", false);
		isShowTrails = userPref.getBoolean("ShowTrails", false);
		isTrailEqualsFlightColor = userPref.getBoolean("TrailEqualsFlightColor", false);
		isShowTargetRing = userPref.getBoolean("ShowTargetRing", false);
		isShowIntersectPoints = userPref.getBoolean("ShowIntersectPoints", false);
		
		asymptoteColor = new Color(userPref.getInt("AsymptoteColor", Color.CYAN.getRGB()));
		cursorColor = new Color(userPref.getInt("CursorColor", Color.BLACK.getRGB()));
		trailColor = new Color(userPref.getInt("TrailColor", Color.BLUE.getRGB()));
		targetRingColor = new Color(userPref.getInt("TargetRingColor", Color.RED.getRGB()));
		intersectPointColor = new Color(userPref.getInt("IntersectPointColor", Color.GREEN.getRGB()));
		
		for (int i = 0; i < flightColor.length; i++) {
			flightColor[i] = new Color(userPref.getInt("FlightColor" + String.valueOf(i), Color.RED.getRGB()));
		}
	}

	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}

	private void applyButtonActionListenerEvent(ActionEvent event) {
		userPref.putBoolean("ShowArcs", isShowArcs);
		userPref.putBoolean("ShowCursors", isShowCursors);
		userPref.putBoolean("ShowAsymptotes", isShowAsymptotes);
		userPref.putBoolean("ShowTrails", isShowTrails);
		userPref.putBoolean("TrailEqualsFlightColor", isTrailEqualsFlightColor);
		userPref.putBoolean("ShowTargetRing", isShowTargetRing);
		userPref.putBoolean("ShowIntersectPoints", isShowIntersectPoints);
		userPref.putDouble("MaxCatt", maxCatt);
		userPref.putInt("CursorDiameter", cursorDiameter);
		userPref.putInt("AsymptoteColor", asymptoteColor.getRGB());
		userPref.putInt("TrailColor", trailColor.getRGB());
		userPref.putInt("CursorColor", cursorColor.getRGB());
		userPref.putInt("TargetRingColor", targetRingColor.getRGB());
		userPref.putInt("IntersectPointColor", intersectPointColor.getRGB());

		for (int i = 0; i < flightColor.length; i++) {
			userPref.putInt("FlightColor" + String.valueOf(i), flightColor[i].getRGB()); 
		}

		firePropertyChange("PROPERTY_CHANGE", null, null);
	}

	public Color[] getArcColors() {
		return flightColor;
	}
	
	public Color getArcColor(int flight) {
		return flightColor[flight];
	}
	
	public Color getAsymptoteColor() {
		return asymptoteColor;
	}
	
	public Color getCursorColor() {
		return cursorColor;
	}
	
	public Color getTrailColor() {
		return trailColor;
	}
	
	public Color getIntersectPointColor() {
		return intersectPointColor;
	}
	
	public Color getTargetRingColor() {
		return targetRingColor;
	}
	
	public int getCursorDiameter() {
		return cursorDiameter;
	}
	
	public double getMaxCatt() {
		return maxCatt;
	}
	
	public boolean isShowArcs() {
		return isShowArcs;
	}
	
	public boolean isShowAsymptotes() {
		return isShowAsymptotes;
	}
	
	public boolean isShowTrails() {
		return isShowTrails;
	}
	
	public boolean isTrailEqualsFlightColor() {
		return isTrailEqualsFlightColor; 
	}
	
	public boolean isShowCursors() {
		return isShowCursors;
	}
	
	public boolean isShowTargetRing() {
		return isShowTargetRing;
	}
	
	public boolean isShowIntersectPoints() {
		return isShowIntersectPoints;
	}

}
