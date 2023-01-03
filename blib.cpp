// Artiom Matevosyans's Brain Library
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include "u.hpp"

namespace blib {

	typedef unsigned char byte;
	
	// Maps are 1:1 ratio and are 1 channel, so essentially they ARE a channel.
	// Maybe in later versions they will be renamed to channels and actual maps
	// will be implemented.
	class Map {
		public:
		byte* pixels;
		uint32_t dim;

		Map() {
			pixels = nullptr;
			dim = 0;
		}

		// NOTE: Copies _pixels
		Map(byte* _pixels, int _dim) {
			dim = _dim;
			pixels = (byte*)malloc(dim*dim);
			for (int i = 0; i < dim*dim; i++) {
				pixels[i] = _pixels[i];
			}
		}

		Map(int _dim) {
			dim = _dim;
			pixels = (byte*)malloc(dim*dim);
		}
		
		Map(char* fp) {
			load(fp);
		}

		~Map() {
			free(pixels);
		}

		// NOTE: Replaces if there is anything already in the map.
		bool load(char* fp) {
			if (pixels)
				free(pixels);
			
			FILE* f = fopen(fp, "rb");
			if (!f)
				return false;

			fread(&dim, 4, 1, f);
			pixels = (byte*)malloc(dim*dim);
			fread(pixels, dim*dim, 1, f);

			fclose(f);
			return true;
		}

		// Overrides file if it exists
		bool save(char* fp) {
			FILE* f = fopen(fp, "wb");
			if (!f)
				return false;

			fwrite(&dim, 4, 1, f);
			fwrite(pixels, dim*dim, 1, f);

			fclose(f);
			return true;
		}

		// (0,0) = top left
		byte getPixel(int x, int y) {
			return pixels[y*dim + x];
		}
		
		void setPixel(int x, int y, byte p) {
			if (x >= dim || y >= dim || x < 0 || y < 0)
				return;
			pixels[y*dim + x] = p;
		}

		// This is essentially part of the convolution
		// We place this map on another and do the thingy thing that gets us the
		// sum of each pixel multiplication
		byte placeOn(Map& on, int _x, int _y) {
			byte ret = 0;
			for (int x = _x; x < _x+dim; x++) {
				for (int y = _y; y < _y+dim; y++) {
					byte a = getPixel(x-_x, y-_y);
					byte b = on.getPixel(x, y);
					ret += a*b;
				}
			}
			return ret;
		}

		Map poolAvg(byte dimPool, byte stride) {
			if (dim % stride != 0 || dimPool > dim)
				return Map(0);
			
			Map ret(dim-stride);
			
			for (int x = 0; x <= dim - dimPool; x+=stride) {	
				for (int y = 0; y <= dim - dimPool; y+=stride) {
					// Getting the average value
					byte value = 0;
					for (int _x = x; _x < x+stride; _x++) {
						for (int _y = y; _y < y+stride; _y++) {
							byte p = getPixel(_x, _y);
							printf("%i ", p);
							value += p;
						}
					}
					printf("\n");
					ret.setPixel(x/stride, y/stride, value/(dimPool*2));
				}
			}

			return ret;
		}
		
		Map poolMax(byte dimPool, byte stride) {
			if (dim % stride != 0 || dimPool > dim)
				return Map(0);
			
			Map ret(dim-stride);
			
			for (int x = 0; x <= dim - dimPool; x+=stride) {	
				for (int y = 0; y <= dim - dimPool; y+=stride) {
					// Getting the average value
					byte value = getPixel(x, y);
					for (int _x = x; _x < x+stride; _x++) {
						for (int _y = y; _y < y+stride; _y++) {
							byte p = getPixel(_x, _y);
							if (value < p)
								value = p;
						}
					}

					ret.setPixel(x/stride, y/stride, value);
				}
			}

			return ret;
		}
		
		// TODO: Maybe optimize shit? you literally change a single operation
		// for this function.
		Map poolMin(byte dimPool, byte stride) {
			if (dim % stride != 0 || dimPool > dim)
				return Map(0);
			
			Map ret(dim-stride);
			
			for (int x = 0; x <= dim - dimPool; x+=stride) {	
				for (int y = 0; y <= dim - dimPool; y+=stride) {
					// Getting the average value
					byte value = getPixel(x, y);
					for (int _x = x; _x < x+stride; _x++) {
						for (int _y = y; _y < y+stride; _y++) {
							byte p = getPixel(_x, _y);
							if (value > p)
								value = p;
						}
					}

					ret.setPixel(x/stride, y/stride, value);
				}
			}

			return ret;
		}
	};

	// TODO: Add padding
	class Kernel {
		public:
		byte stride;
		Map map;

		Kernel(Map _map, byte _stride = 1) {
			map = _map;
			stride = _stride;
		}

		// NOTE: this->map.dim must be smaller than other.dim, ofc.
		// Also, other.dim % this->stride = 0, otherwise kernel overshoots.
		// If you fail to satisfy requirements Map(0) is returned.
		// Returns a feature map.
		Map convolve(Map& other) {
			if (other.dim % stride != 0 || map.dim > other.dim)
				return Map(0);
			
			Map ret(other.dim-stride);
			
			for (int x = 0; x <= other.dim - map.dim; x+=stride) {	
				for (int y = 0; y <= other.dim - map.dim; y+=stride) {
					ret.setPixel(x/stride, y/stride, map.placeOn(other, x, y));
				}
			}

			return ret;
		}
	};

	enum ActivationFunction {
		AFN_NULL = 0, // For linear regression output layer
		AFN_TANH, // TanH, for hidden layers, longer learning
		AFN_RELU, // ReLU, for hidden layers mostly, efficient
		AFN_SIGMOID, // Sigmoid, for binary output layer usually
		AFN_SOFTMAX, // Softmax for multi-class classification OUTPUTS
	};
	
	enum LayerType {
		LAYER_N,		// Neural layer(Multi-Layered P)
		LAYER_C,		// Convolution without any pooling
		LAYER_C_MAX,	// Convolution with max-pool applied
		LAYER_C_AVG,	// Convolution with average-pool applied
		LAYER_C_MIN,	// Convolution with min-pool applied
	};

	class LayerCfg {
		public:
		char type;
		char afn;
		int unitsN;

		LayerCfg(int _unitsN, char _type=0, int _afn=0) {
			unitsN = _unitsN;
			type = _type;
			afn = _afn;
		}
	};

	// Convolutional brain
	class BrainCfg {
		public:
		u::Stack<LayerCfg> s;

		BrainCfg() {
			s = u::Stack<LayerCfg>();
		}

		BrainCfg(LayerCfg* layers, int layersN) {
			s = u::Stack<LayerCfg>(layers, layersN);
		}

		void add(LayerCfg& l) {
			s.push(l);
		}
	};

	class Brain {
		public:
		BrainCfg cfg;

		Brain(BrainCfg& _cfg) {
			cfg = _cfg;
		}
	};

	// Fully connected brain
	class FBrain {

	};

	// MLP

	struct cell {
		// Basic rule: has to be in previous layer
		// nullptr in a cell for none
		struct {
			cell* cell_ptr; // if nullptr, this entire input isn't used
			float weight; // -1 to 1
		}* inputs;
		int inputs_n;

		byte output;
	};

}

int main() {
	blib::byte p[] = {
		0,2,2,0, 
		0,4,2,0, 
		0,2,3,0, 
		0,2,2,0};
	blib::byte p2[] = {
		0,1, 
		1,0};

	blib::Map a(p, 4);
	blib::Map pooled = a.poolMax(2,2);
	printf("\n\n%i %i\n%i %i", pooled.pixels[0], pooled.pixels[1], pooled.pixels[2], pooled.pixels[3]);
/*
	blib::Map b(p2, 2);
	blib::Kernel x(b, 2);

	blib::Map res = x.convolve(a);
*/
	return 0;
}
