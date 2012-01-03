package orbotix.robot.widgets;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;
import orbotix.robot.app.R;

/**
 * A popover widget that can be filled with a custom layout. When shown, the popover will be displayed out of the anchor
 * view given in the constructor. The popover is completely interactive while shown and can be dismissed by either
 * touching outside its bounds or by calling {@link #dismiss()}.
 *
 * @author Lorensius. W. T
 */
public class CustomPopover extends CustomPopupWindow {
	private final ImageView mArrowUp;
	private final ImageView mArrowDown;

	protected static final int ANIM_GROW_FROM_LEFT = 1;
	protected static final int ANIM_GROW_FROM_RIGHT = 2;
	protected static final int ANIM_GROW_FROM_CENTER = 3;
	protected static final int ANIM_REFLECT = 4;
	protected static final int ANIM_AUTO = 5;
    protected static final int ANIM_APPEAR = 6;

	private int animStyle;
	private ViewGroup mTrack;
	private ScrollView scroller;

    private View mContent;
    private View mPopover;

    /**
     * Creates a popover that will be filled with content and will appear from anchor.
     * @param contentLayoutId the content of the popover (i.e. the popover's custom layout)
     * @param anchor the view from which this popover will appear
     */
	public CustomPopover(int contentLayoutId, View anchor) {
		super(anchor);
        LayoutInflater inflater = LayoutInflater.from(anchor.getContext());
        mPopover                = inflater.inflate(R.layout.popover, null);
        setContentView(mPopover);
        mArrowDown 	            = (ImageView)mPopover.findViewById(R.id.arrow_down);
        mArrowUp 	            = (ImageView)mPopover.findViewById(R.id.arrow_up);

        scroller                = (ScrollView)mPopover.findViewById(R.id.scroller);
        animStyle	            = ANIM_APPEAR;

        mContent                = inflater.inflate(contentLayoutId, scroller);
	}

	/**
	 * Shows the popover with the current animation scheme.
	 */
	public void show () {
		preShow();

		int xPos, yPos;

		int[] location 		= new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1]
		                	+ anchor.getHeight());

		mPopover.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mPopover.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootHeight 		= mPopover.getMeasuredHeight();
		int rootWidth		= mPopover.getMeasuredWidth();

		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();

		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left - (rootWidth-anchor.getWidth());
		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
		}

		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		boolean onTop		= dyTop > dyBottom;

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos 			= 15;
				LayoutParams l 	= scroller.getLayoutParams();
				l.height		= dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom) {
				LayoutParams l 	= scroller.getLayoutParams();
				l.height		= dyBottom;
			}
		}

		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX()-xPos);

		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

		window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	/**
	 * Set animation style
	 *
	 * @param screenWidth screen width
     * @param requestedX distance from left edge
     * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
     * 		  and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

        switch (animStyle) {
            case ANIM_GROW_FROM_LEFT:
                window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                break;

            case ANIM_REFLECT:
                window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
                break;

            case ANIM_AUTO:
                if (arrowPos <= screenWidth/4) {
                    window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                } else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
                    window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                } else {
                    window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                }

                break;

            case ANIM_APPEAR:
                window.setAnimationStyle(R.style.Animations_PopUpMenu_Appear);
                break;
		}
	}

	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();

        param.leftMargin = requestedX - arrowWidth / 2;

        hideArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * Used to retrieve the content of the popover if needed.
     * @return the content of the popover as set in {@link #CustomPopover(int, android.view.View)}
     */
    public View getContent() {
        return mContent;
    }
}