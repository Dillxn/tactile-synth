#include <jni.h>
#include <string>
#include <android/input.h>
#include "AudioEngine.h"

static AudioEngine *audioEngine = new AudioEngine();


extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_MainActivity_startEngine(JNIEnv *env, jobject thiz) {
    audioEngine->start();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_MainActivity_stopEngine(JNIEnv *env, jobject thiz) {
    audioEngine->stop();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscFrequency(JNIEnv *env, jobject thiz, jint oscId, jdouble frequency) {
    audioEngine->setOscFrequency(oscId, frequency);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_toggleOsc(JNIEnv *env, jobject thiz, jint oscId, jboolean toggle) {
    audioEngine->toggleOsc(oscId, toggle);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscPhase(JNIEnv *env, jobject thiz, jint oscId, jdouble offset) {
    audioEngine->setOscPhase(oscId, offset);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dillxn_tactilesynth_Synth_isOscDown(JNIEnv *env, jobject thiz, jint oscId) {
    return audioEngine->isOscDown(oscId);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscVoices(JNIEnv *env, jobject thiz, jint oscId,
                                                jint voices) {
    audioEngine->setOscVoices(oscId, voices);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscSpread(JNIEnv *env, jobject thiz, jint oscId,
                                                jdouble spread) {
    audioEngine->setOscSpread(oscId, spread);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscReverb(JNIEnv *env, jobject thiz, jint oscId,
                                                jdouble reverb) {
    audioEngine->setOscReverb(oscId, reverb);
}