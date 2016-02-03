package de.cak85.gala.launcher;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
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

	private ApplicationItem app;
	private BitmapDrawable image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		final String packageName = getIntent().getStringExtra("packageName");
		app = ApplicationManager.getInstance().getGame(packageName);

		setTitle(app.getTitle());
		final ImageView imageView = (ImageView) findViewById(R.id.details_image);
		image = new BitmapDrawable(getResources(),
				ApplicationManager.getInstance().getImage(this, app));
		if (image != null) {
			Drawable[] layers = new Drawable[2];
			layers[0] = app.getIcon();
			layers[1] = getTintedDrawable(DetailsActivity.this,
					image,
					getResources().getColor(R.color.colorPrimary));
			TransitionDrawable transition = new TransitionDrawable(layers);
			imageView.setImageDrawable(transition);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			transition.startTransition(500);
		}
		/*if (app.getDescription() != null) {*/
			TextView textView = (TextView) findViewById(R.id.details_content_text);
			textView.setText(Html.fromHtml(app.getDescription()));
			View progressbar = findViewById(R.id.details_content_progressbar);
			progressbar.setVisibility(View.GONE);
			textView.setVisibility(View.VISIBLE);
		/*} else {
			scrape(app);
		}*/
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

				f.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				f.setTextColor(getResources().getColor(android.R.color.white));
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
		if (image != null) {
			Drawable[] layers = new Drawable[2];
			layers[1] = app.getIcon();
			layers[0] = getTintedDrawable(DetailsActivity.this,
					image,
					getResources().getColor(R.color.colorPrimary));
			TransitionDrawable transition = new TransitionDrawable(layers);
			final ImageView image = (ImageView) findViewById(R.id.details_image);
			image.setImageDrawable(transition);
			image.setScaleType(ImageView.ScaleType.CENTER_CROP);
			transition.startTransition(100);
		}
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

	public static Drawable getTintedDrawableOfColorResId(@NonNull Context context,
	                                                     @NonNull Bitmap inputBitmap,
	                                                     @ColorRes int colorResId) {
		return getTintedDrawable(context, new BitmapDrawable(context.getResources(), inputBitmap),
				ContextCompat.getColor(context, colorResId));
	}

	public static Drawable getTintedDrawable(@NonNull Context context,
	                                         @NonNull Bitmap inputBitmap,
	                                         @ColorInt int color) {
		return getTintedDrawable(context, new BitmapDrawable(context.getResources(), inputBitmap),
				color);
	}

	public static Drawable getTintedDrawable(@NonNull Context context,
	                                         @NonNull Drawable inputDrawable,
	                                         @ColorInt int color) {
		Drawable wrapDrawable = DrawableCompat.wrap(inputDrawable);
		DrawableCompat.setTint(wrapDrawable, color);
		DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.MULTIPLY);
		return wrapDrawable;
	}
}
