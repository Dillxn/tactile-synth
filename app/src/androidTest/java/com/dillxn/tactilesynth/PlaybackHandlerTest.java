package com.dillxn.tactilesynth;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PlaybackHandlerTest {
    private PlaybackHandler playbackHandler;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO);

    @Before
    public void setUp() {
        // Initialize a PlaybackHandler instance using a temporary directory
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        playbackHandler = new PlaybackHandler(tempDir);
    }

    @Test
    public void testStartStopRecording() {
        // Start recording
        playbackHandler.startRecording();
        assertTrue(playbackHandler.isRecording());

        // Stop recording
        playbackHandler.addRecording(); // this method stops the recording
        assertFalse(playbackHandler.isRecording());
    }

    @Test
    public void testSaveLoadRecordings() {
        // Start recording
        playbackHandler.startRecording();
        assertTrue(playbackHandler.isRecording());

        // Stop recording
        playbackHandler.addRecording(); // this method stops the recording
        assertFalse(playbackHandler.isRecording());

        // Save all recordings
        playbackHandler.saveAll();

        // Load all recordings
        playbackHandler.loadAll();

        ArrayList<float[]> recordings = playbackHandler.getRecordings();
        assertFalse(recordings.isEmpty());
    }

    @Test
    public void testPlayStopSelectedRecordings() {
        // Start recording
        playbackHandler.startRecording();
        assertTrue(playbackHandler.isRecording());

        // Stop recording
        playbackHandler.addRecording(); // this method stops the recording
        assertFalse(playbackHandler.isRecording());

        // Play selected recordings
        playbackHandler.playSelected();
        assertTrue(playbackHandler.isPlaying());

        // Stop selected recordings
        playbackHandler.stopSelected();
        assertFalse(playbackHandler.isPlaying());
    }

    @Test
    public void testDeleteRecording() {
        // Start recording
        playbackHandler.startRecording();
        assertTrue(playbackHandler.isRecording());

        // Stop recording
        playbackHandler.addRecording(); // this method stops the recording
        assertFalse(playbackHandler.isRecording());

        ArrayList<float[]> recordings = playbackHandler.getRecordings();
        assertFalse(recordings.isEmpty());

        // Delete recording
        playbackHandler.deleteRecording(recordings.get(0));
        assertTrue(playbackHandler.getRecordings().isEmpty());
    }

}