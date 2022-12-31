# Import Module
import os
from tkinter import *
from PIL import Image, ImageTk, ImageFilter

wanted_size = 128

use_x = 0
cunt = 25

x = 0
y = 0

real_x = 0
real_y = 0

i = 0

def swap_images():
	global l
	l.destroy()
	l = Label(image=img)
	l.image = img
	l.pack()

def motion(event):
	x, y = event.x, event.y
	global real_x
	global real_y
	real_x = x
	real_y = y
	if x > wanted_size-cunt:
		x = wanted_size-cunt
	elif x < cunt:
		x = cunt
	
	if y > wanted_size-cunt:
		y = wanted_size-cunt
	elif y < cunt:
		y = cunt
	
	global img
	global use_x
	global l
	global new_image

	w, h = image.width, image.height
	x = ((x-cunt)/(wanted_size-cunt*2)) * (w-wanted_size)
	y = ((y-cunt)/(wanted_size-cunt*2)) * (h-wanted_size)

	new_image = image.crop((
		0+x*use_x, 0+y*(not use_x),
		wanted_size+x*use_x, wanted_size+y*(not use_x)))
	img = ImageTk.PhotoImage(new_image)
	swap_images()
	

fstr = str(wanted_size)+'\n'

# TODO: SAVE THE CONFIGURATION OF EACH SAVED IMAGE, SO THAT IF YOU DECIDE TO CHANGE THE SIZE OF
# AI INPUT IMAGES MIDWAY, YOU DONT HAVE TO REDO EVERYTHING
def on_lclick(event):
	open_next_image(1)
	swap_images()

def on_close():
	global fstr
	global dname
	print(fstr)
	f = open(dname+'2/cfg.txt', 'w')
	f.write(fstr)
	f.close()
	root.destroy()

def on_rclick(event):
	open_next_image(0)
	swap_images()

# Create Tkinter Object
root = Tk()

root.protocol("WM_DELETE_WINDOW", on_close)
root.bind('<Motion>', motion)
root.bind("<Button-1>", on_lclick)
root.bind("<Button-3>", on_rclick)

dname = 'data_men'

if not os.path.isdir(dname+'2'):
	os.mkdir(dname+'2')
if not os.path.isdir(dname+'2_filtered'):
	os.mkdir(dname+'2_filtered')

def open_next_image(is_ok):
	global i
	global fstr
	global x
	global y
	if is_ok:
		fstr += str(i) + ' ' + str(real_x) + ' ' + str(real_y)+'\n'
		new_image.save(dname+"2/"+str(i)+".png")
	i+=1
	open_image(dname+"/"+str(i)+".jpg")

# Importing the image
def open_image(fp):
	global image
	global use_x
	global fstr
	image = Image.open(fp)
	w, h = image.width, image.height
	image = image.convert('L')
	if w > h:
		use_x = 1
		image = image.resize((int(w*(wanted_size/h)), wanted_size))
	else:
		use_x = 0
		image = image.resize((wanted_size, int(h*(wanted_size/w))))
	if w == h:
		fstr += str(i) + ' 0 0\n'
		image.save(dname+"2_filtered/"+str(i)+".png")
		open_next_image(0)
	#landscape image

open_image(dname+"/"+str(i)+".jpg")

img = ImageTk.PhotoImage(image.crop((0+x,0+y, wanted_size+x, wanted_size+y)))
l = Label(image=img)
l.image = img
l.pack()

# Execute Tkinterdef task():
def task():
	global x
	global y
	#x+=1
	# do something
	root.update()

while 1:
   task()
