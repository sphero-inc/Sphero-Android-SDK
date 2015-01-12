package com.orbotix.joystick.utilities.color;

import android.graphics.ColorMatrixColorFilter;

/**
 * Tools for working with integer color values
 */
public class ColorTools {
	
	public static int ClippedColorPart(int color)
	{
		if(color < 0)
		{
			return 0;
		}else if(color > 0xFF)
		{
			return 0xFF;
		}
		return color;
	}

    /**
     * Returns the sum of the two provided colors.
     *
     * @param color
     * @param color2
     * @return
     */
	public static int ColorSum(int color, int color2)
	{
		return ColorSum(color, color2, false);
	}

    /**
     * Returns the sum of one color and another. If indicated by the subtract param, will return the
     * difference of the first color and the second instead, with a floor on each color part of 0, and
     * a ceiling of 255.
     *
     * @param color
     * @param color2
     * @param subtract
     * @return
     */
	public static int ColorSum(int color, int color2, boolean subtract)
	{
		final int a = (color >>> 24);
		final int r = (color >>  16) & 0xFF;
		final int g = (color >>   8) & 0xFF;
		final int b = (color)        & 0xFF;
		
		final int a2 = (subtract)? -(color2 >>> 24)          : (color2 >>> 24);
		final int r2 = (subtract)? -((color2 >>  16) & 0xFF) : ((color2 >>  16) & 0xFF);
		final int g2 = (subtract)? -((color2 >>   8) & 0xFF) : ((color2 >>   8) & 0xFF);
		final int b2 = (subtract)? -((color2)        & 0xFF) : ((color2)        & 0xFF);
		
		return (ClippedColorPart(a + a2) << 24) +
				(ClippedColorPart(r + r2) << 16) +
				(ClippedColorPart(g + g2) << 8) +
				(ClippedColorPart(b + b2));
	}

    /**
     * Returns the a color created by an average between two colors. For instance, if passed
     * black (0xff000000) and white (0xffffffff), will result in grey (0xff888888). Good for easy
     * color mixing.
     *
     * @param color
     * @param color2
     * @return
     */
    public static int average(int color, int color2){

        final int[] parts1 = getArgb(color);
        final int[] parts2 = getArgb(color2);

        for(int i = 0;i<parts1.length;i++){

            parts1[i] = (int)((float)(parts1[i] + parts2[i]) / 2f);
        }

        return getColorFromArgb(parts1);
    }

    /**
     * Gets an ARGB int array from a provided color
     *
     * @param color
     * @return
     */
    public static int[] getArgb(int color){
        final int a = (color >>> 24);
		final int r = (color >>  16) & 0xFF;
		final int g = (color >>   8) & 0xFF;
		final int b = (color)        & 0xFF;

        return new int[]{ClippedColorPart(a), ClippedColorPart(r), ClippedColorPart(g), ClippedColorPart(b)};
    }

    /**
     * Gets an int color from an int[4] containing argb values.
     * @param argb
     * @return an int containing the result color
     */
    public static int getColorFromArgb(int[] argb){

        if(argb.length != 4){
            throw new IllegalArgumentException("ARGB int array must have a length of 4.");
        }

        return (ClippedColorPart(argb[0]) << 24) +
				(ClippedColorPart(argb[1]) << 16) +
				(ClippedColorPart(argb[2]) << 8) +
				(ClippedColorPart(argb[3]));
    }

    /**
     *
     * @param amount
     * @return A ColorMatrixColorFilter containing a translation matrix of the provided color
     */
	public static ColorMatrixColorFilter getTranslationColorFilter(int amount)
	{
		final int a = (amount >>> 24);
		final int r = (amount >>  16) & 0xFF;
		final int g = (amount >>   8) & 0xFF;
		final int b = (amount)        & 0xFF;
		
		final float[] matrix = {
				1, 0, 0, 0, r,
				0, 1, 0, 0, g,
				0, 0, 1, 0, b,
				0, 0, 0, 1, a
		};
		
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		return filter;
	}

    /**
     *
     * @param factor
     * @return a ColorMatrixColorFilter containing a mScale contrast filter of the provided factor
     */
	public static ColorMatrixColorFilter getScaleContrastFilter(float factor)
	{
		final float scale = factor + 1f;
		final float[] matrix = {
				scale, 0, 0, 0, 0,
				0, scale, 0, 0, 0,
				0, 0, scale, 0, 0,
				0, 0, 0, 1, 0
		};
		
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		return filter;
	}

    /**
     *
     * @param factor
     * @return a ColorMatrixColorFilter containing a contrast filter of the provided factor
     */
	public static ColorMatrixColorFilter getContrastFilter(float factor)
	{
		final float scale = factor + 1f;
		final float translation = (-.5f * scale + .5f) * 255f;
		final float[] matrix = {
				scale, 0, 0, 0, translation,
				0, scale, 0, 0, translation,
				0, 0, scale, 0, translation,
				0, 0, 0, 1, 0
		};
		
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		return filter;
	}
}
