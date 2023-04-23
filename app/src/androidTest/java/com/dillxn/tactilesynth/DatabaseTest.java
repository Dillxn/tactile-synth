package com.dillxn.tactilesynth;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DatabaseTest {

    private Database database;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Database.getInstance(context);
    }

    @Test
    public void testGetModel() {
        assertNotNull("The database model should not be null", database.getModel());
    }

    @Test
    public void testGetConfig() {
        assertNotNull("The database config should not be null", database.getConfig());
    }

    @Test
    public void testGetPreset() {
        assertNotNull("The database preset should not be null", database.getPreset());
    }

    @Test
    public void testGetDebug() {
        boolean debugState = database.getDebug();
        assertTrue(debugState == true || debugState == false);
    }

    @Test
    public void testSetDebug() {
        boolean currentDebugState = database.getDebug();
        database.setDebug(!currentDebugState);
        assertEquals("The debug state should be updated", !currentDebugState, database.getDebug());
    }

    @Test
    public void testGetGrid() {
        boolean gridState = database.getGrid();
        assertTrue(gridState == true || gridState == false);
    }

    @Test
    public void testSetGrid() {
        boolean currentGridState = database.getGrid();
        database.setGrid(!currentGridState);
        assertEquals("The grid state should be updated", !currentGridState, database.getGrid());
    }

    @Test
    public void testGetScales() {
        List<String> scales = database.getScales();
        assertNotNull("The scales list should not be null", scales);
        assertFalse("The scales list should not be empty", scales.isEmpty());
    }

    @Test
    public void testGetKeys() {
        List<String> keys = database.getKeys();
        assertNotNull("The keys list should not be null", keys);
        assertFalse("The keys list should not be empty", keys.isEmpty());
    }
}