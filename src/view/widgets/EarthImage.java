package view.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class EarthImage extends JPanel {
	
	private static final String EARTH_IMAGE = "resource/earth.jpg";

	/**
	 * 
	 */
	private static final long serialVersionUID = 6319433780080785942L;
	
	private Image earth;
	
	private int height = 0, width = 0;
	
	public EarthImage() {
		
		earth = new ImageIcon(EARTH_IMAGE).getImage();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double ratio = 1.0 * earth.getWidth(null) / earth.getHeight(null);
		this.width = screenSize.width;
		this.height = (int) (this.width / ratio);
		
		earth = earth.getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);

		Dimension size = new Dimension(this.width, this.height);
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);
	    setLayout(null);
	}
	
	public int getImageWidth() {
		return this.width;
	}
	
	public int getImageHeight() {
		return this.height;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (earth != null) {
	        g.drawImage(earth, 0, 0, this.width, this.height, this);
	    }
	}
}
