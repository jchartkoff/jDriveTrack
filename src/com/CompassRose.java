package com;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Color;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;

public class CompassRose extends JPanel {
	private static final long serialVersionUID = 1890587064525434224L;
	
	private Color selectColor = new Color(255, 0, 0);
	private int heading;
	private NumberFormat headingFormat;
	private JLabel headingLabel = new JLabel();
	private int radius;

	public CompassRose() {
		setOpaque(true);
		setDoubleBuffered(true);
		setVisible(true);
		headingFormat = new DecimalFormat("000");
		headingLabel.setFont(new Font("Tahoma", 1, 14));
		headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headingLabel.setVerticalAlignment(SwingConstants.CENTER);
		setLayout(new BorderLayout());
		add(headingLabel, BorderLayout.CENTER);
	}


	public void setHeading(int heading) {
		this.heading = heading;
		repaint();
	}

	public void setSelectColor(Color selectColor) {
		this.selectColor = selectColor;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
			radius = ((getHeight() / 2) - (3 * (getHeight() / 2) / 10)); 
			headingLabel.setText(headingFormat.format(heading)); 
			
			for (double i = 180.0; i <= 540.0; i = i + 11.25) { 
				Ellipse2D dot = new Ellipse2D.Double(((Math.sin(-i * Math.PI / 180.0) * radius)
						+ (getWidth() / 2.0) - 5.0),
						((Math.cos(i * Math.PI / 180.0) * radius)
						+ (getWidth() / 2.0) - 2.0), 10, 10);
	
				if (Math.abs(heading - (i - 180.0)) <= 5.5) {
					g2.setColor(selectColor);
					g2.draw(dot);
					g2.fill(dot);
				} else {
					g2.setColor(new Color(128, 128, 128));
					g2.draw(dot);
				}
			}
		} finally {
			g2.dispose();
		}
	}
}