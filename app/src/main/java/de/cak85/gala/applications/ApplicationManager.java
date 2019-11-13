package de.cak85.gala.applications;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.cak85.gala.R;
import de.cak85.gala.applications.database.DBHelper;

/**
 * Application manager is responsible application-wide for the applications.
 *
 * <br><br>
 * Created by ckuster on 29.01.2016.
 */
public class ApplicationManager {

	private static final String TAG = "Gala/ApplicationManager";

	private static final String PREFS_KEY = "chosen_games";

	private List<ApplicationItem> games = new ArrayList<>();

	private static ApplicationManager ourInstance = new ApplicationManager();

	@NonNull
	public static ApplicationManager getInstance() {
		return ourInstance;
	}

	private ApplicationManager() {
	}

	@NonNull
	public List<ApplicationItem> getGames() {
		return games;
	}

	@Nullable
	public ApplicationItem getGame(@Nullable String packageName) {
		for (ApplicationItem app : games) {
			if (app.getPackageName().equals(packageName)) {
				return app;
			}
		}
		return null;
	}

	public void load(@NonNull Context context) {
		if (!games.isEmpty()) {
			return;
		}
		Gson gson = new Gson();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String jsonPreferences = sharedPref.getString(PREFS_KEY, "");

		Type type = new TypeToken<List<ApplicationItem>>() {}.getType();
		try {
			List<ApplicationItem> tempList = gson.fromJson(jsonPreferences, type);
			if (tempList != null) {
				games.clear();
				games.addAll(tempList);
				Iterator<ApplicationItem> iterator = games.iterator();
				while (iterator.hasNext()) {
					ApplicationItem applicationItem = iterator.next();
					try {
						setIcon(context, applicationItem);
					} catch (PackageManager.NameNotFoundException e) {
						iterator.remove();
						Log.w(TAG, "could not find package " + applicationItem.getPackageName()
								+ ": deleted.");
					}
				}
			}
		} catch (JsonParseException e) {
			games = new ArrayList<>();
			e.printStackTrace();
		}
	}

	@Nullable
	public Bitmap getImage(@NonNull Context context, @NonNull ApplicationItem applicationItem,
	                       int width, int height) {
		try {
			ContextWrapper cw = new ContextWrapper(context);
			File directory = cw.getDir("images", Context.MODE_PRIVATE);
			File file = new File(directory, applicationItem.getPackageName() + ".png");

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file), null, options);

			// calculate dimensions
			int reqWidth = width;
			int reqHeight = height;
			if (height > 0 && height > width) {
				reqWidth = (int) (options.outWidth / (options.outHeight / ((float) reqHeight)));
			} else if (width > 0 && width > height) {
				reqHeight = (int) (options.outHeight / (options.outWidth / ((float) reqWidth)));
			} else {
				reqWidth = options.outWidth;
				reqHeight = options.outHeight;
			}

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, height);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
		} catch (FileNotFoundException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * from {@code https://developer.android.com/training/displaying-bitmaps/load-bitmap.html}
	 *
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	/**
	 * Sets the icon for the given application item.
	 *
	 * @param context The current application context
	 * @param applicationItem The application item
	 * @throws PackageManager.NameNotFoundException if the application item can not be found
	 * on the system
	 */
	private void setIcon(Context context, ApplicationItem applicationItem)
			throws PackageManager.NameNotFoundException {

		// Get the application's resources
		PackageManager pm = context.getPackageManager();
		final ApplicationInfo packageInfo = pm.getApplicationInfo(
				applicationItem.getPackageName(), PackageManager.GET_META_DATA);
        Resources res = pm.getResourcesForApplication(packageInfo);

		// Get a copy of the configuration, and set it to the desired resolution
        Configuration config = res.getConfiguration();
        Configuration originalConfig = new Configuration(config);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			config.densityDpi =  DisplayMetrics.DENSITY_XXXHIGH;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			config.densityDpi =  DisplayMetrics.DENSITY_XXHIGH;
		}

		// Update the configuration with the desired resolution
        DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(config, dm);

		// Grab the app icon
		applicationItem.setIcon(packageInfo.loadIcon(pm));

        // Set our configuration back to what it was
        res.updateConfiguration(originalConfig, dm);
	}

	public void save(@NonNull Context context) {
		Gson gson = new Gson();
		String jsonCurProduct = gson.toJson(games);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(PREFS_KEY, jsonCurProduct);
		editor.apply();
	}

	private void saveImage(Context context, ApplicationItem app, Bitmap image) {
		if (image == null) {
			return;
		}
		ContextWrapper cw = new ContextWrapper(context);
		File directory = cw.getDir("images", Context.MODE_PRIVATE);
		File file = new File(directory, app.getPackageName() + ".png");
		if (!file.exists()) {
			try {
				FileOutputStream out = new FileOutputStream(file);
				image.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				image.recycle();
			}
		}
	}

	public void getInstalledApplications(@NonNull Context context,
			@NonNull AsyncTaskListener<List<ApplicationItem>, Void> listener) {
		final InstalledGamesFinderTask asyncTask = new InstalledGamesFinderTask(context, listener);
		asyncTask.execute();
	}

    private static class InstalledGamesFinderTask
            extends AsyncTask<Void, Void, List<ApplicationItem>> {

		private final AsyncTaskListener<List<ApplicationItem>, Void> listener;
		private Context context;

		public Context getContext() {
			return context;
		}

		InstalledGamesFinderTask(Context context,
		                                AsyncTaskListener<List<ApplicationItem>, Void> listener) {
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected List<ApplicationItem> doInBackground(Void... params) {
			PackageManager pm = context.getPackageManager();
			List<ApplicationInfo> packages = pm
					.getInstalledApplications(PackageManager.GET_META_DATA);
			List<ApplicationItem> applicationItems = new ArrayList<>();
			for (ApplicationInfo packageInfo : packages) {
				if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null
						&& !pm.getLaunchIntentForPackage(packageInfo.packageName).equals("")) {
					applicationItems.add(new ApplicationItem(context, packageInfo, pm));
				}
			}
			return applicationItems;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listener.onPreExecute();
		}

		@Override
		protected void onPostExecute(List<ApplicationItem> apps) {
			super.onPostExecute(apps);
			listener.onPostExecute(apps);
		}
	}

	public void retrieveInfos(final @NonNull Context context, final int imageWidth,
			final @NonNull ApplicationItem applicationItem,
			final @Nullable AsyncTaskListener<List<ApplicationItem>, String> listener) {
		final RetrieveInfosTask asyncTask = new RetrieveInfosTask(context, imageWidth, false, listener);
		asyncTask.execute(applicationItem);
	}

	public void autoDiscoverGames(final @NonNull Context context, final int imageWidth,
			final @Nullable AsyncTaskListener<List<ApplicationItem>, String> listener) {
		// we start the getInstalledApplications(...) method, and listen for it to finish.
		// Then we start the scrape task to retrieve the games.
		Runnable r = new Runnable() {
			@Override
			public void run() {
				final ProgressDialog progressDialog = ProgressDialog.show(context,
						context.getString(R.string.autodiscover_dialog_title),
						context.getString(R.string.autodiscover_dialog_message));
				getInstalledApplications(context,
						new AsyncTaskListener<List<ApplicationItem>, Void>() {
					@Override
					public void onPreExecute() {
					}

					@Override
					public void onProgress(Void... progress) {
					}

					@Override
					public void onPostExecute(final List<ApplicationItem> apps) {
						final RetrieveInfosTask asyncTask = new RetrieveInfosTask(context, imageWidth,
								true,
								new AsyncTaskListener<List<ApplicationItem>, String>() {
									@Override
									public void onPreExecute() {
										if (listener != null)
											listener.onPreExecute();
									}

									@Override
									public void onProgress(String... progress) {
										progressDialog.setMessage(Html.fromHtml(context.getString(
												R.string.autodiscover_dialog_message_checking) +
												" <b>" + progress[0] + "</b>"));
										if (listener != null)
											listener.onProgress(progress);
									}

									@Override
									public void onPostExecute(List<ApplicationItem> param) {
                                        final List<ApplicationItem> games = getGames();

                                        games.clear();
										games.addAll(param);
										if (progressDialog != null && progressDialog.isShowing()) {
											progressDialog.dismiss();
										}
										if (listener != null)
											listener.onPostExecute(param);
									}
								});
                        asyncTask.execute(apps.toArray(new ApplicationItem[]{}));
					}
				});
			}
		};
		new Thread(r).run();
	}

	private class RetrieveInfosTask extends AsyncTask<ApplicationItem, String,
			List<ApplicationItem>> {

		private Context context;
		private int imageWidth;
		private AsyncTaskListener<List<ApplicationItem>, String> listener;
		private boolean scrapeOnlyGames;

		RetrieveInfosTask(Context context, int imageWidth, boolean scrapeOnlyGames,
		                         AsyncTaskListener<List<ApplicationItem>, String> listener) {
			this.context = context;
			this.imageWidth = imageWidth;
			this.listener = listener;
			this.scrapeOnlyGames = scrapeOnlyGames;
		}

		@Override
		protected List<ApplicationItem> doInBackground(ApplicationItem... apps) {
			List<ApplicationItem> games = new ArrayList<>();
			final DBHelper dbHelper = DBHelper.getInstance(context);
			Map<String, Boolean> categories = dbHelper.getCategories();
			System.out.println("categories from db:"
					+ Arrays.toString(categories.entrySet().toArray()));
			for (ApplicationItem app : apps) {
				publishProgress(app.getName());
				Bitmap image = null;
				String description = null;
				String title = null;
				final String packageName = app.getPackageName();
				boolean checkedCategory = categories.containsKey(packageName);
                boolean isGame = checkedCategory ? categories.get(packageName) : false;
				if (checkedCategory) {
					if (scrapeOnlyGames && !isGame) {
						continue;
					}
					image = getImage(context, app, 100, -1);
				}

                InputStream inputStream;
                try {
                    URL url = new URL(context.getString(R.string.scrapepage)
                            + getCleanPackageName(app));
                    URLConnection urlConnection = url.openConnection();
                    inputStream = urlConnection.getInputStream();
                } catch (IOException e) {
                    System.err.println("could not find " + packageName + ".");
                    continue;
                }
                try (BufferedReader bufferedReader =
                             new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (image == null
                                && line.contains("data-screenshot-item")) {
                            String imageSource = line.substring(
                                    line.indexOf("data-screenshot-item"));
                            imageSource = imageSource.substring(imageSource.indexOf("src=\"")
                                    + 5);
                            imageSource = imageSource.substring(0, imageSource.indexOf("="));
                            imageSource = (imageSource.contains("http") ? "" : "https:")
                                    + imageSource + "=w" + imageWidth;
                            InputStream in = new URL(imageSource).openStream();
                            image = BitmapFactory.decodeStream(in);
                        }
                        if (title == null && line.contains("main-title")) {
                            title = line.substring(line.indexOf("main-title"));
                            title = title.substring(title.indexOf(">") + 1,
                                    title.indexOf("</title>"));
                            title = StringEscapeUtils.unescapeHtml4(title);
                        }
                        if (description == null && line.contains("itemprop=\"description\"")) {
                            description =
                                    line.substring(line.indexOf("itemprop=\"description\""));
                            description = description.substring(description.indexOf("content"));
                            description = description.substring(description.indexOf("=") + 2);
                            description = StringEscapeUtils.unescapeHtml4(description);
                        }
                        if (!checkedCategory && line.contains("itemprop=\"genre\"")) {
                            int index = line.indexOf("itemprop=\"genre\"");
                            String category = line.substring(index);
                            category = category.substring(category.indexOf("href") + 6);
                            category = category.substring(0, category.indexOf("\""));
                            isGame = category.toLowerCase().contains("game");
                            dbHelper.saveCategory(packageName, isGame);
                            checkedCategory = true;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("An error occured for " + packageName + ": "
                            + e.getLocalizedMessage());
                    continue;
                }
				if (scrapeOnlyGames && !isGame) {
					continue;
				}
				if (description != null) {
					app.setTitle(title);
					app.setDescription(description);
					ApplicationManager.getInstance().saveImage(context, app, image);
					games.add(app);
				}
			}
			return games;
		}

		Throwable getCause(Throwable e) {
			Throwable cause;
			Throwable result = e;

			while(null != (cause = result.getCause())  && (result != cause) ) {
				result = cause;
			}
			return result;
		}

		private String getCleanPackageName(ApplicationItem app) {
			return app.getPackageName().replace("_humble", "");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (listener != null)
				listener.onProgress(values);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (listener != null)
				listener.onPreExecute();
		}

		@Override
		protected void onPostExecute(List<ApplicationItem> apps) {
			super.onPostExecute(apps);
			if (listener != null)
				listener.onPostExecute(apps);
		}
	}

}
