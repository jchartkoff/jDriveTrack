package com;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.cache.BasicDataFileStore;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.ArrayList;

public class BulkDownloadPanel extends JDialog {
	private static final long serialVersionUID = 6301789743122511942L;
	
	protected WorldWindow wwd;
    private Sector currentSector;
    private ArrayList<BulkRetrievablePanel> retrievables;
    private JButton selectButton;
    private JLabel sectorLabel;
    private JButton startButton;
    private JPanel monitorPanel;
    private JPanel mainPanel;
    private BasicDataFileStore cache;
    private SectorSelector selector;
    private Dimension screenSize;

    public BulkDownloadPanel(WorldWindow wwd) {
        this.wwd = wwd;

        setSize(new Dimension(500, 300));
        
        // Init retievable list
        retrievables = new ArrayList<BulkRetrievablePanel>();
        // Layers
        for (Layer layer : wwd.getModel().getLayers()) {
            if (layer instanceof BulkRetrievable)
                retrievables.add(new BulkRetrievablePanel((BulkRetrievable) layer));
        }
        // Elevation models
        CompoundElevationModel cem = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();
        for (ElevationModel elevationModel : cem.getElevationModels()) {
            if (elevationModel instanceof BulkRetrievable)
                retrievables.add(new BulkRetrievablePanel((BulkRetrievable) elevationModel));
        }

        // Init sector selector
        selector = new SectorSelector(wwd);
        selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
        selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
        selector.setBorderWidth(3);
        selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                updateSector();
            }
        });
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    	setTitle("WorldWind Bulk Downloader");
    	Toolkit tk = Toolkit.getDefaultToolkit();
		screenSize = tk.getScreenSize();
        initComponents();
    }
    
    private void updateSector() {
        currentSector = selector.getSector();
        if (currentSector != null) {
            // Update sector description
            sectorLabel.setText(makeSectorDescription(currentSector));
            selectButton.setText("Clear sector");
            startButton.setEnabled(true);
        } else {
            // null sector
            sectorLabel.setText("-");
            selectButton.setText("Select sector");
            startButton.setEnabled(false);
        }
        updateRetrievablePanels(currentSector);
    }

    private void updateRetrievablePanels(Sector sector) {
        for (BulkRetrievablePanel panel : retrievables) {
            panel.updateDescription(sector);
        }
    }

    private void selectButtonActionPerformed(ActionEvent event) {
        if (selector.getSector() != null) {
            selector.disable();
        } else {
            selector.enable();
        }
        updateSector();
    }

    public void clearSector() {
        if (selector.getSector() != null) {
            selector.disable();
        }
        updateSector();
    }

    private void startButtonActionPerformed(ActionEvent event) {
        for (BulkRetrievablePanel panel : retrievables) {
            if (panel.selectCheckBox.isSelected()) {
                BulkRetrievable retrievable = panel.retrievable;
                BulkRetrievalThread thread = retrievable.makeLocal(currentSector, 0, cache,
                    new BulkRetrievalListener() {
                        @Override
						public void eventOccurred(BulkRetrievalEvent event) {
                            // This is how you'd include a retrieval listener. Uncomment below to monitor downloads.
                            // Be aware that the method is not invoked on the event dispatch thread, so any interaction
                            // with AWT or Swing must be within a SwingUtilities.invokeLater() runnable.

                            //System.out.printf("%s: item %s\n",
                            //    event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_SUCCEEDED) ? "Succeeded"
                            //: event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_FAILED) ? "Failed"
                            //    : "Unknown event type", event.getItem());
                        }
                    });
                if (thread != null) monitorPanel.add(new DownloadMonitorPanel(thread));
            }
        }
    }

    public boolean hasActiveDownloads() {
        for (Component c : monitorPanel.getComponents()) {
            if (c instanceof DownloadMonitorPanel)
                if (((DownloadMonitorPanel) c).thread.isAlive())
                    return true;
        }
        return false;
    }

    public void cancelActiveDownloads() {
        for (Component c : monitorPanel.getComponents()) {
            if (c instanceof DownloadMonitorPanel) {
                if (((DownloadMonitorPanel) c).thread.isAlive()) {
                    DownloadMonitorPanel panel = (DownloadMonitorPanel) c;
                    panel.cancelButtonActionPerformed(null);
                    try {
                        // Wait for thread to die before moving on
                        long t0 = System.currentTimeMillis();
                        while (panel.thread.isAlive() && System.currentTimeMillis() - t0 < 500) {
                            Thread.sleep(10);
                        }
                    } catch (Exception ignore) { }
                }
            }
        }
    }

    public void clearInactiveDownloads() {
        for (int i = monitorPanel.getComponentCount() - 1; i >= 0; i--) {
            Component c = monitorPanel.getComponents()[i];
            if (c instanceof DownloadMonitorPanel) {
                DownloadMonitorPanel panel = (DownloadMonitorPanel) c;
                if (!panel.thread.isAlive() || panel.thread.isInterrupted()) {
                    monitorPanel.remove(i);
                }
            }
        }
        monitorPanel.validate();
    }

    private void initComponents() {
    	mainPanel = new JPanel();
        int border = 6;
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Download")));
        mainPanel.setToolTipText("Layer imagery bulk download.");

        final JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
        JLabel locationLabel = new JLabel(" Cache:");
        final JLabel locationName = new JLabel("");
        JButton locationButton = new JButton("...");
        locationPanel.add(locationLabel, BorderLayout.WEST);
        locationPanel.add(locationName, BorderLayout.CENTER);
        locationPanel.add(locationButton, BorderLayout.EAST);
        mainPanel.add(locationPanel, BorderLayout.CENTER);

        locationButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                int status = fc.showOpenDialog(locationPanel);
                if (status == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file != null) {
                        locationName.setText(file.getPath());
                        cache = new BasicDataFileStore(file);
                        updateRetrievablePanels(selector.getSector());
                    }
                }
            }
        });

        // Select sector button
        JPanel sectorPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        sectorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        selectButton = new JButton("Select sector");
        selectButton.setToolTipText("Press Select then press and drag button 1 on globe");
        selectButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
                selectButtonActionPerformed(event);
            }
        });
        sectorPanel.add(selectButton);
        sectorLabel = new JLabel("-");
        sectorLabel.setPreferredSize(new Dimension(350, 16));
        sectorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sectorPanel.add(sectorLabel);
        mainPanel.add(sectorPanel);

        // Retrievable list combo and start button
        JPanel retrievablesPanel = new JPanel();
        retrievablesPanel.setLayout(new BoxLayout(retrievablesPanel, BoxLayout.Y_AXIS));
        retrievablesPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

        // RetrievablePanel list
        for (JPanel panel : retrievables) {
            retrievablesPanel.add(panel);
        }
        mainPanel.add(retrievablesPanel);

        // Start button
        JPanel startPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        startPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        startButton = new JButton("Start download");
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
                startButtonActionPerformed(event);
            }
        });
        startPanel.add(startButton);
        mainPanel.add(startPanel);

        // Download monitor panel
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new BoxLayout(monitorPanel, BoxLayout.Y_AXIS));
        monitorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        //add(monitorPanel);

        // Put the monitor panel in a scroll pane.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(monitorPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setPreferredSize(new Dimension(350, 200));
        mainPanel.add(scrollPane);
        
        add(mainPanel);
        
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    }

    public static String makeSectorDescription(Sector sector) {
        return String.format("S %7.4f\u00B0 W %7.4f\u00B0 N %7.4f\u00B0 E %7.4f\u00B0",
            sector.getMinLatitude().degrees,
            sector.getMinLongitude().degrees,
            sector.getMaxLatitude().degrees,
            sector.getMaxLongitude().degrees);
    }

    public static String makeSizeDescription(long size) {
        double sizeInMegaBytes = size / 1024 / 1024;
        if (sizeInMegaBytes < 1024) return String.format("%,.1f MB", sizeInMegaBytes);
        else if (sizeInMegaBytes < 1024 * 1024) return String.format("%,.1f GB", sizeInMegaBytes / 1024);
        return String.format("%,.1f TB", sizeInMegaBytes / 1024 / 1024);
    }

    public class BulkRetrievablePanel extends JPanel {
		private static final long serialVersionUID = -5993140560893359468L;
		
		private BulkRetrievable retrievable;
        private JCheckBox selectCheckBox;
        private JLabel descriptionLabel;
        private Thread updateThread;
        private Sector sector;

        BulkRetrievablePanel(BulkRetrievable retrievable) {
            this.retrievable = retrievable;
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            // Check + name
            selectCheckBox = new JCheckBox(retrievable.getName());
            selectCheckBox.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e) {
                    if (((JCheckBox) e.getSource()).isSelected() && sector != null) updateDescription(sector);
                }
            });
            add(selectCheckBox, BorderLayout.WEST);
            // Description (size...)
            descriptionLabel = new JLabel();
            add(descriptionLabel, BorderLayout.EAST);
        }

        public void updateDescription(final Sector sector) {
            if (updateThread != null && updateThread.isAlive()) return;

            this.sector = sector;
            if (!selectCheckBox.isSelected()) {
                doUpdateDescription(null);
                return;
            }

            updateThread = new Thread(new Runnable() {
                @Override
				public void run() {
                    doUpdateDescription(sector);
                }
            });
            updateThread.setDaemon(true);
            updateThread.start();
        }

        private void doUpdateDescription(final Sector sector) {
            if (sector != null) {
                try {
                    long size = retrievable.getEstimatedMissingDataSize(sector, 0, cache);
                    final String formattedSize = BulkDownloadPanel.makeSizeDescription(size);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
						public void run() {
                            descriptionLabel.setText(formattedSize);
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
						public void run() {
                            descriptionLabel.setText("-");
                        }
                    });
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                	@Override
                    public void run() {
                        descriptionLabel.setText("-");
                    }
                });
            }
        }

        @Override
		public String toString() {
            return retrievable.getName();
        }
    }

    public class DownloadMonitorPanel extends JPanel {
		private static final long serialVersionUID = -2083099223805187576L;
		
		private BulkRetrievalThread thread;
        private Progress progress;
        private Timer updateTimer;

        private JLabel descriptionLabel;
        private JProgressBar progressBar;
        private JButton cancelButton;

        public DownloadMonitorPanel(BulkRetrievalThread thread) {
            this.thread = thread;
            progress = thread.getProgress();
            initComponents();
            updateTimer = new Timer(1000, new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent event) {
                    updateStatus();
                }
            });
            updateTimer.start();
        }

        private void updateStatus() {
            // Update description
            String text = thread.getRetrievable().getName();
            text = text.length() > 30 ? text.substring(0, 27) + "..." : text;
            text += " (" + BulkDownloadPanel.makeSizeDescription(progress.getCurrentSize())
                + " / " + BulkDownloadPanel.makeSizeDescription(progress.getTotalSize())
                + ")";
            descriptionLabel.setText(text);
            // Update progress bar
            int percent = 0;
            if (progress.getTotalCount() > 0)
                percent = (int) ((float) progress.getCurrentCount() / progress.getTotalCount() * 100f);
            progressBar.setValue(Math.min(percent, 100));
            // Update tooltip
            String tooltip = BulkDownloadPanel.makeSectorDescription(thread.getSector());
            descriptionLabel.setToolTipText(tooltip);
            progressBar.setToolTipText(makeProgressDescription());

            // Check for end of thread
            if (!thread.isAlive()) {
                // Thread is done
                cancelButton.setText("Remove");
                cancelButton.setBackground(Color.GREEN);
                updateTimer.stop();
            }
        }

        private void cancelButtonActionPerformed(ActionEvent event) {
            if (thread.isAlive()) {
                // Cancel thread
                thread.interrupt();
                cancelButton.setBackground(Color.ORANGE);
                cancelButton.setText("Remove");
                updateTimer.stop();
            } else {
                // Remove from monitor panel
                Container top = getTopLevelAncestor();
                getParent().remove(this);
                top.validate();
            }
        }

        private void initComponents() {
            int border = 2;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // Description label
            JPanel descriptionPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            String text = thread.getRetrievable().getName();
            text = text.length() > 40 ? text.substring(0, 37) + "..." : text;
            descriptionLabel = new JLabel(text);
            descriptionPanel.add(descriptionLabel);
            add(descriptionPanel);

            // Progrees and cancel button
            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(100, 16));
            progressPanel.add(progressBar);
            progressPanel.add(Box.createHorizontalStrut(8));
            cancelButton = new JButton("Cancel");
            cancelButton.setBackground(Color.RED);
            cancelButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent event) {
                    cancelButtonActionPerformed(event);
                }
            });
            progressPanel.add(cancelButton);
            add(progressPanel);
        }

        private String makeProgressDescription() {
            String text = "";
            if (progress.getTotalCount() > 0) {
                int percent = (int) ((double) progress.getCurrentCount() / progress.getTotalCount() * 100d);
                text = percent + "% of ";
                text += makeSizeDescription(progress.getTotalSize());
            }
            return text;
        }
    }
}


