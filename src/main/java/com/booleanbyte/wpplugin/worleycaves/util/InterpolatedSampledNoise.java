package com.booleanbyte.wpplugin.worleycaves.util;

import org.pepsoft.minecraft.Chunk;

public class InterpolatedSampledNoise {

    private WorleyUtil worley;
    private FastNoise displacementNoise;

    private int maxCaveHeight = 256;
    private int minCaveHeight = 1;
    private float noiseCutoff = -0.18f;
    private float warpAmplifier = 8.0f;
    private float easeInDepth = 15.0f;
    private float yCompression = 2.0f;
    private float xzCompression = 1.0f;
    private float surfaceCutoff = -0.081f;

    private float[][][] noiseSamples;

    public InterpolatedSampledNoise(WorleyUtil worley, FastNoise displacementNoise) {
        this.worley = worley;
        this.displacementNoise = displacementNoise;
    }

    public void sampleNoise(Chunk chunk, int maxSurfaceHeight) {
        int originalMaxHeight = 128;
        noiseSamples = new float[5][130][5];

        for (int x = 0; x < 5; x++) {
            int realX = x * 4 + chunk.getxPos() * 16;
            for (int z = 0; z < 5; z++) {
                int realZ = z * 4 + chunk.getzPos() * 16;
                boolean columnHasCaveFlag = false;

                // loop from top down for y values so we can adjust noise above current y later on
                for (int y = 128; y >= 0; y--) {
                    float realY = y * 2;
                    if (realY > maxSurfaceHeight || realY > maxCaveHeight || realY < minCaveHeight) {
                        // if outside of valid cave range set noise value below normal minimum of -1.0
                        noiseSamples[x][y][z] = -1.1F;
                    } else {
                        // Experiment making the cave system more chaotic the more you descend
                        float dispAmp = (float) (warpAmplifier * ((originalMaxHeight - y) / (originalMaxHeight * 0.85)));

                        float xDisp = 0f;
                        float yDisp = 0f;
                        float zDisp = 0f;

                        xDisp = displacementNoise.GetNoise(realX, realZ) * dispAmp;
                        yDisp = displacementNoise.GetNoise(realX, realZ + 67.0f) * dispAmp;
                        zDisp = displacementNoise.GetNoise(realX, realZ + 149.0f) * dispAmp;

                        // doubling the y frequency to get some more caves
                        float noise = worley.SingleCellular3Edge(realX * xzCompression + xDisp, realY * yCompression + yDisp, realZ * xzCompression + zDisp);
                        noiseSamples[x][y][z] = noise;

                        if (noise > noiseCutoff) {
                            columnHasCaveFlag = true;
                            // if noise is below cutoff, adjust values of neighbors helps prevent caves fracturing during interpolation
                            if (x > 0)
                                noiseSamples[x - 1][y][z] = (noise * 0.2f) + (noiseSamples[x - 1][y][z] * 0.8f);
                            if (z > 0)
                                noiseSamples[x][y][z - 1] = (noise * 0.2f) + (noiseSamples[x][y][z - 1] * 0.8f);

                            // more heavily adjust y above 'air block' noise values to give players more head room
                            if (y < 128) {
                                float noiseAbove = noiseSamples[x][y + 1][z];
                                if (noise > noiseAbove)
                                    noiseSamples[x][y + 1][z] = (noise * 0.8F) + (noiseAbove * 0.2F);
                                if (y < 127) {
                                    float noiseTwoAbove = noiseSamples[x][y + 2][z];
                                    if (noise > noiseTwoAbove)
                                        noiseSamples[x][y + 2][z] = (noise * 0.35F) + (noiseTwoAbove * 0.65F);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    public float getInterpolation(int x, int y, int z) {
        int ix = x / 4;
        int iy = y / 2;
        int iz = z / 4;

        float cx = (x - ix * 4) / 4.0f;
        float cy = (y - iy * 2) / 2.0f;
        float cz = (z - iz * 4) / 4.0f;

        // Grab the 8 sample points needed from the noise values
        float x0y0z0 = noiseSamples[ix][iy][iz];
        float x0y0z1 = noiseSamples[ix][iy][iz + 1];
        float x1y0z0 = noiseSamples[ix + 1][iy][iz];
        float x1y0z1 = noiseSamples[ix + 1][iy][iz + 1];
        float x0y1z0 = noiseSamples[ix][iy + 1][iz];
        float x0y1z1 = noiseSamples[ix][iy + 1][iz + 1];
        float x1y1z0 = noiseSamples[ix + 1][iy + 1][iz];
        float x1y1z1 = noiseSamples[ix + 1][iy + 1][iz + 1];

        // Interpolate
        float x00 = MathHelper.lerp(x0y0z0, x1y0z0, cx);
        float x01 = MathHelper.lerp(x0y0z1, x1y0z1, cx);
        float x10 = MathHelper.lerp(x0y1z0, x1y1z0, cx);
        float x11 = MathHelper.lerp(x0y1z1, x1y1z1, cx);
        float xy0 = MathHelper.lerp(x00, x10, cy);
        float xy1 = MathHelper.lerp(x01, x11, cy);
        float xyz = MathHelper.lerp(xy0, xy1, cz);

        return xyz;
    }
}
