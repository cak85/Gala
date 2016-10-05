package de.cak85.gala.launcher;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import de.cak85.gala.R;
import de.cak85.gala.applications.ApplicationItem;
import de.cak85.gala.applications.ApplicationManager;
import de.cak85.gala.applications.AsyncTaskListener;
import de.cak85.gala.interfaces.ItemTouchHelperAdapter;
import de.cak85.gala.interfaces.ItemTouchHelperViewHolder;
import de.cak85.gala.preferences.PreferencesActivity;

public class GameListActivity extends AppCompatActivity {

	private static final int EDIT_GAMES_REQUEST = 1;
	public static final String DEFAULT_SPACING = "4dp";
	private static final int SHOW_PREFERENCES_REQUEST = 2;
	private static final String DEFAULT_HEIGHT = "64dp";

	/**
	 * Icon background color is given to DetailsActivity using this constant.
	 */
	public static final String INTENT_ICON_BACKGROUND_COLOR = "iconBackgroundColor";

	/**
	 * selected package name is given to DetailsActivity using this constant.
	 */
	public static final String INTENT_PACKAGE_NAME = "packageName";

	private SimpleItemRecyclerViewAdapter mAdapter;
	private int spacing;
	private int height;
	private boolean showShadow;
	private boolean showDownloadedImages;
	private int columns;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

		reloadPreferences();

	    View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setTitle(getResources().getString(R.string.app_name));
    }

	private void reloadPreferences() {
		final SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(this);
		showShadow = sharedPreferences.getBoolean(
				getString(R.string.pref_key_user_interface_shadow), false);
		showDownloadedImages = sharedPreferences.getBoolean(
				getString(R.string.pref_key_user_interface_show_downloaded_images), false);
		spacing = Integer.valueOf(sharedPreferences.getString(
				getString(R.string.pref_key_user_interface_spacing),
				DEFAULT_SPACING).replaceAll("[\\D]", ""));
		height = Integer.valueOf(sharedPreferences.getString(
				getString(R.string.pref_key_user_interface_height),
				DEFAULT_HEIGHT).replaceAll("[\\D]", ""));
		columns = Integer.valueOf(sharedPreferences.getString(
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
	        reloadPreferences();
	        View recyclerView = findViewById(R.id.item_list);
	        assert recyclerView != null;
	        ((GridLayoutManager) ((RecyclerView) recyclerView)
			        .getLayoutManager()).setSpanCount(columns);
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, columns));
		handleFirstRun();
		mAdapter = new SimpleItemRecyclerViewAdapter(ApplicationManager.getInstance().getGames());
        recyclerView.setAdapter(mAdapter);
		ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
		touchHelper.attachToRecyclerView(recyclerView);
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
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>
			implements ItemTouchHelperAdapter {

        private final List<ApplicationItem> mValues;
	    private Context context;
		private float density;
		private long keyDownTime;

		private SimpleItemRecyclerViewAdapter(List<ApplicationItem> items) {
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
	        Drawable image;
	        if (showDownloadedImages) {
		        final Bitmap bitmap = ApplicationManager.getInstance().getImage(GameListActivity.this,
				        mValues.get(position));
		        if (bitmap != null) {
			        image = new BitmapDrawable(getResources(),
					        bitmap);
		        } else {
			        image = mValues.get(position).getIcon();
		        }
	        } else {
		        image = mValues.get(position).getIcon();
	        }
            holder.mImageView.setImageDrawable(image);
            holder.mIdView.setText(mValues.get(position).getName());

	        GridLayoutManager.LayoutParams lp =
			        new GridLayoutManager.LayoutParams(
					        GridLayoutManager.LayoutParams.MATCH_PARENT,
					        GridLayoutManager.LayoutParams.WRAP_CONTENT);
	        density = context.getResources().getDisplayMetrics().density;
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

	        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
			        holder.mImageView.getLayoutParams();
	        params.height = (int) (height * density);
	        holder.mImageView.setLayoutParams(params);

            Palette palette = Palette.from(((BitmapDrawable)mValues.get(
		            position).getIcon()).getBitmap()).generate();
            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
            if (vibrantSwatch != null) {
                holder.mIdView.setBackgroundColor(vibrantSwatch.getRgb());
                holder.mIdView.setTextColor(vibrantSwatch.getBodyTextColor());
            } else {
	            holder.mIdView.setBackgroundColor(palette.getMutedColor(
			            ContextCompat.getColor(GameListActivity.this, R.color.colorPrimary)));
                holder.mIdView.setTextColor(palette.getDarkMutedColor(Color.WHITE));
            }
            final Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
	        final int iconBackgroundColor;
            if (lightMutedSwatch != null) {
                iconBackgroundColor = lightMutedSwatch.getRgb();
            } else {
                iconBackgroundColor = Color.LTGRAY;
            }
	        holder.mImageView.setBackgroundColor(iconBackgroundColor);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
	                Intent myIntent = new Intent(GameListActivity.this, DetailsActivity.class);
	                myIntent.putExtra(INTENT_PACKAGE_NAME, holder.mItem.getPackageName());
	                myIntent.putExtra(INTENT_ICON_BACKGROUND_COLOR, iconBackgroundColor);
	                ActivityOptionsCompat options = ActivityOptionsCompat.
			                makeSceneTransitionAnimation(
					                GameListActivity.this, holder.mImageView,
					                "details");
	                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		                GameListActivity.this.startActivity(myIntent, options.toBundle());
	                } else {
		                GameListActivity.this.startActivity(myIntent);
	                }
                }
            });
	        holder.mView.setOnKeyListener(new View.OnKeyListener() {
		        @Override
		        public boolean onKey(View v, int keyCode, KeyEvent event) {
			        final int i = holder.getAdapterPosition();
			        Log.i("KeyEvent", event.toString());
			        if (event.isLongPress()) {
				        v.setSelected(true);
				        keyDownTime = event.getDownTime();
				        return true;
			        } else if (keyDownTime < event.getDownTime() && v.isSelected()
					        && (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A
					        || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_B
			                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
				        v.setSelected(false);
				        return true;
			        } else if (v.isSelected()) {
				        if (i < mValues.size() - 1 && event.getAction() == KeyEvent.ACTION_DOWN
						        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					        Collections.swap(mValues, i, i + 1);
					        notifyItemMoved(i, i + 1);
					        return true;
				        } else if (i > 0 && event.getAction() == KeyEvent.ACTION_DOWN
						        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
					        Collections.swap(mValues, i, i - 1);
					        notifyItemMoved(i, i - 1);
					        return true;
				        } else if (i + columns < mValues.size()
						        && event.getAction() == KeyEvent.ACTION_DOWN
						        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
					        Collections.swap(mValues, i, i + columns);
					        notifyItemMoved(i, i + columns);
					        return true;
				        } else if (i - columns >= 0 && event.getAction() == KeyEvent.ACTION_DOWN
						        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					        Collections.swap(mValues, i, i - columns);
					        notifyItemMoved(i, i - columns);
					        return true;
				        }
			        }
			        return false;
		        }
	        });
	        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
		        @Override
		        public boolean onLongClick(View v) {
			        return true;
		        }
	        });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

		@Override
		public void onItemMove(int fromPosition, int toPosition) {
			if (fromPosition < toPosition) {
				for (int i = fromPosition; i < toPosition; i++) {
					Collections.swap(mValues, i, i + 1);
				}
			} else {
				for (int i = fromPosition; i > toPosition; i--) {
					Collections.swap(mValues, i, i - 1);
				}
			}
			notifyItemMoved(fromPosition, toPosition);
		}

		class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
            private final View mView;
            private final ImageView mImageView;
            private final TextView mIdView;
            private ApplicationItem mItem;

            private ViewHolder(View view) {
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

			@Override
			public void onItemSelected() {
				mView.setSelected(true);
			}

			@Override
			public void onItemClear() {
				mView.setSelected(false);
			}
		}
    }
}
