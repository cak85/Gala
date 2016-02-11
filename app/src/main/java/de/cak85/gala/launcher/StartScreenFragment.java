package de.cak85.gala.launcher;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class StartScreenFragment extends Fragment {

	private ApplicationItem gameItem = null;
	private int backgroundColor = 0;
	private int textColor = 0;
	private int x = 20;
	private int y = 20;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView =inflater.inflate(R.layout.startscreen, container, false);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
				                           int oldRight, int oldBottom) {
					v.removeOnLayoutChangeListener(this);

					// get the hypotenuse so the radius is from one corner to the other
					int radius = (int) Math.hypot(right, bottom);

					Animator reveal = ViewAnimationUtils.createCircularReveal(v, x, y, 0, radius);

					reveal.setInterpolator(new AccelerateDecelerateInterpolator());
					reveal.setDuration(500);
					reveal.start();
				}
			});
		}
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		assert getView() != null;
		getView().setBackgroundColor(backgroundColor);
		setupGameItem(getView());

		// start the app after 2 s
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					Intent launchIntent = getActivity().getPackageManager()
							.getLaunchIntentForPackage(gameItem.getPackageName());
					getActivity().finish();
					startActivity(launchIntent);
				} catch (NullPointerException e) {}
			}
		}, 2000);
	}

	private void setupGameItem(View view) {
		ImageView image = (ImageView) view.findViewById(R.id.startscreen_imageView);
		image.setImageDrawable(gameItem.getIcon());
		TextView text = (TextView) view.findViewById(R.id.startscreen_textView);
		text.setText(gameItem.getName());
		text.setTextColor(textColor);
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		if (getView() != null) {
			getView().setBackgroundColor(backgroundColor);
		}
	}

	public void setGameItem(ApplicationItem gameItem) {
		this.gameItem = gameItem;
		if (getView() != null) {
			setupGameItem(getView());
		}
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
