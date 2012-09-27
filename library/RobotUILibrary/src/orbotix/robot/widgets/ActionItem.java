package orbotix.robot.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import orbotix.robot.app.R;

/**
 * Action item, displayed as menu with icon and text.
 *
 * @author Lorensius. W. L. T
 *
 */
public class ActionItem {
	private Drawable mIcon;
	private String mTitle;
	private OnClickListener mListener;
    private View mView;
    private Context mContext;
    private boolean somethingHasChanged;
    private int mLayoutId;

	/**
	 * Constructor
	 */
	public ActionItem(Context context) {
        mContext = context;
        mLayoutId = R.layout.action_item;
    }

	/**
	 * Constructor
	 *
	 * @param icon {@link Drawable} action icon
	 */
	public ActionItem(Context context, Drawable icon) {
        mContext = context;
        mLayoutId = R.layout.action_item;
		setIcon(icon);
	}

    public void setLayoutId(int layoutId) {
        mLayoutId = layoutId;
    }

	/**
	 * Set action title
	 *
	 * @param title action title
	 */
	public void setTitle(String title) {
		mTitle = title;
        somethingHasChanged = true;
	}

	/**
	 * Get action title
	 *
	 * @return action title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Set action icon
	 *
	 * @param icon {@link Drawable} action icon
	 */
	public void setIcon(Drawable icon) {
		mIcon = icon;
        somethingHasChanged = true;
	}

	/**
	 * Get action icon
	 * @return  {@link Drawable} action icon
	 */
	public Drawable getIcon() {
		return mIcon;
	}

	/**
	 * Set on click listener
	 *
	 * @param listener on click listener {@link View.OnClickListener}
	 */
	public void setOnClickListener(OnClickListener listener) {
		mListener = listener;
        somethingHasChanged = true;
	}

	/**
	 * Get on click listener
	 *
	 * @return on click listener {@link View.OnClickListener}
	 */
	public OnClickListener getListener() {
		return mListener;
	}

    /**
     * Returns the complete view for this ActionItem. The view will only contain the elements present
     * at the time of the call (e.g. if there is only an icon associated with this ActionItem then
     * the View returned will only contain a clickable ImageView).
     *
     * @return the view to be added to a {@link QuickAction}
     */
    public View getView() {
        if (mView != null && !somethingHasChanged) {
            return mView;
        } else {
            // either something has changed in this ActionItem or we have never created the view
            // to return. Either way we need to make a view that contains all of the objects, set
            // it clickable and return it.
            mView = LayoutInflater.from(mContext).inflate(mLayoutId, null);
            addIcon();
            addTitle();
            addOnClickListener();
            somethingHasChanged = false;
            return mView;
        }
    }

    private void addIcon() {
        if (mIcon != null) {
            ImageView iconView = (ImageView)mView.findViewById(R.id.icon);
            iconView.setVisibility(View.VISIBLE);
            iconView.setImageDrawable(mIcon);
        }
    }

    private void addTitle() {
        if (mTitle != null) {
            TextView titleView = (TextView)mView.findViewById(R.id.title);
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(mTitle);
        }
    }

    private void addOnClickListener() {
        if (mListener != null) {
            mView.setOnClickListener(mListener);
        }
    }
}