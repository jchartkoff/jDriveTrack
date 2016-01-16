package jdrivetrack;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.StatisticsPanel;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;

public class StatisticsPanelDialog extends JDialog {
	private static final long serialVersionUID = -7235353417787069491L;
	
	public StatisticsPanelDialog(WorldWindow wwd) {
		setLayout(new BorderLayout());
		setAlwaysOnTop(true);
		setTitle("WorldWind Statistics");
		StatisticsPanel statisticsPanel = new StatisticsPanel(wwd, new Dimension(275,400));
		statisticsPanel.setEnabled(true);
		statisticsPanel.setVisible(true);
		statisticsPanel.update();
		add(statisticsPanel);
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		WWUtil.alignComponent(null, this, AVKey.RIGHT_OF_CENTER);
		setVisible(true);
	}

}
