from flask import Flask, render_template, request, json
import tensorflow as tf
import numpy as np
#import gunicorn
"""
set FLASK_APP=app.py
set FLASK_DEBUG=1
flask run
"""


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploaded/image'
model = tf.keras.models.load_model('srcim_model12_96.h5') #'srcim_model12_96.h5'

@app.route('/')
def upload_f():
    return render_template('upload.html')


@app.route('/uploader', methods=['POST'])
def upload_file():

    file = request.files['file']
    #file = request.files['QualityControlStation']
    file.save("product1.jpg")

    img = tf.keras.preprocessing.image.load_img("product1.jpg", target_size=(224, 224))
    # img = tf.keras.preprocessing.image.load_img('product.jpg', target_size=(224, 224))
    img_array = tf.keras.preprocessing.image.img_to_array(img) / 255
    img_array = tf.expand_dims(img_array, 0)  # Create a batch
    pred = model.predict(np.vstack([img_array]))
    if np.argmax(pred, axis=1) == 0:
        respo = "NOK"
    if np.argmax(pred, axis=1) == 1:
        respo = "OK"
    print(respo)
    app.response_class(json.dumps(respo), status=200)
    return app.response_class(json.dumps(respo), status=200)

# return render_template('pred.html', val=ss)


if __name__ == '__main__':
    app.run(debug=True)#, port=4500)
