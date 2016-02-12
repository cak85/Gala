package de.cak85.gala.launcher;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;
import de.cak85.gala.applications.ApplicationManager;
import de.cak85.gala.applications.AsyncTaskListener;
import de.cak85.gala.preferences.PreferencesActivity;

public class GameListActivity extends AppCompatActivity {

	private static final int EDIT_GAMES_REQUEST = 1;
	public static final String DEFAULT_SPACING = "4";
	private static final int SHOW_PREFERENCES_REQUEST = 2;
	private SimpleItemRecyclerViewAdapter mAdapter;
	private int spacing;
	private boolean showShadow;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

		final SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		setSpacing(sharedPreferences);
		setShadow(sharedPreferences);

	    View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setTitle(getResources().getString(R.string.app_name));
    }

	private void setShadow(SharedPreferences sharedPreferences) {
		showShadow = sharedPreferences.getBoolean(
				getString(R.string.pref_key_user_interface_shadow), false);
	}

	private void setSpacing(SharedPreferences sharedPreferences) {
		spacing = Integer.valueOf(sharedPreferences.getString(
				getString(R.string.pref_key_user_interface_spacing),
				DEFAULT_SPACING).replaceAll("[\\D]", ""));
	}

	private int getColumns(SharedPreferences sharedPreferences) {
		return  Integer.valueOf(sharedPreferences.getString(
				getString(R.string.pref_key_user_interface_num_columns),
				String.valueOf(getResources().getInteger(R.integer.num_grids))));
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_list_menu, menu);
        return true;
    }

    public void editGames(MenuItem item){
        Intent myIntent = new Intent(GameListActivity.this, EditGamesActivity.class);
        startActivityForResult(myIntent, EDIT_GAMES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_GAMES_REQUEST) {
            if (resultCode == RESULT_OK) {
                mAdapter.notifyDataSetChanged();
	            ApplicationManager.getInstance().save(this);
            }
        } else if (requestCode == SHOW_PREFERENCES_REQUEST) {
	        SharedPreferences sharedPreferences =
			        PreferenceManager.getDefaultSharedPreferences(this);
	        setSpacing(sharedPreferences);
	        setShadow(sharedPreferences);
	        View recyclerView = findViewById(R.id.item_list);
	        assert recyclerView != null;
	        ((GridLayoutManager) ((RecyclerView) recyclerView)
			        .getLayoutManager()).setSpanCount(getColumns(sharedPreferences));
	        mAdapter.notifyDataSetChanged();
        }
    }

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		//View recyclerView = findViewById(R.id.item_list);
		//recyclerView.getRootView().setBackgroundColor(getResources().getColor(android.R.color.background_light));
		FragmentManager fm = getFragmentManager();
		StartScreenFragment f = (StartScreenFragment) fm.findFragmentByTag("tag");
		if (f != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(f);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			ft.commit();
		}
		super.onResume();
	}

	private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(this,
		        getColumns(PreferenceManager.getDefaultSharedPreferences(this))));
		handleFirstRun();
		mAdapter = new SimpleItemRecyclerViewAdapter(ApplicationManager.getInstance().getGames());
        recyclerView.setAdapter(mAdapter);
    }

	private void handleFirstRun() {
		final ApplicationManager applicationManager = ApplicationManager.getInstance();
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getBoolean("isFirstRun", true)) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
						case DialogInterface.BUTTON_POSITIVE:
							autoDiscoverGames();
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.first_run_question))
					.setPositiveButton(getString(android.R.string.yes),
							dialogClickListener).setNegativeButton(getString(android.R.string.no),
					dialogClickListener).show();
			settings.edit().putBoolean("isFirstRun", false).apply();
		} else {
			applicationManager.load(this);
		}
	}

	private void autoDiscoverGames() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ApplicationManager.getInstance().autoDiscoverGames(GameListActivity.this,
				metrics.widthPixels,
				new AsyncTaskListener<List<ApplicationItem>, String>() {
					@Override
					public void onPreExecute() {
					}

					@Override
					public void onProgress(String... progress) {
					}

					@Override
					public void onPostExecute(
							List<ApplicationItem> applicationItems) {
						mAdapter.notifyDataSetChanged();
						ApplicationManager.getInstance().save(GameListActivity.this);
					}
				});
	}

	public void showPreferences(MenuItem item) {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivityForResult(i, SHOW_PREFERENCES_REQUEST);
    }

	// used to prevent the dialog for discovering games can accidentally be shown twice.
	private boolean dialogShown = false;

	public void discoverGames(MenuItem item) {
		if (dialogShown) {
			return;
		}
		dialogShown = true;
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						autoDiscoverGames();
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				dialogShown = false;
			}
		});
		builder.setMessage(getString(R.string.discover_games_question))
				.setPositiveButton(getString(android.R.string.yes),
						dialogClickListener).setNegativeButton(getString(android.R.string.no),
				dialogClickListener).show();
	}

	public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<ApplicationItem> mValues;
	    private Context context;

	    public SimpleItemRecyclerViewAdapter(List<ApplicationItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	        context = parent.getContext();
	        View view = LayoutInflater.from(context)
                    .inflate(R.layout.game_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mImageView.setImageDrawable(mValues.get(position).getIcon());
            holder.mIdView.setText(mValues.get(position).getName());

	        GridLayoutManager.LayoutParams lp =
			        new GridLayoutManager.LayoutParams(
					        GridLayoutManager.LayoutParams.MATCH_PARENT,
					        GridLayoutManager.LayoutParams.WRAP_CONTENT);
	        final float density = context.getResources().getDisplayMetrics().density;
	        int margin = (int) (density * spacing);
	        lp.setMargins(margin, margin, margin, margin);
	        holder.mView.setLayoutParams(lp);

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		        if (showShadow) {
			        holder.mView.setElevation(2f * density);
		        } else {
			        holder.mView.setElevation(0f);
		        }
	        }

            Palette palette = Palette.from(((BitmapDrawable)mValues.get(
		            position).getIcon()).getBitmap()).generate();
            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
            if (vibrantSwatch != null) {
                holder.mIdView.setBackgroundColor(vibrantSwatch.getRgb());
                holder.mIdView.setTextColor(vibrantSwatch.getBodyTextColor());
            } else {
	            holder.mIdView.setBackgroundColor(palette.getMutedColor(
		                getResources().getColor(R.color.colorPrimary)));
                holder.mIdView.setTextColor(palette.getDarkMutedColor(Color.WHITE));
            }
            final Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
            if (lightMutedSwatch != null) {
                holder.mImageView.setBackgroundColor(lightMutedSwatch.getRgb());
            } else {
                holder.mImageView.setBackgroundColor(Color.LTGRAY);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
	                Intent myIntent = new Intent(GameListActivity.this, DetailsActivity.class);
	                myIntent.putExtra("packageName", holder.mItem.getPackageName());
	                ActivityOptionsCompat options = ActivityOptionsCompat.
			                makeSceneTransitionAnimation(
					                GameListActivity.this, (View) holder.mImageView,
					                "details");
	                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		                GameListActivity.this.startActivity(myIntent, options.toBundle());
	                } else {
		                GameListActivity.this.startActivity(myIntent);
	                }
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
            public final TextView mIdView;
            public ApplicationItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.image);
                mIdView = (TextView) view.findViewById(R.id.title);
//                mContentView = (TextView) view.findViewById(R.title.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mIdView.getText() + "'";
            }
        }
    }
}
