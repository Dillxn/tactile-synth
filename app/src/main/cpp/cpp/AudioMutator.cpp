//
// Created by Dillon on 4/24/2022.
//

#include "AudioMutator.h"
#include <android/log.h>

void AudioMutator::mutate(void *audioData, int32_t numFrames) {
    float *ad = static_cast<float *>(audioData);
    for (int i = 0; i < numFrames; i++) {
        ad[i] = 0;
        // for all active oscillators
        for (int a = 0; a < MAX_OSCILLATORS; a++) {
            ad[i] += oscillators_[a].render(i);
        }

        // adjust volume for osc count
        ad[i] /= (float(oscCount_) / 2) + 1;

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
