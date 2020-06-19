# ImageFind

ImageFind lets you search the text in your images by running machine learning OCR algorithm on your gallery.
The app was programmed with privacy in mind and the OCR is done completley on-device.

## Scanning images

![Scan light](./img/scanLight.jpg =540x1200) | ![Scan dark](./img/scanDark.jpg =540x1200)

Scanning is performed by selecting picture folders and processing the images with OCR algorithm. Scanning images is performed in the bacground on another thread. Because everything is processed on-device depending on the CPU and image count, the scan might take a while.

## Searching images

![Search light](./img/searchLight.jpg =540x1200) | ![Search dark](./img/searchDark.jpg =540x1200)

Searching is performed by typing in the search bar in the toolbar. Found image list is refreshed live while you type.

## Opening image

![Searched photo up close](./img/photoUpClose.jpg =540x1200)

By clicking on the image, it's opened in the default gallery app or alternatively the user is presented with a list of adequate apps.
