//
// Created by hcon on 14.03.18.
//Used to interface with java.
//
#include <jni.h>
#include "string.h"

JNIEXPORT jstring JNICALL
Java_no_ntnu_bachelor2018_filmreader_PiqlLib_Wrapper_test2(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "asdf");
}