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

private:
    std::atomic<bool> isWaveOn_{false};
    double phase_ = 0.0;
    double phaseIncrement_ = 0.0;
    double frequency_ = 440.0;
    double sampleRate_;
    int waveform_ = 1;
    double envelope_[4] = {0, 1500, 1.0, 5000}; //adsr, measured in ms or percent of amplitude
    clock_t oscStartTime_ = clock();
    clock_t oscStopTime_ = clock();
    bool pendingStop = false;
    double attackTime_ = 50;
    double releaseTime = 500;
    int voices_ = 7;
    double voice_blend_ = .7;
    double spread_ = 0;
    double voiceStates_[MAX_VOICES][3];

    float getWaveformData(int waveformIndex, double phase);

    void phaseVoices();

    void tuneVoices();

    double getPhaseIncrement(double frequency);

    void setVoicePhaseIncrements();

    void setVoicePhaseIncrement(int voiceId);

};


#endif //TACTILESYNTH_OSC_H
