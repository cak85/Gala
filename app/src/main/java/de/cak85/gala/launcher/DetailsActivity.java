package de.cak85.gala.launcher;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.transition.Transition;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;
import de.cak85.gala.applications.ApplicationManager;

public class DetailsActivity extends AppCompatActivity {

	public static final int TRANSITION_MILLIS = 800;
	public static final int TRANSITION_REVERSE_MILLIS = 250;
	private ApplicationItem app;
	private TransitionDrawable transitionDrawable;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		final String packageName = getIntent().getStringExtra(GameListActivity.INTENT_PACKAGE_NAME);
		final int iconBackgroundColor = getIntent().getIntExtra(
				GameListActivity.INTENT_ICON_BACKGROUND_COLOR, Color.LTGRAY);
		app = ApplicationManager.getInstance().getGame(packageName);

		setTitle(app.getName());
		final ImageView imageView = (ImageView) findViewById(R.id.details_image);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		BitmapDrawable image = new BitmapDrawable(getResources(),
				 ApplicationManager.getInstance().getImage(
						 this, app, size.x, -1));
		final SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		boolean showDownloadedImages = sharedPreferences.getBoolean(
				getString(R.string.pref_key_user_interface_show_downloaded_images), false);
		final Drawable icon = app.getIcon();
		if (image.getBitmap() != null) {
			Drawable[] layers = new Drawable[2];
			layers[0] = showDownloadedImages ?
					image : getDrawable(icon, iconBackgroundColor, image, this);
			layers[1] = getTintedDrawable(image,
					ContextCompat.getColor(DetailsActivity.this, R.color.colorPrimary), this);
			transitionDrawable = new TransitionDrawable(layers);
		} else {
			Drawable[] layers = new Drawable[2];
			layers[0] = getDrawable(icon, iconBackgroundColor, null, this);
			layers[1] = getTintedDrawable(icon,
					ContextCompat.getColor(DetailsActivity.this, R.color.colorPrimary), this);
			transitionDrawable = new TransitionDrawable(layers);
		}
		imageView.setImageDrawable(transitionDrawable);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		transitionDrawable.startTransition(TRANSITION_MILLIS);

		TextView titleView = (TextView) findViewById(R.id.details_content_title);
		titleView.setText(app.getTitle());
		TextView textView = (TextView) findViewById(R.id.details_content_text);
		if (Build.VERSION.SDK_INT >= 24) {
			textView.setText(Html.fromHtml(app.getDescription(), Html.FROM_HTML_MODE_LEGACY));
		} else {
			textView.setText(Html.fromHtml(app.getDescription()));
		}
		View progressbar = findViewById(R.id.details_content_progressbar);
		progressbar.setVisibility(View.GONE);
		textView.setVisibility(View.VISIBLE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				StartScreenFragment f = new StartScreenFragment();

				f.setBackgroundColor(ContextCompat.getColor(DetailsActivity.this,
						R.color.colorPrimary));
				f.setTextColor(ContextCompat.getColor(DetailsActivity.this, android.R.color.white));
				f.setGameItem(app);
				f.setCoordinates((int) fab.getX() + fab.getWidth() / 2,
						(int) fab.getY() + fab.getHeight() / 2);
				ft.replace(android.R.id.content, f, "tag");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.commit();
			}
		});

		//noinspection WrongConstant
		setRequestedOrientation(Integer.valueOf(sharedPreferences.getString(
				getString(R.string.pref_key_user_interface_orientation),
				String.valueOf(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE))));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
				@Override
				public void onTransitionStart(Transition transition) {
				}
				@Override
				public void onTransitionEnd(Transition transition) {
					fab.show();
					fab.requestFocus();
				}
				@Override
				public void onTransitionCancel(Transition transition) {
				}
				@Override
				public void onTransitionPause(Transition transition) {
				}
				@Override
				public void onTransitionResume(Transition transition) {
				}
			});
		} else {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					fab.show();
					fab.requestFocus();
				}
			}, 500);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fab.setVisibility(View.GONE);
			}
		});
		transitionDrawable.reverseTransition(TRANSITION_REVERSE_MILLIS);
	}

	/*@SuppressLint("SetJavaScriptEnabled")
	private void scrape(final ApplicationItem app) {
		String packageName = app.getPackageName();
		class MyJavaScriptInterface {
			@JavascriptInterface
			@SuppressWarnings("unused")
			public void processHTML(String html) {
				if (html.contains("itemprop=\"description\"")) {
					String description =
							html.substring(html.indexOf("itemprop=\"description\""));
					description = description.substring(description.indexOf("div"));
					description = description.substring(description.indexOf(">") + 1);
					description = description.substring(0, description.indexOf("</div>"));
					app.setDescription(description);
					ApplicationManager.getInstance().save(DetailsActivity.this);
				} else {
					app.setDescription("Not found in Play Store!");
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView = (TextView) findViewById(R.id.details_content_text);
						textView.setText(Html.fromHtml(app.getDescription()));
						View progressbar = findViewById(R.id.details_content_progressbar);
						progressbar.setVisibility(View.GONE);
						textView.setVisibility(View.VISIBLE);
					}
				});
			}
		}

		final WebView browser = (WebView) findViewById(R.id.details_browser);
		if (Build.VERSION.SDK_INT >= 11){
			browser.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		browser.getSettings().setJavaScriptEnabled(true);
		browser.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

		browser.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				browser.loadUrl("javascript:HTMLOUT.processHTML(document.documentElement.outerHTML);");
			}
		});

		final String url = getString(R.string.scrapepage) + packageName;
		System.out.println("url = " + url);
		browser.loadUrl(url);
	}*/

	public static Drawable getTintedDrawable(@NonNull Drawable inputDrawable,
											 @ColorInt int color, @NonNull Context context) {
		int width = inputDrawable.getMinimumWidth();
		int height = inputDrawable.getMinimumHeight();
		Drawable d = new ColorDrawable(color);
		final Rect bounds = new Rect(0, 0, width, height);
		d.setBounds(bounds);
		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		d.draw(c);
		c.drawBitmap(((BitmapDrawable) inputDrawable).getBitmap(), bounds, bounds, null);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
		//DrawableCompat.setTintMode(bitmapDrawable, PorterDuff.Mode.MULTIPLY);
		//DrawableCompat.setTint(bitmapDrawable, color);
		bitmapDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		return bitmapDrawable;
	}

	private static Drawable getDrawable(@NonNull Drawable icon,
	                             @ColorInt int iconBackgroundColor,
	                             BitmapDrawable image,
	                             @NonNull Context context) {
		int width = image != null ? image.getMinimumWidth() : icon.getMinimumWidth();
		int height = image != null ? image.getMinimumHeight() : icon.getMinimumHeight();
		Drawable d = new ColorDrawable(iconBackgroundColor);
		d.setBounds(new Rect(0, 0, width, height));
		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		d.draw(c);
		float iconScaleFactor = ((float) width) / ((float) icon.getMinimumWidth());
		int scaledIconHeight = (int) (icon.getMinimumHeight() * iconScaleFactor);
		int diff = (height - scaledIconHeight) / 2;
		c.drawBitmap(((BitmapDrawable) icon).getBitmap(), icon.getBounds(),
				new Rect(0, diff, width, diff + scaledIconHeight), null);
		return new BitmapDrawable(context.getResources(), bitmap);
	}
}
