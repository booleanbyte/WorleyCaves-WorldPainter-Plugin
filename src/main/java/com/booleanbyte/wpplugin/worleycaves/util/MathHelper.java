package com.booleanbyte.wpplugin.worleycaves.util;

public class MathHelper {

    public static float clamp(float v, float min, float max) {
        return Math.min(Math.max(v, min), max);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float clampedLerp(float a, float b, float t) {
        return lerp(a, b, clamp(t, 0, 1));
    }
}
