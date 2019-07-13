#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_task_sunrisesunset_SunriseSunsetApp_invokeNativeFunction(JNIEnv *env, jobject instance) {
        return env->NewStringUTF("");
    }
}