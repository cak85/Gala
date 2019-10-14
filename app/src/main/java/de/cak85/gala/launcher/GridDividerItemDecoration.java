package de.cak85.gala.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Item Decorator for the EditGAmesActivity.
 * <br><br>
 * Created by ckuster on 21.01.2016.
 */
class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

	private static final int[] ATTRS = { android.R.attr.listDivider };

	private final Drawable mDivider;

	public GridDividerItemDecoration(Context context) {
		TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		a.recycle();
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
		drawVertical(c, parent);
		drawHorizontal(c, parent);
	}

	/** Draw dividers at each expected grid interval */
	private void drawVertical(Canvas c, RecyclerView parent) {
		if (parent.getChildCount() == 0) return;

		int childCount = parent.getChildCount();
		childCount -= ((childCount % 2) == 1) ? 1 : 2;

		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params =
					(RecyclerView.LayoutParams) child.getLayoutParams();

			final int left = child.getLeft() - params.leftMargin;
			final int right = child.getRight() + params.rightMargin;
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + mDivider.getIntrinsicHeight();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	/** Draw dividers to the right of each child view */
	private void drawHorizontal(Canvas c, RecyclerView parent) {
		final int childCount = parent.getChildCount();

		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params =
					(RecyclerView.LayoutParams) child.getLayoutParams();

			final int left = child.getRight() + params.rightMargin;
			final int right = left + mDivider.getIntrinsicWidth();
			final int top = child.getTop() - params.topMargin;
			final int bottom = child.getBottom() + params.bottomMargin;
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		//We can supply forced insets for each item view here in the Rect
		outRect.set(0, 0, 0, 0);
	}
}