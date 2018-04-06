### What is this repository for? ###

* This repository is for the Piql film-reader App for android.
### How do I get set up? ###

To set up this project you will need to download the opencv SDK and have android studio installed.
To import project:


1. Clone this repository
1. Open up the project in android studio.
1. Download and extract opencv library version 3.4.0 for android [here](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.4.0/opencv-3.4.0-android-sdk.zip/download) 
1. Go to File>new>import module and open the opencv library module OpenCV-android-sdk/sdk/java/
1. Set module dependency in File>project structure>app>dependencies>+ and choose opencvlivrary340
1. Set the correct sdk versions in the build.gradle file in the opencv module(compileSDKversion 26, targetSDKversion 26, minSDKversion 21)
1. Copy the contents of(or symlink) OpenCV-android-sdk/sdk/native/jni to FilmReader/app/src/main/jniLibs (create jniLibs folder. Case sensitive)

If everything went correctly, the project should be imported and ready to use.

If there is any problems importing the project or something else contact us at:
Håkon Heggholmen: 	mail: heggholmen_3@hotmail.com  	+47 47148413
Christian Hådem: 	mail: chrizzsh@gmail.com 			+47 94143712
Even Måren Stende:  									+47 94889248