//
// Created by Dillon on 4/22/2022.
//

#include "Osc.h"
#include <math.h>
#include <android/log.h>
#include <time.h>

#define TWO_PI (3.14159 * 2)

Osc::Osc() {
    phaseVoices();
}

void Osc::setSampleRate(int32_t sampleRate) {
    sampleRate_ = (double) sampleRate;
}

void Osc::setWaveOn(bool isWaveOn) {
    if (isWaveOn) {
        oscStartTime_ = clock();
        attack_sustain_release = 1;
    } else {
        oscStopTime_ = clock();
        attack_sustain_release = 3;
    }
}

void Osc::setFrequency(double frequency) {
    frequency_ = frequency;
    phaseIncrement_ = getPhaseIncrement(frequency);
    tuneVoices();
}

float Osc::render(int frame) {
    float audioData = 0;

    if (attack_sustain_release != 0) {
        // calculate next sample value for wave
        audioData = (float) getWaveformData(waveform_, phase_);

        // voices
        int maxMid = floor(MAX_VOICES / 2);
        int currentMid = floor(voices_ / 2);
        int start = maxMid - currentMid;
        int end = maxMid + currentMid;


        for (int i = start; i < end; i++) {
            // add waveform data
            double voicePhase = voiceStates_[i][1];
            audioData += getWaveformData(waveform_, voicePhase) * abs(voicesVolume_);
            // increment phase
            double voicePhaseIncrement = voiceStates_[i][2];
            voiceStates_[i][1] = fmod(voicePhase + voicePhaseIncrement, TWO_PI);
        }

        audioData /= MAX_VOICES;


        // adjust volume for attack
        if (attack_sustain_release == 1) {
            int uptime = (clock() - oscStartTime_);
            float uptimeRatio = uptime / envelope_[0];
            float multiplier = uptimeRatio + (currentAmp * (1 - uptimeRatio));
            audioData *= fmin(multiplier, 1);
            if (uptime >= envelope_[0]) {
                attack_sustain_release = 2;
            }
        }

        // adjust volume for release
        if (attack_sustain_release == 3) {
            int downTime = (clock() - oscStopTime_);
            float ratio = fmin(downTime / (envelope_[3] * .5), 1);
            audioData *= 1 - ratio;
            if (downTime >= envelope_[3]) {
                attack_sustain_release = 0;
            }
        }

        currentAmp = audioData;

        // increment phase
        phase_ = fmod((phase_ + phaseIncrement_), TWO_PI);
    }

    return audioData;
}

float Osc::getWaveformData(int waveformIndex, double phase) {
    // saw waveform
    return 1 - (phase / TWO_PI);
}

bool Osc::isWaveOn() {
    return attack_sustain_release != 0;
}

void Osc::setPhase(double offset) {
    phase_ = TWO_PI * offset;
}

void Osc::setVoices(int amount) {
    voices_ = fmin(amount, MAX_VOICES);
}

void Osc::setSpread(double amount) {
    spread_ = amount;
}

// randomize all voice phases
void Osc::phaseVoices() {
    // generate voice phases
    for (int i = 0; i < MAX_VOICES; i++) {
        double phase = ((float)(rand() % 100) / 100) * TWO_PI;
        voiceStates_[i][1] = phase;
    }
}

// set voice frequencies
void Osc::tuneVoices() {
    int mid = floor(MAX_VOICES / 2);
    for (int i = 0; i < MAX_VOICES; i++) {
        double octaveDistance = frequency_;
        double noteDistance = octaveDistance / 12;
        double spreadDistance = noteDistance * spread_;
        double voiceDistance = spreadDistance / MAX_VOICES;

        // set frequency
        voiceStates_[i][0] = frequency_ + (i - mid) * voiceDistance;

        // set phase increment
        voiceStates_[i][2] = getPhaseIncrement(voiceStates_[i][0]);
    }
}

// get phase increment given frequency
double Osc::getPhaseIncrement(double frequency) {
    return (TWO_PI * frequency) / sampleRate_;
}

void Osc::setVoicesVolume(double volume) {
    voicesVolume_ = volume;
}

void Osc::setVolume(double volume) {
    volume_ = volume;
}

void Osc::setAttack(double amount) {
    envelope_[0] = amount * MAX_ATTACK;
}
