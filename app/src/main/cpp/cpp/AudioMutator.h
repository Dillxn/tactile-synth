//
// Created by Dillon on 4/24/2022.
//

#ifndef TACTILESYNTH_AUDIOMUTATOR_H
#define TACTILESYNTH_AUDIOMUTATOR_H

#define MAX_OSCILLATORS 10

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

    void setOscReverb(int oscId, double reverb);

private:
    Osc oscillators_[MAX_OSCILLATORS];
    int oscCount_;

    MVerb<float> mVerb;
    double reverbVolume_;

    void countOscillators();
};


#endif //TACTILESYNTH_AUDIOMUTATOR_H
