#ifndef TACTILESYNTH_AUDIOGENERATOR_H
#define TACTILESYNTH_AUDIOGENERATOR_H

#define MAX_OSCILLATORS 5
#define MAX_FREQUENCY 20000

#include "Osc.h"
#include "MVerb.h"
#include <vector>
#include <cmath>

class AudioGenerator
{
public:
    AudioGenerator();

    void generate(void *audioData, int32_t numFrames);

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

    void setDelay(double amount);

    void setTremoloAmount(float amount);

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

    double delayAmount_ = 0;

    int32_t sampleRate_ = 0;

    std::vector<float> delayBuffer_;
    int delayBufferSize_;
    int delayWritePos_;
    int delayReadPos_;
    int delayTime_;
    float tremoloAmount_;
    float tremoloPhase_;
};

#endif // TACTILESYNTH_AUDIOGENERATOR_H
