import os


d = './data_femboys_raw'
files = os.listdir(d)

i = 0
for f in files:
	os.rename(d + '/' + f, d + '/_' + f)
	i+=1

i = 0
for f in files:
	os.rename(d + '/_' + f, d + '/' +str(i)+'.jpg')
	i+=1

