package com.booleanbyte.wpplugin.worleycaves.layers;

import org.pepsoft.worldpainter.layers.Layer;

import java.io.Serializable;

import static org.pepsoft.worldpainter.layers.Layer.DataSize.NIBBLE;

public class WorleyCavesLayer extends Layer {
    private static final long serialVersionUID = 1L;

    static final String ID = "com.booleanbyte.wpplugin.worleycaves.WorleyCavesLayer.v1";

    static final String NAME = "Worley Caves";

    static final String DESCRIPTION = "A cave generation layer for Worley's Caves";

    static final DataSize DATA_SIZE = NIBBLE;

    static final int PRIORITY = 20;

    // This needs to be last, otherwise the static fields are not yet initialised
    public static final WorleyCavesLayer INSTANCE = new WorleyCavesLayer();

    private WorleyCavesLayer() {
        super(ID, NAME, DESCRIPTION, DATA_SIZE, PRIORITY);
    }
}