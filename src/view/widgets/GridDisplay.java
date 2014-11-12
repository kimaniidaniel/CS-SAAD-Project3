package view.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import simulation.Earth;
import view.util.ColorGenerator;
import common.IGrid;

public class GridDisplay extends JPanel {

	/**
		 * 
		 */
	private static final long serialVersionUID = 5907053878068878363L;

	private IGrid grid;
	
	private final ColorGenerator visualizer;
	
	public GridDisplay(ColorGenerator visualizer, int width, int height) {
		this.visualizer = visualizer;
		
		this.setBackground(new Color(0,0,0,0));
		this.setOpaque(false);
		
		Dimension size = new Dimension(width, height);
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);
	    setLayout(null);
	}

	/**
	 * Informs Swing how to render in terms of subcomponents.
	 *
	 * @param g
	 *            Graphics - Graphs context for drawing
	 * @override paintComponent in JPanel
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		if (grid != null) {
			int w = this.getSize().width;
			int h = this.getSize().height;
			
			float cellHeight = (float)h / grid.getGridHeight();
			float cellWidth = (float)w / grid.getGridWidth();

			for (int y = 0; y < grid.getGridHeight(); y++) {
				for (int x = 0; x < grid.getGridWidth(); x++) {
					
					float t = grid.getTemperature(x, y);
					
					int celly = Math.round(y * cellHeight);
					int cellx = Math.round(x * cellWidth);
					int nextCelly = Math.round((y+1) * cellHeight);
					int nextCellx = Math.round((x+1) * cellWidth);
					int cellw = nextCellx-cellx;
					int cellh = nextCelly-celly;

					// "fill" the rectangle with the temp color
					g.setColor(visualizer.calculateColor(t));
					g.fillRect(cellx, celly, cellw, cellh);
				}
			}
			// Draw grid lines
			g.setColor(Color.GRAY);
			for (int y = 1; y < grid.getGridHeight()-1; y++) {
				int celly = Math.round(y * cellHeight);
				g.drawLine(0, celly, w, celly);
			}
			for (int x = 0; x < grid.getGridWidth(); x++) {
				int cellx = Math.round(x * cellWidth);
				g.drawLine(cellx, 0, cellx, h);
			}
			// Draw sun position
			g.setColor(Color.YELLOW);
			float pixPerDeg = w/360.0f;
			float degFromLeft = grid.getSunPositionDeg()+180f;
			int sunx = Math.round(degFromLeft * pixPerDeg);
			//g.drawLine(sunx, 0, sunx, h);
			
			//Draw sun position latitude
			float degFromTop = grid.getSunLatitudeDeg();
			if(degFromTop > 0)
				degFromTop = 90f - degFromTop;
			else
				degFromTop = 90f + Math.abs(degFromTop);
			pixPerDeg = h/180.0f;
			int suny = Math.round(degFromTop * pixPerDeg);
			g.drawOval(sunx, suny, 10, 10);
			
			//Draw planet orbit around earth
			//g.drawOval(0,0, (int)(300*Earth.a/Earth.b), 300);
			//Draw planet position on ellipse
			//g.drawOval((int)(300*Earth.a/grid.getPlanetX()),0, 10,10);
			//g.drawOval(0,(int)(300*Earth.a/grid.getPlanetY()), 10,10);
			//g.drawOval((int)(w*grid.getPlanetX()/360) - w/2,(int)(h*grid.getPlanetY()/180) - h/2 , 10,10);
			
			/*pixPerDeg = (float) (h/(2*Earth.a));
			int x = (int) grid.getPlanetX();
			if(x > 0)
				x = (int) (Earth.a + x);
			else
				x = (int) (Earth.a - x);
			
			int y = (int) grid.getPlanetY();
			if(y > 0)
				y = (int) (Earth.b - y);
			else
				y = (int) (Earth.b + Math.abs(y));
			
			x = Math.round(x * pixPerDeg);
			y = Math.round(y * pixPerDeg);
			g.drawOval(x, y, 10,10);*/
			//System.out.println(grid.getPlanetX() + ", " + grid.getPlanetY());
			

		}
	}

	public void update(IGrid grid) {
		this.grid = grid;
		repaint();
	}
}
