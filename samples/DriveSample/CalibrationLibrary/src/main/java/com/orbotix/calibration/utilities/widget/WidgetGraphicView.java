package com.orbotix.calibration.utilities.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A View that can have many WidgetParts added to it, and will draw them each invalidation cycle.
 * <p/>
 * <p/>
 * Author: Adam Williams
 * Date: 11/22/11
 * Time: 10:28 AM
 */
public class WidgetGraphicView extends View implements WidgetGraphic {

    private final List<WidgetGraphicPart> mWidgetGraphicParts = new ArrayList<WidgetGraphicPart>();

    public WidgetGraphicView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public void addWidgetPart(WidgetGraphicPart part) {
        mWidgetGraphicParts.add(part);
    }

    @Override
    public void showAllWidgetParts() {
        for (WidgetGraphicPart w : mWidgetGraphicParts) {
            w.show();
        }
        invalidate();
    }

    @Override
    public void hideAllWidgetsParts() {
        for (WidgetGraphicPart w : mWidgetGraphicParts) {
            w.hide();
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {

        Rect dirty_area = null;

        for (WidgetGraphicPart w : mWidgetGraphicParts) {

            if (dirty_area == null) {
                dirty_area = w.draw(canvas);
            } else {
                dirty_area.union(w.draw(canvas));
            }
        }

        if (dirty_area != null) {
            invalidate(dirty_area);
        }
    }


}
