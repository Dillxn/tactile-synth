#include "AudioGenerator.h"
#include <android/log.h>

AudioGenerator::AudioGenerator() {
    mVerb.setParameter(MVerb<float>::PREDELAY, 0);
    highPassFilter.Type(highPassFilter.HIGHPASS);
}

void AudioGenerator::generate(void *audioData, int32_t numFrames) {
    float *ad = static_cast<float *>(audioData);

    float digitAmount = ((1 - bitCrushMix_) * 8);
    float multiplier = (float)pow(10, digitAmount);

    for (int i = 0; i < numFrames; i++) {
        ad[i] = 0.f;

        /// oscillators
        for (int a = 0; a < MAX_OSCILLATORS; a++) {
            ad[i] += (float)oscillators_[a].render(i);
        }
        ad[i] /= MAX_OSCILLATORS;


        /// bit crusher
        if (bitCrushMix_ > .5) {
            //ad[i] = floor(ad[i] * (float)multiplier) / (float)multiplier;
        }


        /// compress
        ad[i] = pow(ad[i], .5);


        /// lowpass
        lowPassFilter.Resonance(pow(lowPassAmount_, .4) * .9);
        ad[i] = lowPassFilter(ad[i]);
        /// highpass
        ad[i] = highPassFilter(ad[i]);

        /// reverb
        float reverbFrame = mVerb.process(ad[i], numFrames, reverbVolume_);
        ad[i] += reverbFrame;
        ad[i] /= 2;


        // hard clipper
        if (ad[i] > clipperThreshold_) {
            ad[i] = clipperThreshold_;
        }

        // normalize for clipping
        ad[i] *= 1 / clipperThreshold_;

    }


}

void AudioGenerator::toggleOsc(int oscId, bool isToneOn) {
    oscillators_[oscId].setWaveOn(isToneOn);
    countOscillators();
}

void AudioGenerator::setOscFrequency(int oscId, double frequency) {
    oscillators_[oscId].setFrequency(frequency);
}

void AudioGenerator::setOscPhase(int oscId, double offset) {
    oscillators_[oscId].setPhase(offset);
}

void AudioGenerator::setSampleRate(int32_t rate) {
    for (int x = 0; x < MAX_OSCILLATORS; x++) {
        oscillators_[x].setSampleRate(rate);
    }
    mVerb.setSampleRate(rate);
    lowPassFilter.SetSampleRate(rate);
    highPassFilter.SetSampleRate(rate);
}

bool AudioGenerator::isOscDown(int oscId) {
    return oscillators_[oscId].isWaveOn();
}

void AudioGenerator::setOscVoices(int oscId, int voices) {
    oscillators_[oscId].setVoices(voices);
}

void AudioGenerator::setOscSpread(int oscId, double spread) {
    oscillators_[oscId].setSpread(spread);
}

void AudioGenerator::countOscillators() {
    int tempOscCount = 0;
    for (int x = 0; x < MAX_OSCILLATORS; x++)
        if (oscillators_[x].isWaveOn())
            tempOscCount++;

    oscCount_ = tempOscCount;
}

void AudioGenerator::setReverb(double reverb) {
    //mVerb.setParameter(MVerb<float>::DECAY, reverb);
    reverbVolume_ = reverb;
}

void AudioGenerator::setOscVoicesVolume(int oscId, double volume) {
        oscillators_[oscId].setVoicesVolume(volume);
}

void AudioGenerator::setOscVolume(int oscId, double volume) {
        oscillators_[oscId].setVolume(volume);
}

void AudioGenerator::setOscAttack(int oscId, double amount) {
        oscillators_[oscId].setAttack(amount);
}

void AudioGenerator::setBitCrush(double amount) {
    bitCrushMix_ = amount;
}

void AudioGenerator::setFilter(double amount) {
    lowPassAmount_ = fmax((amount), 0);
    lowPassFrequency_ = MAX_FREQUENCY - (pow(lowPassAmount_,.2) * MAX_FREQUENCY);
    lowPassFilter.Frequency(lowPassFrequency_);

    highPassAmount_ = abs(fmin(amount,0));
    highPassFrequency_ = pow(highPassAmount_, 5) * MAX_FREQUENCY;
    highPassFilter.Frequency(highPassFrequency_);
}
