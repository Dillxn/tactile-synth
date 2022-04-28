//
// Created by Dillon on 4/24/2022.
//

#include "AudioMutator.h"
#include <android/log.h>

AudioMutator::AudioMutator() {
    mVerb.setParameter(MVerb<float>::PREDELAY, 0);
    highPassFilter.Type(highPassFilter.HIGHPASS);
}

void AudioMutator::mutate(void *audioData, int32_t numFrames) {
    float *ad = static_cast<float *>(audioData);

    float digitAmount = ((1 - bitCrushMix_) * 8);
    float multiplier = (float)pow(10, digitAmount);

    for (int i = 0; i < numFrames; i++) {
        ad[i] = 0.f;

        // oscillators
        for (int a = 0; a < MAX_OSCILLATORS; a++) {
            ad[i] += (float)oscillators_[a].render(i);
        }
        // adjust volume for osc count
        ad[i] /= MAX_OSCILLATORS;


        // lowpass
        ad[i] = lowPassFilter(ad[i]);

        // highpass
        ad[i] = highPassFilter(ad[i]);



        // bit crusher
        //if (bitCrushMix_ > .5)
            //ad[i] = floor(ad[i] * (float)multiplier) / (float)multiplier;


        // compress
        ad[i] = ad[i] > 0 ? (float)((pow(ad[i], .6))) : 0;

        // reverb
        ad[i] += mVerb.process(ad[i], numFrames, reverbVolume_);

        // hard clipper
        if (false) {
            if (ad[i] > clipperThreshold_) {
                ad[i] = clipperThreshold_;
            }

            // normalize for clipping
            ad[i] *= 1 / clipperThreshold_;
        }

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
    lowPassFilter.SetSampleRate(rate);
    highPassFilter.SetSampleRate(rate);
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

void AudioMutator::setReverb(double reverb) {
    //mVerb.setParameter(MVerb<float>::DECAY, reverb);
    reverbVolume_ = reverb;
}

void AudioMutator::setOscVoicesVolume(int oscId, double volume) {
        oscillators_[oscId].setVoicesVolume(volume);
}

void AudioMutator::setOscVolume(int oscId, double volume) {
        oscillators_[oscId].setVolume(volume);
}

void AudioMutator::setOscAttack(int oscId, double amount) {
        oscillators_[oscId].setAttack(amount);
}

void AudioMutator::setBitCrush(double amount) {
    bitCrushMix_ = amount;
}

void AudioMutator::setFilter(double amount) {

    lowPassFrequency_ = MAX_FREQUENCY - (pow(fmax(amount, 0),.2) * MAX_FREQUENCY);
    lowPassFilter.Frequency(lowPassFrequency_);

    highPassFrequency_ = (float)pow(abs(fmin(amount,0)),2) * MAX_FREQUENCY;
    highPassFilter.Frequency(highPassFrequency_);
}
