/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.draw.graphics.GraphicMinMax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A scroll panel used by the circuit component
 */
public class CircuitScrollPanel extends JPanel {
    private static final int BORDER = SIZE * 10;
    private final CircuitComponent circuitComponent;
    private final JScrollBar horizontal;
    private final JScrollBar vertical;
    private GraphicMinMax graphicMinMax;
    private AffineTransform transform;
    private boolean updatingBars;

    /**
     * Creates a new instance
     *
     * @param circuitComponent the circuit component to use
     */
    public CircuitScrollPanel(CircuitComponent circuitComponent) {
        super(new BorderLayout());
        horizontal = new JScrollBar(JScrollBar.HORIZONTAL);
        vertical = new JScrollBar(JScrollBar.VERTICAL);

        this.circuitComponent = circuitComponent;
        add(circuitComponent, BorderLayout.CENTER);
        add(horizontal, BorderLayout.SOUTH);
        add(vertical, BorderLayout.EAST);

        horizontal.addAdjustmentListener(adjustmentEvent -> {
            if (!updatingBars && transform != null)
                circuitComponent.translateCircuitToX(-adjustmentEvent.getValue() * transform.getScaleX());
        });
        vertical.addAdjustmentListener(adjustmentEvent -> {
            if (!updatingBars && transform != null)
                circuitComponent.translateCircuitToY(-adjustmentEvent.getValue() * transform.getScaleY());
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (transform != null)
                    updateBars();
            }
        });

        circuitComponent.setCircuitScrollPanel(this);
    }

    private GraphicMinMax getCircuitSize() {
        if (graphicMinMax == null) {
            graphicMinMax = new GraphicMinMax();
            circuitComponent.getCircuit().drawTo(graphicMinMax);
        }
        return graphicMinMax;
    }

    void sizeChanged() {
        graphicMinMax = null;
        if (transform != null)
            updateBars();
    }

    /**
     * Updates the transformation
     *
     * @param transform the transform
     */
    void transformChanged(AffineTransform transform) {
        this.transform = transform;
        updateBars();
    }

    private void updateBars() {
        GraphicMinMax gr = getCircuitSize();

        if (gr.getMin() != null && gr.getMax() != null && transform != null) {
            Point2D min = new Point2D.Float();
            Point2D max = new Point2D.Float();
            try {
                // Use the circuit canvas size, not this panel (which includes the bars).
                transform.inverseTransform(new Point2D.Float(0, 0), min);
                transform.inverseTransform(
                        new Point2D.Float(circuitComponent.getWidth(), circuitComponent.getHeight()), max);
                setValues(horizontal, min.getX(), max.getX(), gr.getMin().x, gr.getMax().x);
                setValues(vertical, min.getY(), max.getY(), gr.getMin().y, gr.getMax().y);
            } catch (NoninvertibleTransformException e) {
                // can not happen! Scaling is never zero!
                e.printStackTrace();
            }
        }
        horizontal.setVisible(true);
        vertical.setVisible(true);
    }

    private void setValues(JScrollBar bar, double viewMin, double viewMax, int circuitMin, int circuitMax) {
        int border = Math.max(BORDER, (circuitMax - circuitMin) / 10);
        circuitMin -= border;
        circuitMax += border;
        int extent = (int) (viewMax - viewMin);
        updatingBars = true;
        try {
            bar.setValues((int) viewMin, extent, circuitMin, circuitMax);
        } finally {
            updatingBars = false;
        }
    }

    /**
     * @return the width of the bars
     */
    int getBarWidth() {
        return vertical.getPreferredSize().width;
    }
}
