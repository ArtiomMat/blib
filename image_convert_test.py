from PIL import Image
wanted_size = 64

i,j = 0,0
#disabled = [0,1,3,5,6,13,15,17,19,42,36,37,39,24,25,48,49,50,55,58,59,57,65,67,56,76,73,71,63,104,85,89,90,79,80,94,112,116,118,96]
disabled=[]
disabled.sort()
total = 124

for i in range(0,total):
	image = Image.open('../data_femboys_raw/'+str(i)+'.jpg')
	#image = image.convert('L')

	w,h=image.width,image.height
	
	disable_adder = False
	if j < len(disabled):
		if disabled[j] == i:
			disable_adder = True
			j+=1

	if w > h:
		w = int(w*(wanted_size/h))
		adder = 0
		if not disable_adder:
			adder = (w-wanted_size)/2

		image = image.resize((w, wanted_size))\
				.crop((
				0+adder, 0,
				wanted_size+adder, wanted_size))
	else:
		h = int(h*(wanted_size/w))
		adder = 0
		if not disable_adder:
			adder = (h-wanted_size)/2

		image = image.resize((wanted_size, h))\
				.crop((
				0, 0+adder,
				wanted_size, wanted_size+adder))



	image.save('../data_men_bad2/'+str(i)+'.png')