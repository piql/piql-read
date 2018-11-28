### What is this repository for? ###

* This repository is for the Piql film-reader App for android.
### How do I get set up? ###

To set up this project you will need to download the opencv SDK and have android studio installed.
To import project:


1. Clone this repository
1. Open up the project in android studio.
1. Download and extract opencv library version 3.4.0 for android [here](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.4.0/opencv-3.4.0-android-sdk.zip/download) 
1. Go to File>new>import module and open the opencv library module OpenCV-android-sdk/sdk/java/
1. Go to File>project structure> and click on module opencvlivrary340. 
1. Next select the property tab and change Compile Sdk Version to 26, then select the flavors tab and change Min Sdk Version to 21 and Target Sdk version to 26
1. Then click the module app, select the Dependencies tab and add a new module and select opencvlivrary340
1. Copy the contents of(or symlink) OpenCV-android-sdk/sdk/native/jni to FilmReader/app/src/main/jniLibs (create jniLibs folder. Case sensitive)

If everything went correctly, the project should be imported and ready to use.

If there is any problems importing the project or something else contact us at:
Haakon Heggholmen: 	mail: heggholmen_3@hotmail.com  	+47 47148413
Christian Haadem: 	mail: chrizzsh@gmail.com 			+47 94143712
Even Maaren Stende: mail: ewen_37@hotmail.com			+47 94889248