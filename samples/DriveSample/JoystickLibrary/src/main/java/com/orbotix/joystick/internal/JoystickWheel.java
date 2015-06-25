package com.orbotix.joystick.internal;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Wheel that draws itself to the JoystickView
 */
public class JoystickWheel extends Drawable {

    private final Point position = new Point();

    private final Paint paint = new Paint();

    private int radius = 75;

    private int alpha = 0x33;

    public JoystickWheel(){

        int blur = (this.radius / 10);
        blur = (blur < 1)?1:blur;

        this.paint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.INNER));
        this.paint.setStyle(Paint.Style.FILL);
        this.setColor(0xff000000);
    }

    /**
     * Sets the radius of this wheel, in pixels.
     * @param radius
     */
    public void setRadius(int radius){
        this.radius = radius;
    }

    /**
     * Sets the color of this joystick wheel to the specified coclor
     * @param color
     */
    public void setColor(int color){
        this.paint.setColor(color);
    }

    /**
     * Sets the position of the JoystickWheel to the specified position
     * @param position
     */
    public void setPosition(Point position){
        this.position.set(position.x, position.y);



        this.setBounds(new Rect(
                this.position.x - this.radius,
                this.position.y - this.radius,
                this.position.x + this.radius,
                this.position.y + this.radius
        ));
        

    }

    /**
     * Gets the center position of the puck wheel, as an Point.
     * @return a Point, containing the center point of the puck wheel
     */
    public Point getPosition(){
        return new Point(this.position);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(this.position.x, this.position.y, this.radius, this.paint);
    }

    @Override
    public void setAlpha(int i) {

        float a = Math.abs(i & 0xff);
        a = (a / 255);
        a = 0x33 * a;
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
