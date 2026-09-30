/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;


import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.data.DummyElement;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CircuitTest extends TestCase {

    private static Circuit createCircuit() {
        Circuit c = new Circuit();

        // add a ROM
        final DataField data = new DataField(4);
        data.setData(0, 0xffff0000ffff0000L);
        data.setData(1, 0x8fff0000ffff0000L);
        final VisualElement rom = new VisualElement(ROM.DESCRIPTION.getName())
                .setAttribute(Keys.DATA, data);
        c.add(rom);

        // add an input
        final VisualElement in = new VisualElement(In.DESCRIPTION.getName())
                .setAttribute(Keys.INPUT_DEFAULT, new InValue(0x8fff0000ffff0000L));
        c.add(in);

        return c;
    }

    public void test64Bit() throws IOException {
        Circuit c = createCircuit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.save(baos);

        // check ROM
        c = Circuit.loadCircuit(new ByteArrayInputStream(baos.toByteArray()), new ShapeFactory(new ElementLibrary()));
        VisualElement rom = c.getElements().get(0);
        DataField d = rom.getElementAttributes().get(Keys.DATA);
        assertEquals(0xffff0000ffff0000L, d.getDataWord(0));
        assertEquals(0x8fff0000ffff0000L, d.getDataWord(1));

        // check input
        VisualElement in = c.getElements().get(1);
        assertEquals(0x8fff0000ffff0000L, in.getElementAttributes().get(Keys.INPUT_DEFAULT).getValue());
    }

    public void testFindElementsIncludesTextDescription() throws IOException {
        ShapeFactory shapeFactory = new ShapeFactory(new ElementLibrary());
        Circuit c = new Circuit();
        VisualElement text = new VisualElement(DummyElement.TEXTDESCRIPTION.getName())
                .setShapeFactory(shapeFactory)
                .setAttribute(Keys.DESCRIPTION, "LearningNoteAlpha")
                .setPos(new Vector(40, 60));
        c.add(text);

        ArrayList<VisualElement> found = c.findElements("learningnote");
        assertEquals(1, found.size());
        assertEquals(DummyElement.TEXTDESCRIPTION.getName(), found.get(0).getElementName());
        assertEquals("LearningNoteAlpha", found.get(0).getElementAttributes().get(Keys.DESCRIPTION));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.save(baos);
        Circuit loaded = Circuit.loadCircuit(new ByteArrayInputStream(baos.toByteArray()), shapeFactory);
        found = loaded.findElements("LearningNoteAlpha");
        assertEquals(1, found.size());
        assertEquals(new Vector(40, 60), found.get(0).getPos());
    }

}
