import javax.imageio.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class Map {

	public static final int L8 = 1;
	// In 5-6-5 bit format! Not 5-5-5, leaving a useless bit is bad for AI!
	public static final int RGB16 = 2;
	public static final int RGB24 = 3;
	public static final int ARGB32 = 4;

	private byte[] pixels;
	private int pixelFormat;
	private int width, height;

	public int getPixelFormat() {
		return pixelFormat;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public byte[] getPixels() {
		return pixels;
	}

	public Map(
		byte[] pixels, 
		int width, 
		int height, 
		int pixelFormat
	) {
		this.width = width;
		this.height = height;
		this.pixelFormat = pixelFormat;

		this.pixels = new byte[width*height*pixelFormat];
		if (pixels.length == this.pixels.length) {
			for (int i = 0; i < this.pixels.length; i++) {
				this.pixels[i] = pixels[i];
			}
		}
	}

	// Assumes pixelType=L8
	public Map(byte[] pixels, int width, int height) {
		this(pixels, width, height, L8);
	}

	public Map(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Map(String fp) throws IOException {
		load(fp);
	}

	public void setPixel(int x, int y, byte[] pixel) {
		for (int i = 0; i < pixelFormat; i++)
			pixels[(y*width+x)*pixelFormat+i] = pixel[i];
	}

	public void getPixel(int x, int y, byte[] pixel) {
		for (int i = 0; i < pixelFormat; i++)
			pixel[i] = pixels[(y*width+x)*pixelFormat+i];
	}

	public byte[] getPixel(int x, int y) {
		byte[] pixel = new byte[pixelFormat];
		getPixel(x, y, pixel);
		return pixel;
	}

	private void init(Raster r) {
		width = r.getWidth();
		height = r.getHeight();
		
		pixels = new byte[width*height*pixelFormat];
	}

	// TODO: Implement: save, load

	// Make sure the image is 1:1 ratio, otherwise it won't be accepted
	public void load(String fp) throws IOException {
		BufferedImage bf = ImageIO.read(new File(fp));

		Raster r = bf.getData();


		// Determining pixelFormat
		{
			ColorModel cm = bf.getColorModel();

			boolean a = cm.hasAlpha();
			int depth = cm.getPixelSize();
			int colors = cm.getNumColorComponents();
			
			boolean supported = true;
			if (!a && depth == 8 && colors == 1) {
				pixelFormat = L8;

				System.out.println(cm.getColorSpace());

				init(r);
				// We use bf.getRGB in grayscale because r.getPixel
				// seems to return contrasted values, no idea why.
				int rgb;
				byte[] pByte = new byte[pixelFormat];
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < width; y++) {
						rgb = bf.getRGB(x, y);
						pByte[0] = (byte)rgb;
						setPixel(x, y, pByte);
					}
				}
			}
			else if (!a && depth == 16 && colors == 3) {
				// We need to make sure we are in 5-6-5 order
				if (cm.getComponentSize()[1] != 6)
					supported = false;
				else {
					pixelFormat = RGB16;
					// TODO
					supported = false;
				}
			}
			else if (!a && depth == 24 && colors == 3) {
				pixelFormat = RGB24;
				System.out.println("LOL");
				init(r);
				int[] p = new int[pixelFormat];
				byte[] pByte = new byte[pixelFormat];
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < width; y++) {
						r.getPixel(x, y, p);
						pByte[0] = (byte)p[0];
						pByte[1] = (byte)p[1];
						pByte[2] = (byte)p[2];
						if (x == 0 && y == 0)
							System.out.println();

						setPixel(x, y, pByte);
					}
				}
			}
			else if (a && depth == 32 && colors == 3) {
				pixelFormat = ARGB32;
				/* TODO: Make sure the pixels are in ARGB and not RGBA, maybe test it with
				image with alpha and see which index corresponds to the alpha. */
				supported = false;
			}
			else
				supported = false;
			
			if (!supported) {
				throw new IOException(
					"Map.load(): Unsupported pixel format. Check Map's formats at the top of the class definition."
				);
			}
		}
		
		
		/*byte[] rBuf = ((DataBufferByte) r.getDataBuffer()).getData();
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = rBuf[i];
		}*/

		
	}

	// FIXME: Contrast getting stronger as you save? it gets darker for some reason.
	public void save(String fp) throws IOException {
		int type;
		if (pixelFormat == L8)
			type = BufferedImage.TYPE_BYTE_GRAY;
		else if (pixelFormat == RGB16)
			type = BufferedImage.TYPE_USHORT_565_RGB;
		else if (pixelFormat == RGB24)
			type = BufferedImage.TYPE_INT_RGB;
		else
			type = BufferedImage.TYPE_INT_ARGB;
		BufferedImage bi = new BufferedImage(width, height, type);

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				byte[] pixel = getPixel(x, y);
				int[] pixelInt = new int[pixelFormat];
				for (int i = 0; i < pixel.length; i++)
					pixelInt[i] = (int)(pixel[i]) & 0xFF;

				int rgb;
				if (pixelFormat == 4)
					rgb = pixelInt[1]<<16 | pixelInt[2] << 8 | pixelInt[3];
				else if (pixelFormat == 3) {
					rgb = pixelInt[0]<<16 | pixelInt[1] << 8 | pixelInt[2];
					// System.out.println(pixelInt[0]);
				}
				else if (pixelFormat == 2)
					rgb = 0;// TODO
				else
					rgb = pixelInt[0]<<16 | pixelInt[0] << 8 | pixelInt[0];
				
				bi.setRGB(x, y, rgb);
			}
		}
		
		ImageIO.write(bi, "PNG", new File(fp));
	}
/*
	public int placeOn(Map on, int _x, int _y) {
		int ret = 0;
		for (int x = _x; x < _x+dim; x++) {
			for (int y = _y; y < _y+dim; y++) {
				int a = getPixel(x-_x, y-_y);
				int b = on.getPixel(x, y);
				ret += a*b;
			}
		}
		return ret;
	}

	// Null if incorrect inputs
	public Map poolAvg(int dimPool, int stride) {
		if (dim % stride != 0 || dimPool > dim)
			return null;
		
		Map ret = new Map(dim-stride);
		
		for (int x = 0; x <= dim - dimPool; x+=stride) {	
			for (int y = 0; y <= dim - dimPool; y+=stride) {
				// Getting the average value
				int value = getPixel(x, y);
				for (int _x = x; _x < x+stride; _x++) {
					for (int _y = y+1; _y < y+stride; _y++) {
						int p = getPixel(_x, _y);
						value += p;
					}
				}
				ret.setPixel(x/stride, y/stride, value/(dimPool*2));
			}
		}

		return ret;
	}

	private Map poolMinMax(int dimPool, int stride, boolean max) {
		if (dim % stride != 0 || dimPool > dim)
		return null;
	
		Map ret = new Map(dim-stride);
	
		for (int x = 0; x <= dim - dimPool; x+=stride) {	
			for (int y = 0; y <= dim - dimPool; y+=stride) {
				// Getting the average value
				int value = getPixel(x, y);
				for (int _x = x; _x < x+stride; _x++) {
					for (int _y = y+1; _y < y+stride; _y++) {
						int p = getPixel(_x, _y);
						// Use > if max and < if min
						if (max) {
							if (value < p)
								value = p;
						}
						else {
							if (value > p)
								value = p;
						}
					}
				}

				ret.setPixel(x/stride, y/stride, value);
			}
		}

		return ret;
	}

	public Map poolMax(int dimPool, int stride) {
		return poolMinMax(dimPool, stride, true);
	}
	public Map poolMin(int dimPool, int stride) {
		return poolMinMax(dimPool, stride, false);
	}
 */
}