package jdrivetrack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CompassRose extends JPanel {
	private static final long serialVersionUID = 1890587064525434224L;
	
	private Color selectColor = Color.RED;
	private int heading;
	private int d;
	private int n;
	private NumberFormat headingFormat;
	private JLabel headingLabel = new JLabel();

	public CompassRose(int n, int d) {
		super(new BorderLayout());
		
		this.n = n;
		this.d = d;
		headingFormat = new DecimalFormat("000");
		headingLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
		headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headingLabel.setVerticalAlignment(SwingConstants.CENTER);
		headingLabel.setOpaque(false);
		headingLabel.setVisible(true);
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
		Graphics2D g2 = (Graphics2D) g.create();
		
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			if (heading == 360) {
				headingLabel.setText("---");
			} else {
				headingLabel.setText(headingFormat.format(heading)); 
			}
			
			double r = (getWidth() / 2d) - (2 * d);
			double c = getWidth() / 2d;

			for (double i = 0; i < 2 * Math.PI; i += ((2 * Math.PI) / n)) { 
				
				double x = c + (Math.sin(i) * r) - (d / 2);
				double y = c - (Math.cos(i) * r) - (d / 2);
				
				Ellipse2D dot = new Ellipse2D.Double(x, y, d, d);
				
				if (Math.abs(Math.toRadians(heading) - i) <= ((2 * Math.PI) / n) / 2) {
					g2.setColor(selectColor);
					g2.draw(dot);
					g2.fill(dot);
				} else {
					g2.setColor(Color.GRAY);
					g2.draw(dot);
				}
			}
		} finally {
			g2.dispose();
		}
	}
}