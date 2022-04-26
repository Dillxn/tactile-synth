//
// Created by Dillon on 4/24/2022.
//

#ifndef TACTILESYNTH_AUDIOMUTATOR_H
#define TACTILESYNTH_AUDIOMUTATOR_H

#define MAX_OSCILLATORS 10

#include "Osc.h"

class AudioMutator {
public:
    void mutate(void *audioData, int32_t numFrames);

    void toggleOsc(int oscId, bool isToneOn);

    void setOscFrequency(int oscId, double frequency);

    void setSampleRate(int32_t rate);

    void setOscPhase(int oscId, double offset);

    bool isOscDown(int oscId);

    void setOscVoices(int oscId, int voices);

    void setOscSpread(int oscId, double spread);

private:
    Osc oscillators_[MAX_OSCILLATORS];
    int oscCount_;

    void countOscillators();
};


#endif //TACTILESYNTH_AUDIOMUTATOR_H
