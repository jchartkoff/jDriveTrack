package com;

import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart extends JPanel {
	private static final long serialVersionUID = -7133162013801082716L;
	
	private XYSeries series; 
	private XYSeriesCollection seriesData;
	private JFreeChart chart; 
	private XYSplineRenderer renderer;
	private XYPlot plot;
	private ChartPanel chartPanel;

	public Chart(int[][] data, String seriesLabel, String title, String xAxisLabel, String yAxisLabel) {
		setOpaque(true);
		setDoubleBuffered(true);
		setVisible(true);
		
		series = new XYSeries(seriesLabel);
		
		seriesData = new XYSeriesCollection(series);

		chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, seriesData, 
				PlotOrientation.VERTICAL, false, false, false);
		
	    plot = (XYPlot) chart.getPlot();
	    
	    renderer = new XYSplineRenderer();
	    
	    chartPanel = new ChartPanel(chart);
	    
	    renderer.setSeriesLinesVisible(0, true);
	    renderer.setSeriesShapesVisible(0, true);
	    renderer.setSeriesShape(0, new Ellipse2D.Double(-1, -1, 2, 2));
	    
	    plot.setRenderer(renderer);

	    chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
	    
	    chart.setAntiAlias(true);
	    
	    for (int i = 0; i < data.length; i++) {
			series.add(data[i][0], data[i][1]);
		}
	    
	    add(chartPanel);
 
	}
}
