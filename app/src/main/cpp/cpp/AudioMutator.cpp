//
// Created by Dillon on 4/24/2022.
//

#include "AudioMutator.h"
#include <android/log.h>

AudioMutator::AudioMutator() {
    mVerb.setParameter(MVerb<float>::PREDELAY, 0);
}

void AudioMutator::mutate(void *audioData, int32_t numFrames) {
    float *ad = static_cast<float *>(audioData);

    // oscillators
    for (int i = 0; i < numFrames; i++) {
        ad[i] = 0.f;
        // for all active oscillators
        for (int a = 0; a < MAX_OSCILLATORS; a++) {
            ad[i] += (float)oscillators_[a].render(i);
        }
        // adjust volume for osc count
        ad[i] /= MAX_OSCILLATORS + 1;
    }

    // reverb
    float reverb[(int)numFrames];
    mVerb.process(ad, reverb, numFrames);
    for (int i = 0; i < numFrames; i++) {
        ad[i] += reverb[i] * reverbVolume_;
    }

    // compressor
    for (int i = 0; i < numFrames; i++) {
        if (ad[i] > compressorThreshold_)
            ad[i] = compressorThreshold_;

        ad[i] *= 1 / compressorThreshold_;
    }


}

void AudioMutator::toggleOsc(int oscId, bool isToneOn) {
    oscillators_[oscId].setWaveOn(isToneOn);
    countOscillators();
}

void AudioMutator::setOscFrequency(int oscId, double frequency) {
    oscillators_[oscId].setFrequency(frequency);
}

void AudioMutator::setOscPhase(int oscId, double offset) {
    oscillators_[oscId].setPhase(offset);
}

void AudioMutator::setSampleRate(int32_t rate) {
    for (int x = 0; x < MAX_OSCILLATORS; x++) {
        oscillators_[x].setSampleRate(rate);
    }
    mVerb.setSampleRate(rate);
}

bool AudioMutator::isOscDown(int oscId) {
    return oscillators_[oscId].isWaveOn();
}

void AudioMutator::setOscVoices(int oscId, int voices) {
    oscillators_[oscId].setVoices(voices);
}

void AudioMutator::setOscSpread(int oscId, double spread) {
    oscillators_[oscId].setSpread(spread);
}

void AudioMutator::countOscillators() {
    int tempOscCount = 0;
    for (int x = 0; x < MAX_OSCILLATORS; x++)
        if (oscillators_[x].isWaveOn())
            tempOscCount++;

    oscCount_ = tempOscCount;
}

void AudioMutator::setOscReverb(int oscId, double reverb) {
    mVerb.setParameter(MVerb<float>::DECAY, reverb);
    reverbVolume_ = reverb;
}

void AudioMutator::setOscVoicesVolume(int oscId, double volume) {
    for (int x = 0; x < MAX_OSCILLATORS; x++)
        oscillators_[x].setVoicesVolume(volume);
}

void AudioMutator::setOscVolume(int oscId, double volume) {
    for (int x = 0; x < MAX_OSCILLATORS; x++)
        oscillators_[x].setVolume(volume);
}
