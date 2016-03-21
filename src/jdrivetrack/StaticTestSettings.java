package jdrivetrack;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.NumberFormatter;

public class StaticTestSettings extends JDialog {
	private static final long serialVersionUID = 4676114275202217003L;
	
	protected static final String APPLY_SETTINGS = "APPLY_SETTINGS";
	
	private JPanel staticSignalLocationSettingsPanel;
    private JPanel flightColoringPanel;
	private JTabbedPane tabbedPane;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JLabel maxCattLabel;
	private JLabel cursorDiameterLabel;
	private JLabel traceDiameterLabel;
	private JLabel intersectPointDiameterLabel;
	
	private double maxCatt;
	private double cursorDiameter;
	private double traceDiameter;
	private double intersectPointDiameter;
	
	private JFormattedTextField tfMaxCatt;
    private JFormattedTextField tfCursorDiameter;
    private JFormattedTextField tfTraceDiameter;
    private JFormattedTextField tfIntersectPointDiameter;
    
	private JCheckBox cbShowArcs;
	private JCheckBox cbShowAsymptotes;
	private JCheckBox cbShowCursors;
	private JCheckBox cbShowTraces;
	private JCheckBox cbTraceEqualsFlightColor;
	private JCheckBox cbShowTargetRing;
	private JCheckBox cbShowIntersectPoints;
	
	private boolean isShowArcs;
	private boolean isShowAsymptotes;
	private boolean isShowCursors;
	private boolean isShowTraces;
	private boolean isTraceEqualsFlightColor;
	private boolean isShowTargetRing;
	private boolean isShowIntersectPoints;
	
	private Color asymptoteColor;
	private Color cursorColor;
	private Color traceColor;
	private Color targetRingColor;
	private Color intersectPointColor;

	private JTextField tfAsymptoteColor;
	private JTextField tfTraceColor;
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
	
	private Preferences userPref = Preferences.userRoot().node("jdrivetrack/prefs/StaticTestSettings");
	
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
		
		cbShowArcs = new JCheckBox();
		cbShowAsymptotes = new JCheckBox();
		cbShowTraces = new JCheckBox();
		cbTraceEqualsFlightColor = new JCheckBox();
		cbShowCursors = new JCheckBox();
		cbShowTargetRing = new JCheckBox();
		cbShowIntersectPoints = new JCheckBox();

        tfAsymptoteColor = new JTextField();
        tfAsymptoteColor.setEditable(false);
        tfAsymptoteColor.setBackground(asymptoteColor);
        tfAsymptoteColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfTraceColor = new JTextField();
        tfTraceColor.setEditable(false);
        
        if (isTraceEqualsFlightColor) {
        	tfTraceColor.setBackground(Color.LIGHT_GRAY);
        } else {
        	tfTraceColor.setBackground(traceColor);
        }
        
        tfTraceColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
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
        okButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);
		
		setTitle("Static Signal Location Analysis");

		tabbedPane = new JTabbedPane();		
		
		maxCattLabel = new JLabel();
		maxCattLabel.setText("Maximum Allowed Conic Angle to Target (degrees) ");
		
		cursorDiameterLabel = new JLabel("Cursor Diameter (pixels) ");
		cursorDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		traceDiameterLabel = new JLabel("Trace Diameter (pixels) ");
		traceDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		intersectPointDiameterLabel = new JLabel("Intersect Point Diameter (pixels) ");
		intersectPointDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		cbShowArcs.setSelected(isShowArcs);
		cbShowAsymptotes.setSelected(isShowAsymptotes);
		cbShowTraces.setSelected(isShowTraces);
		cbTraceEqualsFlightColor.setSelected(isTraceEqualsFlightColor);
		cbShowCursors.setSelected(isShowCursors);
		cbShowTargetRing.setSelected(isShowTargetRing);
		cbShowIntersectPoints.setSelected(isShowIntersectPoints);
		
		staticSignalLocationSettingsPanel = new JPanel();
		staticSignalLocationSettingsPanel.setBorder(BorderFactory.createTitledBorder("Static Signal Location Settings"));
		
		flightColoringPanel = new JPanel();
		flightColoringPanel.setBorder(BorderFactory.createTitledBorder("Flight Arc Colors"));
		
		cbShowArcs.setText("Show Hyperbolic Arcs  ");
		cbShowAsymptotes.setText("Show Asymptotes  ");
		cbShowCursors.setText("Show Cursors  ");
		cbShowTraces.setText("Show Flight Traces  ");
		cbTraceEqualsFlightColor.setText("Trace Color = Flight Color  ");
		cbShowTargetRing.setText("Show Target Ring  ");
		cbShowIntersectPoints.setText("Show Intersect Points  ");
		
		tabbedPane.addTab(" Conic Signal Location Settings ", null, staticSignalLocationSettingsPanel, null);
		
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
		
		tfTraceColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfTraceColorMouseClicked(event);
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
		
		cbShowTraces.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbShowTraceCheckBoxItemStateChanged(event);
			}
		});
		
		cbTraceEqualsFlightColor.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				cbTraceEqualsFlightColorCheckBoxItemStateChanged(event);
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

		NumberFormat integerFormat = NumberFormat.getIntegerInstance();
		NumberFormatter numberFormatter = new NumberFormatter(integerFormat);
		DecimalFormat twoDigitIntegerFormat = new DecimalFormat("#0");
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(true);
		numberFormatter.setMinimum(2);
		numberFormatter.setMaximum(99);
		numberFormatter.setCommitsOnValidEdit(false);

		tfIntersectPointDiameter = new JFormattedTextField(numberFormatter);
		tfIntersectPointDiameter.setText(twoDigitIntegerFormat.format(intersectPointDiameter));
		tfIntersectPointDiameter.setHorizontalAlignment(SwingConstants.CENTER);
		tfIntersectPointDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfIntersectPointDiameter.setBackground(Color.WHITE);
		tfIntersectPointDiameter.setForeground(Color.BLACK);
		
		tfIntersectPointDiameter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfIntersectPointDiameter.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfIntersectPointDiameter.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfIntersectPointDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfIntersectPointDiameter.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});

		tfMaxCatt = new JFormattedTextField(numberFormatter);
		tfMaxCatt.setText(twoDigitIntegerFormat.format(maxCatt));
		tfMaxCatt.setHorizontalAlignment(SwingConstants.CENTER);
		tfMaxCatt.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfMaxCatt.setBackground(Color.WHITE);
		tfMaxCatt.setForeground(Color.BLACK);
		
		tfMaxCatt.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfMaxCatt.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfMaxCatt.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfMaxCatt.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfMaxCatt.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});

		tfCursorDiameter = new JFormattedTextField(numberFormatter);
		tfCursorDiameter.setText(twoDigitIntegerFormat.format(cursorDiameter));
		tfCursorDiameter.setHorizontalAlignment(SwingConstants.CENTER);
		tfCursorDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfCursorDiameter.setBackground(Color.WHITE);
		tfCursorDiameter.setForeground(Color.BLACK);
		
		tfCursorDiameter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfCursorDiameter.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfCursorDiameter.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfCursorDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfCursorDiameter.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});
		
		tfTraceDiameter = new JFormattedTextField(numberFormatter);
		tfTraceDiameter.setText(twoDigitIntegerFormat.format(traceDiameter));
		tfTraceDiameter.setHorizontalAlignment(SwingConstants.CENTER);
		tfTraceDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfTraceDiameter.setBackground(Color.WHITE);
		tfTraceDiameter.setForeground(Color.BLACK);
		
		tfTraceDiameter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfTraceDiameter.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfTraceDiameter.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfTraceDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfTraceDiameter.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});
		
		String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 5784154864021285638L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
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
					traceColor = selectedColor;
					tfTraceColor.setBackground(selectedColor);
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
		isShowIntersectPoints = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}

	protected void cbShowTargetRingCheckBoxItemStateChanged(ItemEvent event) {
		isShowTargetRing = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}

	protected void cbShowTraceCheckBoxItemStateChanged(ItemEvent event) {
		isShowTraces = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}
	
	protected void cbTraceEqualsFlightColorCheckBoxItemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			isTraceEqualsFlightColor = true;
			tfTraceColor.setEnabled(false);
			tfTraceColor.setBackground(Color.LIGHT_GRAY);
		} else {
			isTraceEqualsFlightColor = false;
			tfTraceColor.setEnabled(true);
			tfTraceColor.setBackground(traceColor);
		}
	}
	
	protected void cbShowCursorsCheckBoxItemStateChanged(ItemEvent event) {
		isShowCursors = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}

	protected void cbShowAsymptotesCheckBoxItemStateChanged(ItemEvent event) {
		isShowAsymptotes = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}

	protected void cbShowArcsCheckBoxItemStateChanged(ItemEvent event) {
		isShowArcs = (event.getStateChange() == ItemEvent.SELECTED) ? true : false;
	}

	protected void tfTraceColorMouseClicked(MouseEvent event) {
		colorIndex = 15;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Trace Color");
	}

	protected void tfTargetRingColorMouseClicked(MouseEvent event) {
		colorIndex = 17;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Target Ring Color");
	}

	protected void tfIntersectPointColorMouseClicked(MouseEvent event) {
		colorIndex = 18;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Intersect Point Color");
	}

	protected void tfCursorColorMouseClicked(MouseEvent event) {
		colorIndex = 16;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Cursor Color");
	}

	protected void tfAsymptoteColorMouseClicked(MouseEvent event) {
		colorIndex = 14;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Asymptote Color");
	}

	private void tfFlight0MouseClicked(MouseEvent event) {
		colorIndex = 0;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 1 Color");
	}

	private void tfFlight1MouseClicked(MouseEvent event) {
		colorIndex = 1;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 2 Color");
	}

	private void tfFlight2MouseClicked(MouseEvent event) {
		colorIndex = 2;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 3 Color");
	}

	private void tfFlight3MouseClicked(MouseEvent event) {
		colorIndex = 3;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 4 Color");
	}

	private void tfFlight4MouseClicked(MouseEvent event) {
		colorIndex = 4;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 5 Color");
	}

	private void tfFlight5MouseClicked(MouseEvent event) {
		colorIndex = 5;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 6 Color");
	}

	private void tfFlight6MouseClicked(MouseEvent event) {
		colorIndex = 6;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 7 Color");
	}

	private void tfFlight7MouseClicked(MouseEvent event) {
		colorIndex = 7;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 8 Color");
	}

	private void tfFlight8MouseClicked(MouseEvent event) {
		colorIndex = 8;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 9 Color");
	}

	private void tfFlight9MouseClicked(MouseEvent event) {
		colorIndex = 9;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 10 Color");
	}

	private void tfFlight10MouseClicked(MouseEvent event) {
		colorIndex = 10;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 11 Color");
	}

	private void tfFlight11MouseClicked(MouseEvent event) {
		colorIndex = 11;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 12 Color");
	}

	private void tfFlight12MouseClicked(MouseEvent event) {
		colorIndex = 12;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 13 Color");
	}

	private void tfFlight13MouseClicked(MouseEvent event) {
		colorIndex = 13;
		jccDialog.setVisible(true);
		jccDialog.setTitle("Flight 14 Color");
	}
	
	private void drawGraphicalUserInterface() {
		GroupLayout flightColoringPanelLayout = new GroupLayout(flightColoringPanel);
        flightColoringPanel.setLayout(flightColoringPanelLayout);
        flightColoringPanelLayout.setAutoCreateGaps(true);
        flightColoringPanelLayout.setAutoCreateContainerGaps(true);
        
        flightColoringPanelLayout.setHorizontalGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(flightColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[0],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[0],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[1],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[1],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[2],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[2],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[3],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[3],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[4],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[4],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[5],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[5],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[6],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[6],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[7],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[7],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[8],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[8],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[9],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[9],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[10],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[10],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[11],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[11],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[12],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[12],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[13],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[13],70,70,70)))
                .addContainerGap()));
        
        flightColoringPanelLayout.setVerticalGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, flightColoringPanelLayout.createSequentialGroup()
                .addGap(10,10,10)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[0])
                	.addComponent(tfFlight[0],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[1])
                	.addComponent(tfFlight[1],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[2])
                    .addComponent(tfFlight[2],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[3])
                    .addComponent(tfFlight[3],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[4])
                    .addComponent(tfFlight[4],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[5])
                    .addComponent(tfFlight[5],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[6])
                    .addComponent(tfFlight[6],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[7])
                    .addComponent(tfFlight[7],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[8])
                    .addComponent(tfFlight[8],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[9])
                    .addComponent(tfFlight[9],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[10])
                    .addComponent(tfFlight[10],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[11])
                    .addComponent(tfFlight[11],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[12])
                    .addComponent(tfFlight[12],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[13])
                    .addComponent(tfFlight[13],15,15,15))
                .addContainerGap(10, Short.MAX_VALUE)));
        
        GroupLayout staticSignalLocationSettingsPanelLayout = new GroupLayout(staticSignalLocationSettingsPanel);
        staticSignalLocationSettingsPanel.setLayout(staticSignalLocationSettingsPanelLayout);
        staticSignalLocationSettingsPanelLayout.setAutoCreateGaps(true);
        staticSignalLocationSettingsPanelLayout.setAutoCreateContainerGaps(true);

        staticSignalLocationSettingsPanelLayout.setHorizontalGroup(
        		staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbShowArcs)
                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .addComponent(maxCattLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfMaxCatt, 30,30,30))
                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(cbShowIntersectPoints)
                                    .addComponent(cbShowCursors)
                                    .addComponent(cbShowAsymptotes)
                                    .addComponent(cbShowTargetRing)
                                    .addComponent(cbShowTraces))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(tfIntersectPointColor, 70,70,70)
                                                    .addComponent(tfTraceColor, 70,70,70)
                                                    .addComponent(tfAsymptoteColor, 70,70,70))
                                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(10, 10, 10)
                                                        .addComponent(cbTraceEqualsFlightColor))
                                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(intersectPointDiameterLabel, 200,200,200)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(tfIntersectPointDiameter, 30,30,30))))
                                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                .addComponent(tfCursorColor, 70,70,70)
                                                .addGap(18, 18, 18)
                                                .addComponent(cursorDiameterLabel, 200,200,200)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tfCursorDiameter, 30,30,30)))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE))
                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(tfTargetRingColor, 70,70,70)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addComponent(flightColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap()));
        
        staticSignalLocationSettingsPanelLayout.setVerticalGroup(
            staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .addComponent(cbShowArcs)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowCursors)
                            .addComponent(tfCursorColor, 15,15,15)
                            .addComponent(cursorDiameterLabel)
                            .addComponent(tfCursorDiameter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowIntersectPoints)
                            .addComponent(tfIntersectPointColor, 15,15,15)
                            .addComponent(intersectPointDiameterLabel)
                            .addComponent(tfIntersectPointDiameter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowAsymptotes)
                            .addComponent(tfAsymptoteColor, 15,15,15))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowTargetRing)
                            .addComponent(tfTargetRingColor, 15,15,15))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowTraces)
                            .addComponent(tfTraceColor, 15,15,15)
                            .addComponent(cbTraceEqualsFlightColor))
                        .addGap(40, 40, 40)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(maxCattLabel)
                            .addComponent(tfMaxCatt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(flightColoringPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGroup(layout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
		                .addGroup(layout.createSequentialGroup()
			                .addComponent(okButton,90,90,90)
			                .addComponent(applyButton,90,90,90)
			                .addComponent(cancelButton,90,90,90)))
		        	.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	.addGroup(GroupLayout.Alignment.TRAILING,layout.createSequentialGroup()
        		.addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                .addComponent(okButton)
	                .addComponent(applyButton)
	                .addComponent(cancelButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        
        GroupLayout jccDialogLayout = new GroupLayout(jccDialog.getContentPane());
		jccDialog.getContentPane().setLayout(jccDialogLayout);
		jccDialogLayout.setHorizontalGroup(
	            jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(GroupLayout.Alignment.TRAILING, jccDialogLayout.createSequentialGroup()
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jccApply, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(jccCancel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addGap(134, 134, 134))
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
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
		maxCatt = userPref.getDouble("MaxCatt", 60.0);
		cursorDiameter = userPref.getDouble("CursorDiameter", 3);
		traceDiameter = userPref.getDouble("TraceDiameter", 3);
		intersectPointDiameter = userPref.getDouble("IntersectPointDiameter", 3);
		isShowArcs = userPref.getBoolean("ShowArcs", false);
		isShowAsymptotes = userPref.getBoolean("ShowAsymptotes", false);
		isShowCursors = userPref.getBoolean("ShowCursors", false);
		isShowTraces = userPref.getBoolean("ShowTraces", false);
		isTraceEqualsFlightColor = userPref.getBoolean("TraceEqualsFlightColor", false);
		isShowTargetRing = userPref.getBoolean("ShowTargetRing", false);
		isShowIntersectPoints = userPref.getBoolean("ShowIntersectPoints", false);
		
		asymptoteColor = new Color(userPref.getInt("AsymptoteColor", Color.CYAN.getRGB()));
		cursorColor = new Color(userPref.getInt("CursorColor", Color.BLACK.getRGB()));
		traceColor = new Color(userPref.getInt("TraceColor", Color.BLUE.getRGB()));
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
		
		cursorDiameter = Double.parseDouble(tfCursorDiameter.getText());
		traceDiameter = Double.parseDouble(tfTraceDiameter.getText());
		intersectPointDiameter = Double.parseDouble(tfIntersectPointDiameter.getText());		
		userPref.putBoolean("ShowArcs", isShowArcs);
		userPref.putBoolean("ShowCursors", isShowCursors);
		userPref.putBoolean("ShowAsymptotes", isShowAsymptotes);
		userPref.putBoolean("ShowTraces", isShowTraces);
		userPref.putBoolean("TraceEqualsFlightColor", isTraceEqualsFlightColor);
		userPref.putBoolean("ShowTargetRing", isShowTargetRing);
		userPref.putBoolean("ShowIntersectPoints", isShowIntersectPoints);
		userPref.putDouble("MaxCatt", maxCatt);
		userPref.putDouble("CursorDiameter", cursorDiameter);
		userPref.putDouble("TraceDiameter", traceDiameter);
		userPref.putDouble("IntersectPointDiameter", intersectPointDiameter);
		userPref.putInt("AsymptoteColor", asymptoteColor.getRGB());
		userPref.putInt("TraceColor", traceColor.getRGB());
		userPref.putInt("CursorColor", cursorColor.getRGB());
		userPref.putInt("TargetRingColor", targetRingColor.getRGB());
		userPref.putInt("IntersectPointColor", intersectPointColor.getRGB());

		for (int i = 0; i < flightColor.length; i++) {
			userPref.putInt("FlightColor" + String.valueOf(i), flightColor[i].getRGB()); 
		}

		firePropertyChange(APPLY_SETTINGS, null, null);
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
	
	public Color getTraceColor() {
		return traceColor;
	}
	
	public Color getIntersectPointColor() {
		return intersectPointColor;
	}
	
	public Color getTargetRingColor() {
		return targetRingColor;
	}
	
	public double getCursorRadius() {
		return cursorDiameter;
	}
	
	public double getTraceRadius() {
		return traceDiameter;
	}
	
	public double getIntersectPointRadius() {
		return intersectPointDiameter;
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
	
	public boolean isShowTrace() {
		return isShowTraces;
	}
	
	public boolean isTraceEqualsFlightColor() {
		return isTraceEqualsFlightColor; 
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
