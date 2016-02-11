package de.cak85.gala.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;
import de.cak85.gala.applications.ApplicationManager;
import de.cak85.gala.applications.AsyncTaskListener;

/**
 * Created by ckuster on 13.01.2016.
 */
public class EditGamesActivity extends AppCompatActivity {

	static private List<ApplicationItem> applications;
    private EditGamesRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_games);

        View recyclerView = findViewById(R.id.edit_games_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.edit_games_list_toolbar);
        setSupportActionBar(mActionBarToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_manage_apps));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
	    Intent data = new Intent();
	    setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new GridDividerItemDecoration(this));
        if (applications == null) {
	        applications = new ArrayList<>();
            ApplicationManager.getInstance().getInstalledApplications(this,
		            new AsyncTaskListener<List<ApplicationItem>, Void>() {
	            @Override
	            public void onPreExecute() {
		            View progressbar = findViewById(R.id.edit_games_list_progressbar);
		            View recyclerView = findViewById(R.id.edit_games_list);
		            progressbar.setVisibility(View.VISIBLE);
		            recyclerView.setVisibility(View.GONE);
	            }
	            @Override
	            public void onProgress(Void... progress) {}
	            @Override
	            public void onPostExecute(List<ApplicationItem> apps) {
		            applications.addAll(apps);
		            final Collator collator = Collator.getInstance(Locale.GERMAN);
		            collator.setStrength(Collator.SECONDARY);
		            Collections.sort(applications, new Comparator<ApplicationItem>() {
			            @Override
			            public int compare(ApplicationItem lhs, ApplicationItem rhs) {
				            return collator.compare(lhs.getName(), rhs.getName());
			            }
		            });
		            View progressbar = findViewById(R.id.edit_games_list_progressbar);
		            View recyclerView = findViewById(R.id.edit_games_list);
		            progressbar.setVisibility(View.GONE);
		            recyclerView.setVisibility(View.VISIBLE);
		            mAdapter.notifyDataSetChanged();
	            }
            });
        }
        mAdapter = new EditGamesRecyclerViewAdapter(applications);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(null);
    }

    public class EditGamesRecyclerViewAdapter
            extends RecyclerView.Adapter<EditGamesRecyclerViewAdapter.ViewHolder> {

        private final List<ApplicationItem> mValues;

        public EditGamesRecyclerViewAdapter(List<ApplicationItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_games_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
	        final List<ApplicationItem> games =
			        ApplicationManager.getInstance().getGames();
            holder.mView.setOnKeyListener(new View.OnKeyListener() {
	            @Override
	            public boolean onKey(View v, int keyCode, KeyEvent event) {
		            if (event.getAction() == KeyEvent.ACTION_UP) {
			            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
				            if (games.contains(holder.mItem)) {
					            games.remove(holder.mItem);
				            } else {
					            games.add(holder.mItem);
					            DisplayMetrics metrics = new DisplayMetrics();
					            getWindowManager().getDefaultDisplay().getMetrics(metrics);
					            ApplicationManager.getInstance().retrieveInfos(EditGamesActivity.this,
                                        metrics.widthPixels,
                                        holder.mItem,
                                        null);
				            }
				            notifyItemChanged(position);
				            holder.mView.requestFocus();
				            return false;
			            }
		            }
		            return false;
	            }
            });
	        holder.mImageView.setImageDrawable(mValues.get(position).getIcon());
            holder.mTitleView.setText(mValues.get(position).getName());
	        holder.mCheckbox.setOnCheckedChangeListener(null);
            holder.mCheckbox.setChecked(games.contains(holder.mItem));
            holder.mCheckbox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                if (games.contains(holder.mItem)) {
		                games.remove(holder.mItem);
	                } else {
		                games.add(holder.mItem);
		                DisplayMetrics metrics = new DisplayMetrics();
		                getWindowManager().getDefaultDisplay().getMetrics(metrics);
		                ApplicationManager.getInstance().retrieveInfos(EditGamesActivity.this,
                                metrics.widthPixels,
                                holder.mItem,
                                null);
	                }
	                System.out.println(games);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mTitleView;
            public final CheckBox mCheckbox;
            public ApplicationItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.edit_games_image);
                mTitleView = (TextView) view.findViewById(R.id.edit_games_title);
                mCheckbox = (CheckBox) view.findViewById(R.id.edit_games_checkbox);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }
}
