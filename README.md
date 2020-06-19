# ImageFind

ImageFind lets you search the text in your images by running machine learning OCR algorithm on your gallery.
The app was programmed with privacy in mind and the OCR is done completley on-device.

## Scanning images

<img src="https://github.com/MrHorvatHD/ImageFind/blob/master/img/scan.png" width="540" height="600"/>
![Scan light](./img/scan.png) 

Scanning is performed by selecting picture folders and processing the images with OCR algorithm. Scanning images is performed in the bacground on another thread. Because everything is processed on-device depending on the CPU and image count, the scan might take a while.

## Searching images

<img src="https://github.com/MrHorvatHD/ImageFind/blob/master/img/search.png" width="540" height="600"/>
![Search light](./img/search.png) 

Searching is performed by typing in the search bar in the toolbar. Found image list is refreshed live while you type.

## Opening image

<img src="https://github.com/MrHorvatHD/ImageFind/blob/master/img/photoUpClose.jpg" width="270" height="600"/>
![Searched photo up close](./img/photoUpClose.jpg)

By clicking on the image, it's opened in the default gallery app or alternatively the user is presented with a list of adequate apps.
