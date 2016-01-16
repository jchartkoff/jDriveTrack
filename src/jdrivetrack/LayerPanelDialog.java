package jdrivetrack;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class LayerPanelDialog extends JDialog {
	private static final long serialVersionUID = -7235353417787069491L;

	public LayerPanelDialog(WorldWindow wwd, Dimension size) {
		setLayout(new BorderLayout());
		setAlwaysOnTop(true);
		setTitle("WorldWind Map Layers");
		add(new LayerPanel(wwd, size));
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		WWUtil.alignComponent(null, this, AVKey.RIGHT_OF_CENTER);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
            	if (WindowEvent.WINDOW_CLOSING == event.getID()) {
            		getContentPane().removeAll();
            	}   
            }
        });
	}

	private class LayerPanel extends JPanel {
		private static final long serialVersionUID = -329361104136452061L;
		
		private JPanel layersPanel;
		private JPanel westPanel;
		private JScrollPane scrollPane;
		private Font defaultFont;
		
		private LayerPanel(WorldWindow wwd) {
			super(new BorderLayout());
			makePanel(wwd, new Dimension(200, 400));
		}

		private LayerPanel(WorldWindow wwd, Dimension size) {
			super(new BorderLayout());
			makePanel(wwd, size);
		}

		private void makePanel(WorldWindow wwd, Dimension size) {
			layersPanel = new JPanel(new GridLayout(0, 1, 0, 4));
			layersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			fill(wwd);

			JPanel dummyPanel = new JPanel(new BorderLayout());
			dummyPanel.add(layersPanel, BorderLayout.NORTH);

			scrollPane = new JScrollPane(dummyPanel);
			scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			if (size != null) scrollPane.setPreferredSize(size);

			westPanel = new JPanel(new GridLayout(0, 1, 0, 10));
			westPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder(" Layers ")));
			westPanel.setToolTipText("Layers to Show");
			westPanel.add(scrollPane);
			add(westPanel, BorderLayout.CENTER);
		}

		private void fill(WorldWindow wwd) {
			for (Layer layer : wwd.getModel().getLayers()) {
				if (!layer.getName().contains(".")) {
					LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
					JCheckBox jcb = new JCheckBox(action);
					jcb.setSelected(action.selected);
					layersPanel.add(jcb);
					if (defaultFont == null) defaultFont = jcb.getFont();
				}
			}
		}

		@Override
		public void setToolTipText(String string) {
			scrollPane.setToolTipText(string);
		}

		private class LayerAction extends AbstractAction {
			private static final long serialVersionUID = -6123615121785087215L;
			private WorldWindow wwd;
			private Layer layer;
			private boolean selected;

			private LayerAction(Layer layer, WorldWindow wwd, boolean selected) {
				super(layer.getName());
				this.wwd = wwd;
				this.layer = layer;
				this.selected = selected;
				layer.setEnabled(selected);
			}
			
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if (((JCheckBox) actionEvent.getSource()).isSelected()) {
					layer.setEnabled(true);
				} else {
					layer.setEnabled(false);
				}	
				wwd.redraw();
			}
		}
	}

	
}
