//
// Created by Dillon on 4/22/2022.
//

#ifndef TACTILESYNTH_OSC_H
#define TACTILESYNTH_OSC_H

#include <atomic>
#include <stdint.h>
#include <ctime>

#define STANDARD_AMPLITUDE 0.3
#define MAX_VOICES 16

class Osc {
public:
    Osc();
    void setWaveOn(bool isWaveOn);
    void setFrequency(double frequency);
    float render(int frame);
    void setSampleRate(int32_t sampleRate);
    bool isWaveOn();
    void setPhase(double offset);
    void setVoices(int voices);
    double amplitude_ = 1.0;

    void setSpread(double spread);

    void setVoicesVolume(double volume);

    void setVolume(double volume);

    void setAttack(double amount);

private:
    double currentAmp=0;
    double volume_ = 1;
    double phase_ = 0.0;
    double phaseIncrement_ = 0.0;
    double frequency_ = 440.0;
    double sampleRate_;
    int waveform_ = 1;
    int MAX_ATTACK = 50000;
    int MIN_ATTACK = 500;
    double envelope_[4] = {5000, 1500, 1.0, 20000}; //adsr, measured in ms or percent of amplitude
    clock_t oscStartTime_ = clock();
    clock_t oscStopTime_ = clock();
    int attack_sustain_release = 0;
    double attackTime_ = 50;
    double releaseTime = 500;
    int voices_ = MAX_VOICES;
    float voicesVolume_ = 0;
    double voice_blend_ = .7;
    double spread_ = 0.1;
    double voiceStates_[MAX_VOICES][3];

    float getWaveformData(int waveformIndex, double phase);

    void phaseVoices();

    void tuneVoices();

    double getPhaseIncrement(double frequency);

    void setVoicePhaseIncrements();

    void setVoicePhaseIncrement(int voiceId);

};


#endif //TACTILESYNTH_OSC_H
