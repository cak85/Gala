package de.cak85.gala.applications;

/**
 * Listener that can be attached to some AsyncTask defined in {@link ApplicationManager}.
 *
 * <br><br>
 * Created by ckuster on 29.01.2016.
 */
public interface AsyncTaskListener<Result, Progress> {
	void onPreExecute();
	void onProgress(Progress... progress);
	void onPostExecute(Result result);
}
