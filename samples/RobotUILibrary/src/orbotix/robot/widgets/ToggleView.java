package orbotix.robot.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import orbotix.robot.app.R;

/**
 * Widget that provides a toggle control interface. Allows the user to select between two choices.
 *
 */
public class ToggleView extends View {
	/** Constant to indicate the toggle is at the first position (top/left) */
	public static final int POSITION_1 = 1;
	/** Constant to indicate the toggle is at the second position (bottom/right) */
	public static final int POSITION_2 = 2;
	/** Constant for setting the toggle in a horizontal orientation. Default */
	public static final int ORIENTATION_HORIZONTAL = 1;
	/** Constant for setting the toggle in a vertical orientation. */
	public static final int ORIENTATION_VERTICAL = 2;

	private Bitmap		 position1Background;
	private Bitmap       position2Background;

	private Bitmap toggleButtonImage;

	private Rect position1Frame;
	private Rect position2Frame;
	private Rect toggleDrawFrame;
	private Point backgroundPosition;

	private int position;
	private int orientation;
	private int translation = 0;

	private OnPositionChangeListener positionChangeListener = null;

	/**
	 * Interface for a callback listener that informs it of changes in the toggle position.
	 *
	 */
	public interface OnPositionChangeListener {
		public void onPositonChange(int position);
	}

	/**
	 * Constructor that sets up the resources. Called when the view is specified in a layout file.
	 * @param context The context for the view.
	 * @param attrs The attribute set from the layout file.
	 */
	public ToggleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray type_array = context.obtainStyledAttributes(attrs, R.styleable.ToggleView);

		int toggle_res_id = type_array.getResourceId(R.styleable.ToggleView_toggleButton, R.drawable.toggle_button);
		toggleButtonImage = BitmapFactory.decodeResource(getResources(), toggle_res_id);

		int pos1_res_id = type_array.getResourceId(R.styleable.ToggleView_position1Background, R.drawable.toggle_slider);
		position1Background = BitmapFactory.decodeResource(getResources(), pos1_res_id);
		int pos2_res_id = type_array.getResourceId(R.styleable.ToggleView_position2Background, R.drawable.toggle_slider);
		position2Background = BitmapFactory.decodeResource(getResources(), pos2_res_id);

		orientation = type_array.getResourceId(R.styleable.ToggleView_orientation, ORIENTATION_HORIZONTAL);
		position = type_array.getResourceId(R.styleable.ToggleView_initialPosition, POSITION_1);
	}

	/**
	 * Method to set the OnPositionChangeListener object.
	 * @param listener The listener object.
	 */
	public void setOnPositionListener(OnPositionChangeListener listener) {
		positionChangeListener = listener;
	}

	/**
	 * Method for changing the the position of the toggle.
	 * @param newPosition The position constant which can only be POSITOIN_1 OR POSITION_2.
	 */
	public void setPosition(int newPosition) {
        position = newPosition;
		if(position == POSITION_1) {
			toggleDrawFrame = position1Frame;
		} else {
			toggleDrawFrame = position2Frame;
		}
		translation = 0;
		invalidate();
	}

	 @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 // work out the largest dimensions
		 int margin_x = 0;
		 int margin_y = 0;
		 int width;
		 if (position1Background.getWidth() > toggleButtonImage.getWidth()) {
			 width = position1Background.getWidth();
		 } else {
			 width = toggleButtonImage.getWidth();
			 margin_y = width - position1Background.getWidth();
		 }
		 int height;
		 if (position1Background.getHeight() > toggleButtonImage.getHeight()) {
			 height = position1Background.getHeight();
		 } else {
			 height = toggleButtonImage.getHeight();
			 margin_x = height - position1Background.getHeight();
		 }
		 setMeasuredDimension(width + margin_x, height + margin_y);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int height = bottom - top;
		int width = right - left;

		// work out background position
		int pos_x = 0;
		if (position1Background.getWidth() < width) {
			pos_x = (width - position1Background.getWidth())/2;
		}
		int pos_y = 0;
		if (position2Background.getHeight() < height) {
			pos_y = (height - position1Background.getHeight())/2;
		}
		backgroundPosition = new Point(pos_x, pos_y);

		// work out rectangular positions for the toggle button
		int margin_x = 0;
		if (toggleButtonImage.getWidth() < width && orientation == ORIENTATION_VERTICAL) {
			margin_x = (width - toggleButtonImage.getWidth())/2;
		}
		int margin_y = 0;
		if (toggleButtonImage.getHeight() < height && orientation == ORIENTATION_HORIZONTAL) {
			margin_y = (height - toggleButtonImage.getHeight())/2;
		}
		position1Frame = new Rect(margin_x, margin_y, margin_x + toggleButtonImage.getWidth(),
				margin_y + toggleButtonImage.getHeight());

		if (orientation == ORIENTATION_HORIZONTAL) {
			int toggle_right = width - margin_x;
			position2Frame = new Rect(toggle_right - toggleButtonImage.getWidth(), margin_y, toggle_right,
					margin_y + toggleButtonImage.getHeight());
		} else {
			int toggle_bottom = height - margin_y;
			position2Frame = new Rect(margin_x, toggle_bottom - toggleButtonImage.getHeight(),
					margin_x + toggleButtonImage.getWidth(), toggle_bottom);
		}
		if (position == POSITION_1) {
			toggleDrawFrame = position1Frame;
		} else {
			toggleDrawFrame = position2Frame;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (position == POSITION_1) {
			canvas.drawBitmap(position1Background, backgroundPosition.x, backgroundPosition.y, null);
		} else {
			canvas.drawBitmap(position2Background, backgroundPosition.x, backgroundPosition.y, null);
		}

		float button_left;
		float button_top;
		if (orientation == ORIENTATION_HORIZONTAL) {
			button_left = toggleDrawFrame.left + translation;
			button_top = toggleDrawFrame.top;
		} else {
			button_left = toggleDrawFrame.left;
			button_top = toggleDrawFrame.top + translation;
		}
		canvas.drawBitmap(toggleButtonImage, button_left, button_top, null);
	}

	/**
	 * Handles touch events to move the toggle.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			translation = 0;
			if (toggleDrawFrame.contains((int)event.getX(), (int)event.getY())) {
				invalidate();
				return true;
			}
			return false;

		case MotionEvent.ACTION_MOVE:
			int button_length;
			int slider_length;
			float button_pos;
			if (orientation == ORIENTATION_HORIZONTAL) {
				button_length = toggleButtonImage.getWidth();
				slider_length = getWidth();
				button_pos = event.getX();
			} else {
				button_length = toggleButtonImage.getHeight();
				slider_length = getHeight();
				button_pos = event.getY();
			}
			int sticky_zone = (int)(1.2 * button_length);

			if (position == POSITION_1) {
				if (button_pos < sticky_zone) {
					translation = 0;
				} else if (button_pos < (slider_length - sticky_zone)) {
					translation = (int)button_pos;
				} else {
					// consider at position
					position = POSITION_2;
					toggleDrawFrame = position2Frame;
					translation = 0;
					if (positionChangeListener != null) {
						positionChangeListener.onPositonChange(position);
					}
				}
			} else {
				if (button_pos > slider_length - sticky_zone) {
					translation = 0;
				} else if (button_pos > sticky_zone) {
					translation = (int)button_pos - slider_length;
				} else {
					// consider at position
					position = POSITION_1;
					toggleDrawFrame = position1Frame;
					translation = 0;
					if (positionChangeListener != null) {
						positionChangeListener.onPositonChange(position);
					}
				}

			}
			invalidate();
			return true;

		case MotionEvent.ACTION_UP:
			translation = 0;
			invalidate();
			return true;

		default:
			return super.onTouchEvent(event);
		}
	}
}
