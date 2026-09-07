package com.bentza.sna.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the 2.1.3 setName fix: control characters (tab,
 * newline, CR) previously survived setName because the sanitizing replace()
 * result was discarded; a node name containing a tab would corrupt the
 * tab-separated .agn format on save.
 */
public class ActorNameSanitizationTest
    {
    @Test
    public void tabIsReplacedWithUnderscore()
        {
        Actor actor = new Actor("default", 2);
        actor.setName("A\tB");
        assertEquals("A_B", actor.getName());
        }

    @Test
    public void newlineAndCarriageReturnAreReplaced()
        {
        Actor actor = new Actor("default", 2);
        actor.setName("line1\nline2\rline3");
        assertEquals("line1_line2_line3", actor.getName());
        }

    @Test
    public void normalNamesAreKept()
        {
        Actor actor = new Actor("default", 2);
        actor.setName("Sociologist 1");
        assertEquals("Sociologist 1", actor.getName());
        }

    @Test
    public void nullNameIsTolerated()
        {
        Actor actor = new Actor("default", 2);
        actor.setName(null);
        // the application renders a null name as the empty string
        assertEquals("", actor.getName());
        }
    }