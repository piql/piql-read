#include <jni.h>

JNIEXPORT jstring JNICALL
Java_no_ntnu_bachelor2018_filmreader_PiqlLib_Wrapper_test(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, returnValue);
}