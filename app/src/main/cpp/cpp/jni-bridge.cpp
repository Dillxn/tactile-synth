#include <jni.h>
#include <string>
#include <android/input.h>
#include "AudioEngine.h"

static AudioEngine *audioEngine = new AudioEngine();


extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_SynthFragment_startEngine(JNIEnv *env, jobject thiz) {
    audioEngine->start();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_SynthFragment_stopEngine(JNIEnv *env, jobject thiz) {
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
Java_com_dillxn_tactilesynth_Synth_setReverb(JNIEnv *env, jobject thiz,
                                             jdouble reverb) {
    audioEngine->setReverb(reverb);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscVoicesVolume(JNIEnv *env, jobject thiz, jint oscId,
                                                      jdouble volume) {
    audioEngine->setOscVoicesVolume(oscId, volume);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscVolume(JNIEnv *env, jobject thiz, jint oscId,
                                                jdouble volume) {
    audioEngine->setOscVolume(oscId, volume);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setOscAttack(JNIEnv *env, jobject thiz, jint oscId,
                                                jdouble amount) {
    audioEngine->setOscAttack(oscId, amount);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setBitCrush(JNIEnv *env, jobject thiz, jdouble amount) {
    audioEngine->setBitCrush(amount);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_setFilter(JNIEnv *env, jobject thiz, jdouble amount) {
    audioEngine->setFilter(amount);
}