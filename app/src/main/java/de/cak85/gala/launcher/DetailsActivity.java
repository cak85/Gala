package de.cak85.gala.launcher;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;
import de.cak85.gala.applications.ApplicationManager;

public class DetailsActivity extends AppCompatActivity {

	public static final int TRANSITION_MILLIS = 800;
	public static final int TRANSITION_REVERSE_MILLIS = 250;
	private ApplicationItem app;
	private TransitionDrawable transitionDrawable;

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
		BitmapDrawable image = new BitmapDrawable(getResources(),
				 ApplicationManager.getInstance().getImage(this, app));
		if (image.getBitmap() != null) {
			Drawable[] layers = new Drawable[2];
			layers[0] = getDrawable(app.getIcon(), iconBackgroundColor, image, this);
			layers[1] = getTintedDrawable(image,
					ContextCompat.getColor(DetailsActivity.this, R.color.colorPrimary), this);
			transitionDrawable = new TransitionDrawable(layers);
		} else {
			Drawable[] layers = new Drawable[2];
			layers[0] = getDrawable(app.getIcon(), iconBackgroundColor, null, this);
			layers[1] = getTintedDrawable(app.getIcon(),
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

		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
		fab.requestFocus();
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
