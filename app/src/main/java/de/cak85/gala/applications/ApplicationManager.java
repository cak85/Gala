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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.cak85.gala.R;

/**
 * Application manager is responsible application-wide for the applications.
 *
 * <br><br>
 * Created by ckuster on 29.01.2016.
 */
public class ApplicationManager {

	private static final String PREFS_KEY = "chosen_games";

	private List<ApplicationItem> games = new ArrayList<>();

	private static ApplicationManager ourInstance = new ApplicationManager();

	public static ApplicationManager getInstance() {
		return ourInstance;
	}

	private ApplicationManager() {
	}

	public List<ApplicationItem> getGames() {
		return games;
	}

	public ApplicationItem getGame(String packageName) {
		for (ApplicationItem app : games) {
			if (app.getPackageName().equals(packageName)) {
				return app;
			}
		}
		return null;
	}

	public void load(Context context) {
		Gson gson = new Gson();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String jsonPreferences = sharedPref.getString(PREFS_KEY, "");

		Type type = new TypeToken<List<ApplicationItem>>() {}.getType();
		try {
			games = gson.fromJson(jsonPreferences, type);
			if (games == null) {
				games = new ArrayList<>();
			} else {
				for (ApplicationItem applicationItem : games) {
					setIcon(context, applicationItem);
				}
			}
		} catch (JsonParseException e) {
			games = new ArrayList<>();
			e.printStackTrace();
		}
	}

	public Bitmap getImage(Context context, ApplicationItem applicationItem) {
		try {
			ContextWrapper cw = new ContextWrapper(context);
			File directory = cw.getDir("images", Context.MODE_PRIVATE);
			File file = new File(directory, applicationItem.getPackageName() + ".png");
			return BitmapFactory.decodeStream(new FileInputStream(file));
		} catch (FileNotFoundException | OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setIcon(Context context, ApplicationItem applicationItem) {
		// Get the application's resources
		Resources res = null;
		Configuration originalConfig = null;
		DisplayMetrics dm = null;
		PackageManager pm = context.getPackageManager();
		try {
			final ApplicationInfo packageInfo = pm.getApplicationInfo(
					applicationItem.getPackageName(), PackageManager.GET_META_DATA);
			res = pm.getResourcesForApplication(packageInfo);
			// Get a copy of the configuration, and set it to the desired resolution
			Configuration config = res.getConfiguration();
			originalConfig = new Configuration(config);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				config.densityDpi =  DisplayMetrics.DENSITY_XXXHIGH;
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				config.densityDpi =  DisplayMetrics.DENSITY_XXHIGH;
			}

			// Update the configuration with the desired resolution
			dm = res.getDisplayMetrics();
			res.updateConfiguration(config, dm);

			// Grab the app icon
			applicationItem.setIcon(packageInfo.loadIcon(pm));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (originalConfig != null) {
				// Set our configuration back to what it was
				res.updateConfiguration(originalConfig, dm);
			}
		}
	}

	public void save(Context context) {
		Gson gson = new Gson();
		String jsonCurProduct = gson.toJson(games);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(PREFS_KEY, jsonCurProduct);
		editor.apply();
	}

	private Uri saveImage(Context context, ApplicationItem app, Bitmap image) {
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
		return Uri.fromFile(file);
	}

	public void getInstalledApplications(Context context,
		    AsyncTaskListener<List<ApplicationItem>, Void> listener) {
		final InstalledGamesFinderTask asyncTask = new InstalledGamesFinderTask(context, listener);
		asyncTask.execute();
	}

	private class InstalledGamesFinderTask extends AsyncTask<Void, Void, List<ApplicationItem>> {

		private final AsyncTaskListener<List<ApplicationItem>, Void> listener;
		private Context context;

		public Context getContext() {
			return context;
		}

		public InstalledGamesFinderTask(Context context,
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

	public void scrapeInfos(final Context context,
	                        final int imageWidth,
	                        final ApplicationItem applicationItem,
	                        final AsyncTaskListener<List<ApplicationItem>, String> listener) {
		final RetrieveInfosTask asyncTask = new RetrieveInfosTask(context, imageWidth, false, listener);
		asyncTask.execute(applicationItem);
	}

	public void autoDiscoverGames(final Context context,
	                        final int imageWidth,
	                        final AsyncTaskListener<List<ApplicationItem>, String> listener) {
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
										games.clear();
										games.addAll(param);
										progressDialog.dismiss();
										if (listener != null)
											listener.onPostExecute(param);
									}
								});
						asyncTask.execute(apps.toArray(new ApplicationItem[apps.size()]));
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

		public RetrieveInfosTask(Context context, int imageWidth, boolean scrapeOnlyGames,
		                         AsyncTaskListener<List<ApplicationItem>, String> listener) {
			this.context = context;
			this.imageWidth = imageWidth;
			this.listener = listener;
			this.scrapeOnlyGames = scrapeOnlyGames;
		}

		@Override
		protected List<ApplicationItem> doInBackground(ApplicationItem... apps) {
			List<ApplicationItem> games = new ArrayList<>();

			outerloop:
			for (ApplicationItem app : apps) {
				publishProgress(app.getTitle());
				Bitmap image = null;
				String description = null;
				try {
					URL url = new URL(context.getString(R.string.scrapepage)
							+ getCleanPackageName(app));
					URLConnection urlConnection = url.openConnection();
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(urlConnection.getInputStream()));
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						if (line.contains("data-expand-to=\"full-screenshot-0\"")) {
							String imageSource = line.substring(
									line.indexOf("data-expand-to=\"full-screenshot-0\""));
							imageSource = imageSource.substring(imageSource.indexOf("src=\"") + 5);
							imageSource = imageSource.substring(0, imageSource.indexOf("="));
							imageSource = "https:" + imageSource + "=w" + imageWidth;
							InputStream in = new URL(imageSource).openStream();
							image = BitmapFactory.decodeStream(in);
						}
						if (line.contains("itemprop=\"description\"")) {
							description =
									line.substring(line.indexOf("itemprop=\"description\""));
							description = description.substring(description.indexOf("div"));
							description = description.substring(description.indexOf(">") + 1);
							description = description.substring(0, description.indexOf("</div>"));
						}
						if (line.contains("class=\"document-subtitle category\"")) {
							int index = line.indexOf("class=\"document-subtitle category\"");
							if (index > -1) {
								String category = line.substring(index);
								category = category.substring(category.indexOf("href") + 6);
								category = category.substring(0, category.indexOf("\""));
								if (scrapeOnlyGames && !category.toLowerCase().contains("game")) {
									continue outerloop;
								}
							}
						}
					}
					bufferedReader.close();
				}
				catch(Exception e) {
					System.err.println(e.getMessage());
				}
				if (description != null) {
					app.setDescription(description);
					ApplicationManager.getInstance().saveImage(context, app, image);
					games.add(app);
				}
			}
			return games;
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
