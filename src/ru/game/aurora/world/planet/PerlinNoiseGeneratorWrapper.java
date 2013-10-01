/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 08.07.13
 * Time: 12:29
 */

package ru.game.aurora.world.planet;


import libnoiseforjava.NoiseGen;
import libnoiseforjava.exception.ExceptionInvalidParam;
import libnoiseforjava.module.Perlin;
import libnoiseforjava.util.NoiseMap;
import libnoiseforjava.util.NoiseMapBuilderPlane;

public class PerlinNoiseGeneratorWrapper
{
    private Perlin p = new Perlin();

    private int seedIncrementer = 0;

    public NoiseMap buildNoiseMap(int noiseWidth, int noiseHeight)
    {
        NoiseMap noiseMap;
        try {
            noiseMap = new NoiseMap(noiseWidth, noiseHeight);

            p.setSeed((int) System.currentTimeMillis() + (seedIncrementer++));
            p.setOctaveCount(3);
            p.setNoiseQuality(NoiseGen.NoiseQuality.QUALITY_FAST);
            NoiseMapBuilderPlane noiseMapBuilderPlane;

            noiseMapBuilderPlane = new NoiseMapBuilderPlane();
            noiseMapBuilderPlane.setSourceModule(p);
            noiseMapBuilderPlane.setDestNoiseMap(noiseMap);
            noiseMapBuilderPlane.setDestSize(noiseWidth, noiseHeight);
            noiseMapBuilderPlane.setBounds(0.0, 4.0, 0.0, 4.0);
            noiseMapBuilderPlane.build();

            return noiseMap;

        } catch (ExceptionInvalidParam exceptionInvalidParam) {
            throw new IllegalStateException(exceptionInvalidParam);
        }
    }

    public Perlin getP() {
        return p;
    }
}
