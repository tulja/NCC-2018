import qrcode
img = qrcode.make('Some data here')
img.save('test1.png')
for i in range(1,401):
        if i > 0 and i < 10:
		filename = 'NCC_2018_00'+str(i)
	elif i > 10 and i < 100:
		filename = 'NCC_2018_0'+str(i)
	else:
		filename = 'NCC_2018_'+str(i)
	img = qrcode.make(filename)	
	img.save(filename+".png")
