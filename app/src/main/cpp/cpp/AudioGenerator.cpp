#include "AudioGenerator.h"
#include <android/log.h>

AudioGenerator::AudioGenerator()
{
    mVerb.setParameter(MVerb<float>::PREDELAY, 0);
    highPassFilter.Type(highPassFilter.HIGHPASS);

    // Initialize the delay buffer and variables
    delayBufferSize_ = 0;
    delayWritePos_ = 0;
    delayReadPos_ = 0;
    delayTime_ = 0;
    tremoloAmount_ = 0;
    tremoloPhase_ = 0;
}

void AudioGenerator::generate(void *audioData, int32_t numFrames)
{
    float *ad = static_cast<float *>(audioData);

    for (int i = 0; i < numFrames; i++)
    {
        ad[i] = 0.f;

        /// oscillators
        for (int a = 0; a < MAX_OSCILLATORS; a++)
        {
            ad[i] += (float)oscillators_[a].render(i);
        }
        ad[i] /= MAX_OSCILLATORS;

        /// bit crusher
        if (bitCrushMix_ > 0)
        {
            float bitCrushFrame = ad[i];
            bitCrushFrame *= 1000;
            bitCrushFrame = (int)bitCrushFrame;
            bitCrushFrame /= 1000;
            ad[i] = bitCrushFrame * bitCrushMix_ + ad[i] * (1 - bitCrushMix_);
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



        /// delay
        float input = ad[i] * delayAmount_;
        float delayedSample = delayBuffer_[delayReadPos_];
        delayBuffer_[delayWritePos_] = input + (delayedSample * 0.5f); // Feedback: multiply delayedSample by a value less than 1
        ad[i] += delayedSample;

        // Update delay buffer read and write positions
        delayWritePos_ = (delayWritePos_ + 1) % delayBufferSize_;
        delayReadPos_ = (delayReadPos_ + 1) % delayBufferSize_;

        /// hard clipper
        if (ad[i] > clipperThreshold_)
        {
            ad[i] = clipperThreshold_;
        }

        // normalize for clipping
        ad[i] *= 1 / clipperThreshold_;

        /// tremolo
        float calculatedRate = tremoloAmount_ * 5.0f; // Map amount to a range of 0 to 5 Hz
        float calculatedDepth = tremoloAmount_; // Use the same amount value for depth
        tremoloPhase_ += calculatedRate / sampleRate_;
        if (tremoloPhase_ > 1.0f) {
            tremoloPhase_ -= 1.0f;
        }
        float tremolo = 1.0f - calculatedDepth * 0.5f * (1.0f + sin(2.0f * M_PI * tremoloPhase_));
        ad[i] *= tremolo;
    }
}

void AudioGenerator::toggleOsc(int oscId, bool isToneOn)
{
    oscillators_[oscId].setWaveOn(isToneOn);
    countOscillators();
}

void AudioGenerator::setOscFrequency(int oscId, double frequency)
{
    oscillators_[oscId].setFrequency(frequency);
}

void AudioGenerator::setOscPhase(int oscId, double offset)
{
    oscillators_[oscId].setPhase(offset);
}

void AudioGenerator::setSampleRate(int32_t rate)
{
    for (int x = 0; x < MAX_OSCILLATORS; x++)
    {
        oscillators_[x].setSampleRate(rate);
    }
    mVerb.setSampleRate(rate);
    lowPassFilter.SetSampleRate(rate);
    highPassFilter.SetSampleRate(rate);

    // Update delay buffer based on the new sample rate
    delayBufferSize_ = rate / 2; // 1 second of delay at the given sample rate
    delayBuffer_.resize(delayBufferSize_, 0);

    sampleRate_ = rate;
}

bool AudioGenerator::isOscDown(int oscId)
{
    return oscillators_[oscId].isWaveOn();
}

void AudioGenerator::setOscVoices(int oscId, int voices)
{
    oscillators_[oscId].setVoices(voices);
}

void AudioGenerator::setOscSpread(int oscId, double spread)
{
    oscillators_[oscId].setSpread(spread);
}

void AudioGenerator::countOscillators()
{
    int tempOscCount = 0;
    for (int x = 0; x < MAX_OSCILLATORS; x++)
        if (oscillators_[x].isWaveOn())
            tempOscCount++;

    oscCount_ = tempOscCount;
}

void AudioGenerator::setReverb(double reverb)
{
    // mVerb.setParameter(MVerb<float>::DECAY, reverb);
    reverbVolume_ = reverb;
}

void AudioGenerator::setOscVoicesVolume(int oscId, double volume)
{
    oscillators_[oscId].setVoicesVolume(volume);
}

void AudioGenerator::setOscVolume(int oscId, double volume)
{
    oscillators_[oscId].setVolume(volume);
}

void AudioGenerator::setOscAttack(int oscId, double amount)
{
    oscillators_[oscId].setAttack(amount);
}

void AudioGenerator::setBitCrush(double amount)
{
    bitCrushMix_ = amount;
}

void AudioGenerator::setFilter(double amount)
{
    lowPassAmount_ = fmax((amount), 0);
    lowPassFrequency_ = MAX_FREQUENCY - (pow(lowPassAmount_, .2) * MAX_FREQUENCY);
    lowPassFilter.Frequency(lowPassFrequency_);

    highPassAmount_ = abs(fmin(amount, 0));
    highPassFrequency_ = pow(highPassAmount_, 5) * MAX_FREQUENCY;
    highPassFilter.Frequency(highPassFrequency_);
}

void AudioGenerator::setDelay(double amount)
{
    delayAmount_ = amount;
}

void AudioGenerator::setTremoloAmount(float amount)
{
    tremoloAmount_ = abs(amount);
}