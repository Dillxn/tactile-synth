#include <android/log.h>
#include "AudioEngine.h"
#include <thread>
#include <mutex>
#include <jni.h>


// double-buffering offers a good tradeoff
// between latency and protection against glitches
constexpr int32_t kBufferSizeInBursts = 4;

std::vector<float> recordedData;
std::mutex recordedDataMutex;

bool _recording = false;

void startRecord() {
    _recording = true;
    recordedData.clear();
}
void stopRecord() {
    _recording = false;
}

int _sampleRate;
int _bufferSize;

// this is called as a step in creating the audio stream
// giving us audioData, a hook into the audio stream raw data
aaudio_data_callback_result_t dataCallback(AAudioStream *stream, void *userData, void *audioData, int32_t numFrames) {
    // first we call 'generate' which renders the oscillators
    // and mutates it with effects
    ((AudioGenerator *) (userData))->generate(audioData, numFrames);

    if (_recording) {
        // Record the generated audio data to the global recordedData vector.
        size_t numBytes = numFrames * sizeof(float);
        float *dataFloats = static_cast<float *>(audioData);
        std::lock_guard<std::mutex> lock(recordedDataMutex);
        recordedData.insert(recordedData.end(), dataFloats, dataFloats + numFrames);
    }

    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

void errorCallback(AAudioStream *stream,
                   void *userData,
                   aaudio_result_t error) {
    if (error == AAUDIO_ERROR_DISCONNECTED) {
        std::function<void(void)> restartFunction =
                std::bind(&AudioEngine::restart,
                          static_cast<AudioEngine *>(userData));
        new std::thread(restartFunction);
    }
}

bool AudioEngine::start() {
    // pass in aaudio information
    AAudioStreamBuilder *streamBuilder;
    AAudio_createStreamBuilder(&streamBuilder);
    AAudioStreamBuilder_setFormat(streamBuilder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setChannelCount(streamBuilder, 1);
    AAudioStreamBuilder_setPerformanceMode(streamBuilder,
                                           AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setDataCallback(streamBuilder,
                                        ::dataCallback, &audioMutator);
    AAudioStreamBuilder_setErrorCallback(streamBuilder,
                                         ::errorCallback, this);

    // open the stream
    aaudio_result_t result = AAudioStreamBuilder_openStream(streamBuilder,
                                                            &stream_);
    if (result != AAUDIO_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioEngine",
                            "Error opening stream %s",
                            AAudio_convertResultToText(result));
        return false;
    }

    // retrieve sample rate of stream for osc
    int32_t sampleRate = AAudioStream_getSampleRate(stream_);
    audioMutator.setSampleRate(sampleRate);
    _sampleRate = sampleRate;

    // set buffer size
    int32_t bufferSize = AAudioStream_getFramesPerBurst(stream_) * kBufferSizeInBursts;
    AAudioStream_setBufferSizeInFrames(
            stream_, bufferSize);
    _bufferSize = bufferSize;

    // start the stream
    result = AAudioStream_requestStart(stream_);
    if (result != AAUDIO_OK) {
        __android_log_print(ANDROID_LOG_ERROR,
                            "AudioEngine",
                            "Error starting stream %s",
                            AAudio_convertResultToText(result));
        return false;
    }
    AAudioStreamBuilder_delete(streamBuilder);

    return true;
}

void AudioEngine::restart() {
    static std::mutex restartingLock;
    if (restartingLock.try_lock()) {
        stop();
        start();
        restartingLock.unlock();
    }
}

void AudioEngine::stop() {
    if (stream_ != nullptr) {
        AAudioStream_requestStop(stream_);
        AAudioStream_close(stream_);
    }
}

void AudioEngine::toggleOsc(int oscId, bool isToneOn) {
    audioMutator.toggleOsc(oscId, isToneOn);
}

void AudioEngine::setOscFrequency(int oscId, double frequency) {
    audioMutator.setOscFrequency(oscId, frequency);
}

void AudioEngine::setOscPhase(int oscId, double offset) {
    audioMutator.setOscPhase(oscId, offset);
}

bool AudioEngine::isOscDown(int oscId) {
    return audioMutator.isOscDown(oscId);
}

void AudioEngine::setOscVoices(int oscId, int voices) {
    audioMutator.setOscVoices(oscId, voices);
}

void AudioEngine::setOscSpread(int oscId, double spread) {
    audioMutator.setOscSpread(oscId, spread);
}

void AudioEngine::setReverb(double reverb) {
    audioMutator.setReverb(reverb);
}

void AudioEngine::setOscVoicesVolume(int oscId, double volume) {
    audioMutator.setOscVoicesVolume(oscId, volume);
}

void AudioEngine::setOscVolume(int oscId, double volume) {
    audioMutator.setOscVolume(oscId, volume);
}

void AudioEngine::setOscAttack(int oscId, double amount) {
    audioMutator.setOscAttack(oscId, amount);
}

void AudioEngine::setBitCrush(double amount) {
    audioMutator.setBitCrush(amount);
}

void AudioEngine::setFilter(double amount) {
    audioMutator.setFilter(amount);
}

// starts collecting generated audio data
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_startRecord(JNIEnv *env, jobject thiz) {
    startRecord();
}

// stops the engine from recording
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_Synth_stopRecord(JNIEnv *env, jobject thiz) {
    stopRecord();
}

// returns a float array containing the currently stored recorded data
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_dillxn_tactilesynth_Synth_getRecordedAudioData(JNIEnv *env, jobject thiz) {

    _jfloatArray *audioDataArray = env->NewFloatArray(recordedData.size());
    env->SetFloatArrayRegion(audioDataArray, 0, recordedData.size(), (const jfloat*)recordedData.data());

    return audioDataArray;
}

// returns sample rate
extern "C"
JNIEXPORT jint JNICALL
Java_com_dillxn_tactilesynth_Synth_getSampleRate(JNIEnv *env, jobject thiz) {
    return _sampleRate;
}

// returns buffer size
extern "C"
JNIEXPORT jint JNICALL
Java_com_dillxn_tactilesynth_Synth_getBufferSize(JNIEnv *env, jobject thiz) {
    return _bufferSize;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_PlaybackHandler_startRecord(JNIEnv *env, jobject thiz) {
    startRecord();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_dillxn_tactilesynth_PlaybackHandler_stopRecord(JNIEnv *env, jobject thiz) {
    stopRecord();
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_dillxn_tactilesynth_PlaybackHandler_getRecordedAudioData(JNIEnv *env, jobject thiz) {
    _jfloatArray *audioDataArray = env->NewFloatArray(recordedData.size());
    env->SetFloatArrayRegion(audioDataArray, 0, recordedData.size(), (const jfloat*)recordedData.data());

    return audioDataArray;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_dillxn_tactilesynth_PlaybackHandler_getSampleRate(JNIEnv *env, jobject thiz) {
    return _sampleRate;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_dillxn_tactilesynth_PlaybackHandler_getBufferSize(JNIEnv *env, jobject thiz) {
    return _bufferSize;
}