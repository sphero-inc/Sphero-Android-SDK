package com.orbotix.joystick.internal;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.orbotix.joystick.utilities.color.ColorTools;

/**
 * Puck that draws itself to the JoystickView
 */
public class JoystickPuck extends Drawable {

    private int radius = 25;
    private final Point position = new Point();

    //PuckParts
    private final Shadow shadow;
    private final Surface surface;
    private final Shading shading;

    /**
     * Constructs a new Joystick Puck
     */
    public JoystickPuck(){

        this.shadow = new Shadow();
        this.surface = new Surface();
        this.shading = new Shading();

        this.setShadowColor(0xff000000);
        this.setSurfaceColor(0xffcccccc);
        this.setShadingColor(0xff000000);
    }

    /**
     * Sets the radius of the JoystickPuck, in pixels
     * @param radius
     */
    public void setRadius(int radius){

        if(radius < 1){
            throw new IllegalArgumentException("Radius must be greater than 0.");
        }

        this.radius = radius;

        this.shadow.setRadius(this.radius);
        this.surface.setRadius(this.radius);
        this.shading.setRadius(this.radius - (this.radius / 8));
    }

    /**
     * Sets the color of the puck's shadow to the specified color
     * @param color
     */
    public void setShadowColor(int color){
        this.shadow.setColor(color);
    }

    /**
     * Sets the color of the puck's surface to the specified colors. Is a gradient that goes from one color to the
     * other.
     *
     * @param color
     */
    public void setSurfaceColor(int color){

        this.surface.setColor(color);
    }

    /**
     * Sets the color of the shading on the puck's surface.
     *
     * @param color
     */
    public void setShadingColor(int color){
        this.shading.setColor(color);
    }

    /**
     * Sets the position of this puck to the specified position
     * @param position
     */
    public void setPosition(Point position){
        this.position.set(position.x, position.y);

        this.shadow.setPosition(position);
        this.surface.setPosition(position);
        this.shading.setPosition(position);


        this.setBounds(new Rect(
                this.position.x - this.radius,
                this.position.y - this.radius,
                this.position.x + this.radius,
                this.position.y + this.radius
        ));
        
    }

    /**
     * Gets a copy of this puck's position.
     * @return An Index containing the position of this puck.
     */
    public Point getPosition(){
        return new Point(this.position);
    }

    @Override
    public void draw(Canvas canvas) {

        this.shadow.draw(canvas);
        this.surface.draw(canvas);
        this.shading.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        this.shadow.setAlpha(i);
        this.surface.setAlpha(i);
        this.shading.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        this.shadow.setColorFilter(colorFilter);
        this.surface.setColorFilter(colorFilter);
        this.shading.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    /**
     * A part of the puck
     */
    private interface PuckPart {

        /**
         * Draws this part to the provided canvas.
         * @param canvas
         */
        public void draw(Canvas canvas);

        /**
         * Set the color of this PuckPart to the provided color, as an int.
         * @param color
         */
        public void setColor(int color);

        /**
         * Set this PuckPart to the provided position.
         * @param position
         */
        public void setPosition(Point position);

        /**
         * Sets this PuckPart's radius to the provided radius.
         * @param radius
         */
        public void setRadius(int radius);
    }

    /**
     * The Puck's shadow
     */
    private class Shadow extends Drawable implements PuckPart {

        private final Paint paint = new Paint();
        private int radius = 25;
        private final Point position = new Point();

        private int alpha = 0x55;

        public Shadow(){

            //Set blur to 1/5th the radius
            final int blur = (this.radius / 5);
            this.paint.setMaskFilter(new BlurMaskFilter(((blur < 1)?1:blur), BlurMaskFilter.Blur.OUTER));
            paint.setStyle(Paint.Style.FILL);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            this.setColor(0xff000000);
        }

        @Override
        public void setColor(int color){

            this.paint.setColor(color);
            final int i = (color >> 24);
            this.setAlpha(i);
        }

        @Override
        public void setPosition(Point position){
            this.position.set(position.x, position.y);
        }

        @Override
        public void setRadius(int radius) {

            if(radius < 1){
            throw new IllegalArgumentException("Radius must be greater than 0.");
            }

            this.radius = radius;
        }

        @Override
        public void draw(Canvas canvas){

            canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
        }

        @Override
        public void setAlpha(int i) {

            final float a = 0x55 * (Math.abs(i & 0xff) / 255f);
            this.paint.setAlpha((int)a);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /**
     * The Puck's surface
     */
    private class Surface extends Drawable implements PuckPart {

        private final Paint paint = new Paint();
        private final Point position = new Point();
        private final Point gradient_pos_1 = new Point();
        private final Point gradient_pos_2 = new Point();
        private int radius = 25;

        private int color_0 = 0xff888888;
        private int color_1 = 0xffdddddd;

        public Surface(){

            paint.setStyle(Paint.Style.FILL);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            paint.setAntiAlias(true);
        }

        @Override
        public void draw(Canvas canvas) {

            LinearGradient gradient =
                    new LinearGradient(
                            this.gradient_pos_1.x,
                            this.gradient_pos_1.y,
                            this.gradient_pos_2.x,
                            this.gradient_pos_2.y,
                            this.color_0,
                            this.color_1,
                            Shader.TileMode.MIRROR
                    );

            this.paint.setShader(gradient);

            canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
        }

        @Override
        public void setRadius(int radius) {

            if(radius < 1){
            throw new IllegalArgumentException("Radius must be greater than 0.");
            }

            this.radius = radius;
        }

        @Override
        public void setAlpha(int i) {

            this.paint.setAlpha(i);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setColor(int color) {

            this.color_0 = ColorTools.ColorSum(color, 0x00333333, true);
            this.color_1 = color;
        }

        @Override
        public void setPosition(Point position) {
            this.position.set(position.x, position.y);

            this.gradient_pos_1.set(this.position.x, this.position.y + this.radius);
            this.gradient_pos_2.set(this.position.x, this.position.y - this.radius);
        }
    }

    /**
     * The shading of the Puck's surface
     */
    public class Shading extends Drawable implements PuckPart {

        private final Point position = new Point();
        private final Paint paint = new Paint();

        private int alpha = 0x44;
        private int color_0 = 0xffffffff;
        private int color_1 = 0xff000000;

        private int radius = 22;

        public Shading(){

            paint.setStyle(Paint.Style.FILL);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            paint.setAntiAlias(true);
        }

        @Override
        public void setRadius(int radius) {

            if(radius < 1){
            throw new IllegalArgumentException("Radius must be greater than 0.");
            }

            this.radius = radius;
        }

        @Override
        public void draw(Canvas canvas) {

            RadialGradient gradient = new RadialGradient(
                    this.position.x,
                    this.position.y,
                    this.radius,
                    this.color_0,
                    this.color_1,
                    Shader.TileMode.MIRROR
            );

            this.paint.setShader(gradient);

            canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
        }

        @Override
        public void setAlpha(int i) {


            float a = Math.abs(i & 0xff);
            a = a / 255f;
            a = 0x44 * a;
            this.alpha = (int)a;
            this.color_1 = (this.color_1 & 0xffffff) + (this.alpha << 24);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setColor(int color) {

            this.color_0 = (color & 0xffffff);
            final int a = (color >> 24);
            this.setAlpha(a);
            this.color_1 = (this.color_0) +(this.alpha << 24);
        }

        @Override
        public void setPosition(Point position) {
            this.position.set(position.x, position.y);
        }
    }


}
