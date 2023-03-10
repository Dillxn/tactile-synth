//
// Created by Dillon on 4/23/2022.
//

#ifndef TACTILESYNTH_AUDIOENGINE_H
#define TACTILESYNTH_AUDIOENGINE_H


#include <aaudio/AAudio.h>
#include "Osc.h"
#include "AudioMutator.h"

class AudioEngine {
public:
    bool start();
    void stop();
    void restart();
    void toggleOsc(int oscId, bool isToneOn);
    void setOscFrequency(int oscId, double frequency);
    void setOscPhase(int oscId, double offset);
    bool isOscDown(int oscId);
    void setOscVoices(int oscId, int voices);

    void setOscSpread(int i, double d);

    void setReverb(double reverb);

    void setOscVoicesVolume(int oscId, double volume);

    void setOscVolume(int oscId, double volume);

    void setOscAttack(int oscId, double amount);

    void setBitCrush(double amount);

    void setFilter(double amount);

private:
    AAudioStream *stream_;
    AudioMutator audioMutator;
};


#endif //TACTILESYNTH_AUDIOENGINE_H
