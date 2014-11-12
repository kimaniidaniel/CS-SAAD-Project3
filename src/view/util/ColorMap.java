/*
 * Class for describing color maps
 */

//NOTE: originally taken from gtmat codebase (which copies matlab colormaps)
//      but heavily modified.

package view.util;

import java.awt.Color;

public class ColorMap {

	String name;
	Color clrs[];

	public ColorMap(String nm, Color c[]) {
		name = nm;
		clrs = c;
	}

	public static ColorMap getMap(String nm) throws IllegalArgumentException {

		ColorMap res = null; // maps[0];

		for (int i = 0; i < maps.length; i++) {
			if (nm.equalsIgnoreCase(maps[i].name)) {
				res = maps[i];
				break;
			}
		}

		if (res == null)
			throw new IllegalArgumentException("unknown colormap requested");

		return res;
	}

	public Color getColor(double here, float opacity) {

		Color res = clrs[0];

		if (here > 0) {

			double dn = 1.00001 / (clrs.length - 1);

			int n = (int) (here / dn);

			double dh = (here - n * dn) * (clrs.length - 1);

			int r0 = clrs[n].getRed();
			int g0 = clrs[n].getGreen();
			int b0 = clrs[n].getBlue();

			if (n > (clrs.length - 2))
				n = clrs.length - 2;

			int r1 = clrs[n + 1].getRed();
			int g1 = clrs[n + 1].getGreen();
			int b1 = clrs[n + 1].getBlue();

			float r = (float) (r0 + dh * (r1 - r0));

			r = check(r);

			float g = (float) (g0 + dh * (g1 - g0));

			g = check(g);

			float b = (float) (b0 + dh * (b1 - b0));

			b = check(b);

			res = new Color(scale(r), scale(g), scale(b), opacity);
		}

		return res;
	}

	private float check(float c) {

		if (c < 0)
			return 0;

		if (c > 255)
			return 255;

		return c;
	}

	public String toString() {

		String res = "Colormap " + name + " with " + clrs.length
				+ " colors: { ";

		for (int i = 0; i < clrs.length; i++) {
			res += clrs[i] + " ";
		}

		return res + "}";
	}

	/*
	 * Colorcube contains as many regularly spaced colors in RGB color space as
	 * possible, while attempting to provide more steps of gray, pure red, pure
	 * green, and pure blue. flag consists of the colors red, white, blue, and
	 * black. This colormap completely changes color with each index increment.
	 * lines produces a colormap of colors specified by the axes ColorOrder
	 * property and a shade of gray. pink contains pastel shades of pink. The
	 * pink colormap provides sepia tone colorization of grayscale photographs.
	 * Prism repeats the six colors red, orange, yellow, green, blue, and
	 * violet. White is an all white monochrome colormap. jet ranges from blue
	 * to red, and passes through the colors cyan, yellow, and orange. It is a
	 * variation of the hsv colormap. The jet colormap is associated with an
	 * astrophysical fluid jet simulation from the National Center for
	 * Supercomputer Applications.
	 */
	public static final Color jet[] = { Color.blue, Color.cyan, Color.yellow,
			new Color(255, 128, 0), Color.red };

	/*
	 * HSV varies the hue component of the hue-saturation-value color model. The
	 * colors begin with red, pass through yellow, green, cyan, blue, magenta,
	 * and return to red. The colormap is particularly appropriate for
	 * displaying periodic functions. HSV(m) is the same as hsv2rgb([h
	 * ones(m,2)]) where h is the linear ramp, h = (0:m���1)'/m.
	 */
	public static final Color HSV[] = { Color.red, Color.yellow, Color.green,
			Color.cyan, Color.blue, Color.magenta, Color.red };

	/*
	 * Hot varies smoothly from black through shades of red, orange, and yellow,
	 * to white.
	 */
	public static final Color hot[] = { Color.black, Color.red,
			new Color(255, 128, 0), Color.yellow, Color.white };

	/*
	 * Cool consists of colors that are shades of cyan and magenta. It varies
	 * smoothly from cyan to magenta.
	 */
	public static final Color cool[] = { Color.cyan, Color.magenta };

	/*
	 * Spring consists of colors that are shades of magenta and yellow.
	 */
	public static final Color spring[] = { Color.magenta, Color.yellow };

	/*
	 * Summer consists of colors that are shades of green and yellow.
	 */
	public static final Color summer[] = { Color.green, Color.yellow };

	/*
	 * Autumn varies smoothly from red, through orange, to yellow.
	 */
	public static final Color autumn[] = { Color.red, new Color(255, 128, 0),
			Color.yellow };

	public static final Color thermal[] = { Color.WHITE,
			new Color(251, 246, 255), new Color(243, 224, 255),
			new Color(214, 153, 255), new Color(153, 0, 255),
			new Color(102, 0, 204), new Color(84, 51, 117),
			new Color(51, 51, 153), new Color(0, 0, 102), Color.BLUE,
			Color.CYAN, Color.GREEN, Color.yellow, new Color(255, 102, 0),
			new Color(255, 128, 0), new Color(255, 51, 0),
			new Color(153, 0, 0), new Color(200, 0, 0), Color.RED,
			new Color(255, 0, 0), new Color(255, 80, 80) };

	/*
	 * Winter consists of colors that are shades of blue and green.
	 */
	public static final Color winter[] = { Color.blue, Color.green };

	/*
	 * Gray returns a linear grayscale colormap.
	 */
	public static final Color gray[] = { Color.black, Color.white };

	/*
	 * Bone is a grayscale colormap with a higher value for the blue component.
	 * This colormap is useful for adding an "electronic" look to grayscale
	 * images.
	 */
	public static final Color bone[] = { new Color(0, 0, 50), Color.white };

	/*
	 * Copper varies smoothly from black to bright copper.
	 */
	public static final Color copper[] = { Color.black, new Color(220, 120, 80) };

	/*
	 * Pink contains pastel shades of pink. The pink colormap provides sepia
	 * tone colorization of grayscale photographs. Prism repeats the six colors
	 * red, orange, yellow, green, blue, and violet. White is an all white
	 * monochrome colormap.
	 */
	public static final Color pink[] = { new Color(255, 128, 128), Color.white };

	/*
	 * Prism repeats the six colors red, orange, yellow, green, blue, and
	 * violet. White is an all white monochrome colormap.
	 */
	public static final Color prism[] = { Color.red, new Color(255, 128, 0),
			Color.yellow, Color.green, Color.cyan, Color.blue,
			new Color(64, 0, 75) };

	/*
	 * White is an all white monochrome colormap.
	 */
	public static final Color white[] = { Color.white, Color.white };

	public static ColorMap maps[] = { new ColorMap("jet", jet),
			new ColorMap("HSV", HSV), new ColorMap("hot", hot),
			new ColorMap("cool", cool), new ColorMap("spring", spring),
			new ColorMap("summer", summer), new ColorMap("autumn", autumn),
			new ColorMap("winter", winter), new ColorMap("gray", gray),
			new ColorMap("bone", bone), new ColorMap("copper", copper),
			new ColorMap("pink", pink), new ColorMap("prism", prism),
			new ColorMap("white", white), new ColorMap("thermal", thermal) };

	private float scale(float temp) {
		return (((1 - 0) * (temp - 0)) / (255 - 0)) + 0;
	}
}