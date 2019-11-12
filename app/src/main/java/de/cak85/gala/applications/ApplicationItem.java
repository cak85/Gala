package de.cak85.gala.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import de.cak85.gala.R;


public class ApplicationItem {

	private final String name;
	private final String packageName;
	private transient Drawable icon;
	private String title;
	private String description;

	ApplicationItem(Context context, @NonNull ApplicationInfo packageInfo,
                    @NonNull PackageManager pm) {
		// Get the application's resources
		Resources res = null;
		Drawable icon;
		Configuration originalConfig = null;
		DisplayMetrics dm = null;
		try {
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
			icon = packageInfo.loadIcon(pm);
		} catch (PackageManager.NameNotFoundException e) {
			icon = packageInfo.loadIcon(pm);
		} finally {
			if (originalConfig != null) {
				// Set our configuration back to what it was
				res.updateConfiguration(originalConfig, dm);
			}
		}

		this.name = pm.getApplicationLabel(packageInfo).toString();
		this.packageName = packageInfo.packageName;
		this.icon = icon;
		this.description = context.getString(R.string.app_default_description);
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public Drawable getIcon() {
		return icon;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationItem that = (ApplicationItem) o;
        return packageName.equals(that.packageName);
    }

	@Override
	public int hashCode() {
		return packageName != null ? packageName.hashCode() : 0;
	}

	public String getDescription() {
		return description;
	}

	@Override
    @NonNull
	public String toString() {
		return "ApplicationItem{" +
				"name='" + name + '\'' +
				", packageName='" + packageName + '\'' +
				'}';
	}

	void setDescription(String description) {
		this.description = description;
	}

	void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
