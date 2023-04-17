package com.dillxn.tactilesynth;

import android.media.AudioTrack;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

// Use AndroidJUnit4 as the test runner for Android-specific testing
@RunWith(AndroidJUnit4.class)
public class MetronomeInstrumentedTest {

    // Declare Metronome and AudioTrack objects to be used throughout the test class
    private Metronome metronome;
    private AudioTrack audioTrack;

    // The setup method runs before each test and initializes the Metronome and AudioTrack instances
    @Before
    public void setup() {
        // Launch MainActivity in order to initialize our singletons
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        metronome = Metronome.getInstance();
        audioTrack = Mockito.mock(AudioTrack.class);
    }

    // Tests the getBeatInterval method of the Metronome class
    @Test
    public void testGetBeatInterval() {
        int bpm = Database.getInstance().getPreset().optInt("bpm");
        double expectedBeatInterval = 60000.0 / bpm;

        double actualBeatInterval = metronome.getBeatInterval();

        assertEquals(expectedBeatInterval, actualBeatInterval, 0.001);
    }

    // The testPlaySound method tests the playSound method of the Metronome class
    @Test
    public void testPlaySound() throws InterruptedException {
        // Set the mock AudioTrack for the Metronome instance
        metronome.setAudioTrack(audioTrack);
        boolean isDownBeat = true;

        // Call the playSound method with isDownBeat parameter
        metronome.playSound(isDownBeat);

        // Sleep for a while to let the playSound() method finish
        Thread.sleep(200); // Adjust the sleep duration as needed

        // Verify that the play method of the AudioTrack instance is called
        verify(audioTrack).play();

        // Verify that the write method of the AudioTrack instance is called at least once
        // with the appropriate parameters (short[] buffer, 0 as offset, and 4800 as the buffer size)
        verify(audioTrack, atLeastOnce()).write(any(short[].class), eq(0), eq(4800));

        // Verify that the pause method of the AudioTrack instance is called within a 250ms timeout
        verify(audioTrack, timeout(250)).pause();
    }

    @Test
    public void testCreateMetronomeSound() {
        // Get the Metronome instance
        Metronome metronome = Metronome.getInstance();

        // Generate a sound buffer for a downbeat
        short[] downbeatBuffer = metronome.createMetronomeSound(true);

        // Generate a sound buffer for a non-downbeat
        short[] nonDownbeatBuffer = metronome.createMetronomeSound(false);

        // Check if both buffers have the same length
        assertEquals(downbeatBuffer.length, nonDownbeatBuffer.length);

        // Compare the output buffers element-wise and assert they're not equal
        boolean buffersAreEqual = true;
        for (int i = 0; i < downbeatBuffer.length; i++) {
            if (downbeatBuffer[i] != nonDownbeatBuffer[i]) {
                buffersAreEqual = false;
                break;
            }
        }

        assertFalse("The output buffers for downbeat and non-downbeat should not be equal", buffersAreEqual);
    }

    @Test
    public void testCreateMetronomeSound_DownbeatsAreEqual() {
        Metronome metronome = Metronome.getInstance();

        short[] downbeatBuffer1 = metronome.createMetronomeSound(true);
        short[] downbeatBuffer2 = metronome.createMetronomeSound(true);

        assertEquals(downbeatBuffer1.length, downbeatBuffer2.length);

        boolean buffersAreEqual = true;
        for (int i = 0; i < downbeatBuffer1.length; i++) {
            if (downbeatBuffer1[i] != downbeatBuffer2[i]) {
                buffersAreEqual = false;
                break;
            }
        }

        assertTrue("The output buffers for two downbeats should be equal", buffersAreEqual);
    }

    @Test
    public void testCreateMetronomeSound_NonDownbeatsAreEqual() {
        Metronome metronome = Metronome.getInstance();

        short[] nonDownbeatBuffer1 = metronome.createMetronomeSound(false);
        short[] nonDownbeatBuffer2 = metronome.createMetronomeSound(false);

        assertEquals(nonDownbeatBuffer1.length, nonDownbeatBuffer2.length);

        boolean buffersAreEqual = true;
        for (int i = 0; i < nonDownbeatBuffer1.length; i++) {
            if (nonDownbeatBuffer1[i] != nonDownbeatBuffer2[i]) {
                buffersAreEqual = false;
                break;
            }
        }

        assertTrue("The output buffers for two non-downbeats should be equal", buffersAreEqual);
    }
}
