#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_filmreader_bacheloroppg_ntnu_no_filmreader_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
