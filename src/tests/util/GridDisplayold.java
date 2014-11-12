package tests.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import view.util.ColorGenerator;

import common.IGrid;

public class GridDisplayold extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5907053878068878363L;
	
	private IGrid curGrid;  // the simulator to provide simulation data
    private ColorGenerator visualizer;
    private int cellWidth;
    private int cellHeight;

    /**
     * Paints one cell of the grid.
     *
     * @param aGraphics Graphics into which painting will be done
     * @param row       row number of the grid cell
     * @param col       column number of the grid cell
     * @param t         intensity of Color red to be painted; a number from 0.0 to 1.0
     */
    private void paintSpot(Graphics aGraphics, int row, int col, float t) {
    	
        int rowPos = row * cellHeight;
        int colPos = col * cellWidth;

        // Overwrite everything that was there previously
        //TODO: since we know we're going to update all spots every time, it may be more
        //      efficient to just do this once up front and only update here.
        //      We could make a separate method to *just* update cell and call that
        //      if we want to maintain this kind of update ability.
        //TODO: is black being set just for transparency visualizers?
        aGraphics.setColor(Color.black);
        aGraphics.fillRect(colPos, rowPos, cellWidth, cellHeight);

        // Color in RGB format with green and blue values = 0.0
        aGraphics.setColor(visualizer.calculateColor(t));
        aGraphics.fillRect(colPos, rowPos, cellWidth, cellHeight);
    }

    /**
     * Informs Swing how to render in terms of subcomponents.
     *
     * @param aGraphics Graphics - Graphs context for drawing
     * @override paintComponent in JPanel
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics aGraphics) {
        super.paintComponent(aGraphics);

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        if (null != curGrid) {
        	
            Graphics anotherGraphics = bi.createGraphics();

            int panelH = getHeight();
            int panelW = getWidth();
            
            // calculate scale factor in case we need to sub-sample the grid output
            float xScale = (float) curGrid.getGridHeight() / (float) panelH;
            float yScale = (float) curGrid.getGridWidth() / (float) panelW;
            
            //TODO: below finds value of each pixel in display grid.  this
            //      is good when grid to be displayed is larger than space 
            //      allows and we sub-sample, but if underlying grid is more
            //      coarse, this creates lots of duplication.
            //      Consider an if/else for different approaches depending
            //      on if we're sub-sampled or not.
            
            cellHeight = 1;								//GRID_SIZE / numCols;
            cellWidth = 1;								//GRID_SIZE / numRows;
            
            for (int i = 0; i < panelH; i++) {
                for (int j = 0; j < panelW; j++) {
                    paintSpot(anotherGraphics, i, j, curGrid.getTemperature((int) Math.floor(i * xScale), (int) Math.floor(j * yScale)));
                }
            }
        }
        aGraphics.drawImage(bi, 0, 0, this);
    }

    public void updateGrid(IGrid grid) {
        this.curGrid = grid;
        repaint();
    }

    /**
     * Method to set the correct visualizer to this visualization pane
     * based on user input
     *
     * @param visualizer
     */
    public void setVisualizer(ColorGenerator visualizer) {
        this.visualizer = visualizer;
    }
}
