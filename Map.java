public class Map {
	private int[] pixels;
	private int dim;

	public int getDim() {
		return dim;
	}

	public int[] getPixels() {
		return pixels;
	}

	public void setPixel(int x, int y, int to) {
		if (x >= dim || y >= dim || x < 0 || y < 0)
			return;
		pixels[y*dim+x] = to;
	}

	public int getPixel(int x, int y) {
		if (x >= dim || y >= dim || x < 0 || y < 0)
			return -1;
		return pixels[y*dim+x];
	}
	
	public Map() {
		pixels = null;
		dim = 0;
	}

	// NOTE: Copies _pixels
	public Map(int[] _pixels, int _dim) {
		dim = _dim;
		
		pixels = new int[dim];
		for (int i = 0; i < dim*dim; i++) {
			pixels[i] = _pixels[i];
		}
	}

	public Map(int _dim) {
		dim = _dim;
		pixels = new int[dim];
	}
	
	// TODO: Implement: save, load

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

}