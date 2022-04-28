//
// Created by Dillon on 4/24/2022.
//

#ifndef TACTILESYNTH_AUDIOMUTATOR_H
#define TACTILESYNTH_AUDIOMUTATOR_H

#define MAX_OSCILLATORS 5
#define MAX_FREQUENCY 20000

#include "Osc.h"
#include "MVerb.h"

class AudioMutator {
public:
    AudioMutator();

    void mutate(void *audioData, int32_t numFrames);

    void toggleOsc(int oscId, bool isToneOn);

    void setOscFrequency(int oscId, double frequency);

    void setSampleRate(int32_t rate);

    void setOscPhase(int oscId, double offset);

    bool isOscDown(int oscId);

    void setOscVoices(int oscId, int voices);

    void setOscSpread(int oscId, double spread);

    void setReverb(double reverb);

    void setOscVoicesVolume(int oscId, double volume);

    void setOscVolume(int oscId, double volume);

    void setOscAttack(int oscId, double amount);

    void setBitCrush(double amount);

    void setFilter(double amount);

private:
    Osc oscillators_[MAX_OSCILLATORS];
    int oscCount_;

    MVerb<float> mVerb;
    float reverbVolume_;
    float bitCrushMix_;
    float lowPassFrequency_;
    float highPassFrequency_;

    StateVariable<float, 4> lowPassFilter;
    StateVariable<float, 4> highPassFilter;

    void countOscillators();

    double clipperThreshold_ = .5;
    float lowPassAmount_ = 0;
    float highPassAmount_ = 0;
};


#endif //TACTILESYNTH_AUDIOMUTATOR_H
