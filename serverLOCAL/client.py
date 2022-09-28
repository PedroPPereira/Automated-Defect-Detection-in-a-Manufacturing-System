from flask import request

url = 'http://127.0.0.1:4500/uploader'
my_img = {'image': open('product.jpg', 'rb')}
r = request.post(url, files=my_img)

# convert server response into JSON format.
print(r.json())