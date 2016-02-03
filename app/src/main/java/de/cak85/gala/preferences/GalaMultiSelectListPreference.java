package de.cak85.gala.preferences;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

/**
 * Created by ckuster on 19.01.2016.
 */
public class GalaMultiSelectListPreference extends MultiSelectListPreference {

	public GalaMultiSelectListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GalaMultiSelectListPreference(Context context) {
		super(context);
	}

	public interface OnClickListener {
		boolean onClick();
	}

	private OnClickListener mOnClicListner;

	@Override
	protected void onClick(){
		boolean consumed = false;
		if (mOnClicListner != null)
			consumed = mOnClicListner.onClick();
		if (!consumed)
			super.onClick();
	}

	public void setOnClickListner(OnClickListener l) {
		mOnClicListner = l;
	}
}
