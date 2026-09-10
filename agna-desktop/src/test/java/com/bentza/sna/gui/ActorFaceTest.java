package com.bentza.sna.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.bentza.sna.net.Actor;

/**
 * 2.1.3: nodes must always end up with a resolvable face — a stored "-"
 * or unresolvable source falls back to the bundled red bullet default.
 */
public class ActorFaceTest
    {
    @Test
    public void defaultFaceIsTheRedBullet()
        {
        MainFrame.setDefaultNodeFaceSource("-");
        String face = MainFrame.getDefaultNodeFaceSource();
        assertNotEquals("-", face);
        assertNotNull(face);
        assertEquals(true, face.contains("Red Bullet.gif"));
        }

    @Test
    public void storedMinusFaceFallsBackToDefault()
        {
        MainFrame.setDefaultNodeFaceSource("-");
        Actor actor = new Actor("fallback node");
        actor.setFace("-");
        assertNotEquals("-", actor.getFaceSource());
        assertNotNull(actor.getFace());
        }

    @Test
    public void freshActorHasTheDefaultFace()
        {
        MainFrame.setDefaultNodeFaceSource("-");
        Actor actor = new Actor("fresh node");
        assertNotEquals("-", actor.getFaceSource());
        assertNotNull(actor.getFace());
        }
    @Test
    public void defaultFaceSelfHealsWhenNotInitialized() throws Exception
        {
        // regression: a settings file without a "Default Node Face" line
        // leaves the field null - the getter must still yield the red bullet
        java.lang.reflect.Field f = com.bentza.sna.core.AppRuntime.class
                .getDeclaredField("default_node_face_source");
        f.setAccessible(true);
        f.set(null, null);
        String face = MainFrame.getDefaultNodeFaceSource();
        assertNotEquals("-", face);
        assertTrue(face.contains("Red Bullet.gif"));
        assertEquals(true, new java.io.File(face).exists());
        }
    }
