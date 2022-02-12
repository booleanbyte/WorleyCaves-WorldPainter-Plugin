package com.booleanbyte.wpplugin.worleycaves.layers.exporters;

import com.booleanbyte.wpplugin.worleycaves.layers.WorleyCavesLayer;
import com.booleanbyte.wpplugin.worleycaves.util.FastNoise;
import com.booleanbyte.wpplugin.worleycaves.util.InterpolatedSampledNoise;
import com.booleanbyte.wpplugin.worleycaves.util.MathHelper;
import com.booleanbyte.wpplugin.worleycaves.util.WorleyUtil;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import static org.pepsoft.minecraft.Material.AIR;

public class WorleyCavesLayerExporter extends AbstractLayerExporter<WorleyCavesLayer> implements FirstPassLayerExporter {

    private WorleyUtil worleyF1divF3;
    private FastNoise displacementNoisePerlin;

    private int maxCaveHeight = 256;
    private int minCaveHeight = 1;
    private float noiseCutoff = -0.18f;
    private float warpAmplifier = 8.0f;
    private float easeInDepth = 15.0f;
    private float yCompression = 2.0f;
    private float xzCompression = 1.0f;
    private float surfaceCutoff = -0.081f;
    private int lavaDepth = 10;
    private boolean additionalWaterChecks = false;

    public WorleyCavesLayerExporter() {
        super(WorleyCavesLayer.INSTANCE, new CavernsSettings());

//        maxCaveHeight = WorleyConfig.maxCaveHeight;
//        minCaveHeight = WorleyConfig.minCaveHeight;
//        noiseCutoff = (float)WorleyConfig.noiseCutoffValue;
//        warpAmplifier = (float)WorleyConfig.warpAmplifier;
//        easeInDepth = (float)WorleyConfig.easeInDepth;
//        yCompression = (float)WorleyConfig.verticalCompressionMultiplier;
//        xzCompression = (float)WorleyConfig.horizonalCompressionMultiplier;
//        surfaceCutoff = (float)WorleyConfig.surfaceCutoffValue;
//        lavaDepth = WorleyConfig.lavaDepth;
    }

    @Override
    public void render(Dimension dimension, Tile tile, Chunk chunk, Platform platform) {
        int checked = 0;
        int carved = 0;
        final CavernsSettings settings = (CavernsSettings) getSettings();
        final long seed = dimension.getSeed();

        if (worleyF1divF3 == null) {
            worleyF1divF3 = new WorleyUtil((int) seed);
            worleyF1divF3.SetFrequency(0.016f);
        }
        if (displacementNoisePerlin == null) {
            displacementNoisePerlin = new FastNoise((int) seed);
            displacementNoisePerlin.SetNoiseType(FastNoise.NoiseType.Perlin);
            displacementNoisePerlin.SetFrequency(0.05f);
        }

        int chunkMaxHeight = getMaxSurfaceHeight(tile, chunk);

        InterpolatedSampledNoise samples = new InterpolatedSampledNoise(worleyF1divF3, displacementNoisePerlin);
        samples.sampleNoise(chunk, chunkMaxHeight + 1);

        final int xOffset = (chunk.getxPos() & 7) << 4;
        final int zOffset = (chunk.getzPos() & 7) << 4;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int localX = xOffset + x;
                final int localZ = zOffset + z;

                if (tile.getBitLayerValue(org.pepsoft.worldpainter.layers.Void.INSTANCE, localX, localZ)) {
                    continue;
                }

                final int cavernsValue = tile.getLayerValue(WorleyCavesLayer.INSTANCE, localX, localZ);
                if (cavernsValue > 0) {
                    final int terrainheight = tile.getIntHeight(localX, localZ);

                    int depth = 0;
                    for (int y = terrainheight; y >= 0; y--) {
                        float noiseValue = samples.getInterpolation(x, y, z);

                        float adjustedNoiseCutoff = noiseCutoff;// + cutoffAdjuster;
                        if (depth < easeInDepth) {
                            // higher threshold at surface, normal threshold below easeInDepth
                            adjustedNoiseCutoff = (float) MathHelper.clampedLerp(noiseCutoff, surfaceCutoff, (easeInDepth - (float) depth) / easeInDepth);

                        }
                        // increase cutoff as we get closer to the minCaveHeight so it's not all flat floors
                        if (y < (minCaveHeight + 5)) {
                            adjustedNoiseCutoff += ((minCaveHeight + 5) - y) * 0.05;
                        }

                        checked++;
                        if (noiseValue > adjustedNoiseCutoff) {
                            chunk.setMaterial(x, y, z, AIR);
                            carved++;
                        }

                        depth++;
                    }
                }
            }
        }
    }

    private int getMaxSurfaceHeight(Tile tile, Chunk chunk) {
        final int xOffset = (chunk.getxPos() & 7) << 4;
        final int zOffset = (chunk.getzPos() & 7) << 4;

        int maxY = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int localX = xOffset + x, localY = zOffset + z;
                maxY = Math.max(maxY, tile.getIntHeight(localX, localY));
            }
        }
        return maxY;
    }

//    private void digBlock(Chunk chunk, Biome biome, int x, int y, int z, boolean foundTop, Material state, Material blockStateUp)
//    {
//        BlockState top = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
//        BlockState filler = biome.getGenerationSettings().getSurfaceBuilderConfig().getUnderMaterial();
//
//        if (this.canReplaceBlock(state, blockStateUp) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock())
//        {
//            if (blockPos.getY() <= lavaDepth)
//            {
//                chunk.setBlockState(blockPos, lavaBlock, false);
//            } else
//            {
//                chunk.setBlockState(blockPos, AIR, false);
//
//                if (foundTop && chunk.getBlockState(blockPos.below()).getBlock() == filler.getBlock())
//                {
//                    chunk.setMaterial(x, y-1, z, top);
//                }
//
//                // replace floating sand with sandstone
//                if (blockStateUp == SAND)
//                {
//                    chunk.setMaterial(x, y+1, z, SANDSTONE);
//                } else if (blockStateUp == RED_SAND)
//                {
//                    chunk.setBlockState(blockPos.above(), RED_SANDSTONE, false);
//                }
//            }
//        }
//    }

    public static class CavernsSettings implements ExporterSettings {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isApplyEverywhere() {
            return false;
        }

        @Override
        public WorleyCavesLayer getLayer() {
            return WorleyCavesLayer.INSTANCE;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CavernsSettings other = (CavernsSettings) obj;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        public CavernsSettings clone() {
            try {
                return (CavernsSettings) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
