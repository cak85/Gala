package de.cak85.gala.launcher;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import de.cak85.gala.interfaces.ItemTouchHelperAdapter;
import de.cak85.gala.interfaces.ItemTouchHelperViewHolder;

/**
 * Created by ckuster on 23.09.2016.
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

	private final ItemTouchHelperAdapter mAdapter;

	public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
		mAdapter = adapter;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN
				| ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
		return makeMovementFlags(dragFlags, 0);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
	                      RecyclerView.ViewHolder target) {
		mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
	}

	@Override
	public boolean isLongPressDragEnabled() {
		return true;
	}

	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		// We only want the active item
		if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
			if (viewHolder instanceof ItemTouchHelperViewHolder) {
				ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
				itemViewHolder.onItemSelected();
			}
		}
		super.onSelectedChanged(viewHolder, actionState);
	}

	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		super.clearView(recyclerView, viewHolder);
		if (viewHolder instanceof ItemTouchHelperViewHolder) {
			ItemTouchHelperViewHolder itemViewHolder =
					(ItemTouchHelperViewHolder) viewHolder;
			itemViewHolder.onItemClear();
		}
	}
}
