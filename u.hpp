// Artiom's Utility Library

#include <stdlib.h>

namespace u {

	template <typename T>
	class Stack {
		public:
		T* e;
		int numRaw, num;
		
		Stack() {
			numRaw = 2;
			num = 0;
			e = (T*)malloc(2*sizeof(T));
		}
		
		// [-1] for last
		T& operator[](int i) {
			if (i<0)
				return e[num-1];
			return e[i];
		}

		// NOTE: buf is copied
		Stack(T* _e, int _num) {
			numRaw = _num+_num%2;
			num = _num;
			
			e = (T*)malloc(numRaw*sizeof(T));
			for (int i = 0; i < num; i++) {
				e[i] = _e[i];
			}
		}

		~Stack() {
			free(e);
		}

		void push(T& what) {
			e[num++] = what;
			if (num >= numRaw) {
				numRaw *= 2;
				e = (T*)realloc(e, numRaw*sizeof(T));
			}
		}
		
		bool pop(T& to) {
			if (num<=0)
				return false;

			to = e[num--];
			if (num <= numRaw/2) {
				numRaw /= 2;
				e = (T*)realloc(e, numRaw*sizeof(T));
			}
			return true;
		}

		bool pop() {
			if (num<=0)
				return false;

			num--;
			if (num <= numRaw/2) {
				numRaw /= 2;
				e = (T*)realloc(e, numRaw*sizeof(T));
			}
			return true;
		}

		// Peek mf at the top
		bool peek(T& to) {
			if (num<=0)
				return false;
			to = e[num-1];
			return true;
		}

		bool peek() {
			if (num<=0)
				return false;
			return true;
		}
	};
};
