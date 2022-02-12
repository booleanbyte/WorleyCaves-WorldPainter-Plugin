package com.booleanbyte.wpplugin.worleycaves;

import com.booleanbyte.wpplugin.worleycaves.layers.WorleyCavesLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.plugins.AbstractPlugin;
import org.pepsoft.worldpainter.plugins.LayerProvider;

import java.util.List;

import static java.util.Collections.singletonList;
import static com.booleanbyte.wpplugin.worleycaves.Version.VERSION;

public class WorleyCavesWPPlugin extends AbstractPlugin implements LayerProvider {

    static final String NAME = "Worley's Caves WP Plugin";

    public WorleyCavesWPPlugin() {
        super(NAME, VERSION);
    }

    private static final List<Layer> LAYERS = singletonList(WorleyCavesLayer.INSTANCE);

    @Override
    public List<Layer> getLayers() {
        return LAYERS;
    }
}
