package com.xdandroid.simplerecyclerview;

import android.graphics.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.xdandroid.materialprogressview.*;

import java.util.*;

/**
 * Created by XingDa on 2016/05/29.
 */

public abstract class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected ProgressViewHolder mProgressViewHolder;
    protected MaterialProgressViewHolder mMaterialProgressViewHolder;
    protected int[] mColorSchemeColors;
    protected boolean mIsLoading;
    protected int mThreshold = 7;
    protected boolean mUseMaterialProgress;
    protected int mHeaderCount;

    public void setThreshold(int threshold) {
        this.mThreshold = threshold;
    }

    public void setUseMaterialProgress(boolean useMaterialProgress, int[] colors) {
        this.mUseMaterialProgress = useMaterialProgress;
        this.mColorSchemeColors = colors;
    }

    public void setColorSchemeColors(int[] colors) {
        this.mColorSchemeColors = colors;
    }

    protected abstract void onLoadMore(Void please_make_your_adapter_class_as_abstract_class);
    protected abstract boolean hasMoreElements(Void let_activity_or_fragment_implement_these_methods);
    protected abstract RecyclerView.ViewHolder onViewHolderCreate(ViewGroup parent, int viewType);
    /**
     * 不要将position传入匿名内部类/方法内部类（如OnClickListener）。
     * Java只实现了值捕获，在匿名内部类中保存的position实际上是onBindViewHolder的position参数的一个副本。
     * 又因为int是基本类型，在设置创建匿名内部类（如设置OnClickListener）时，
     * 该int的值复制到了匿名内部类里面的position，而不是指向同一块栈内存。
     * 当position改变时（如滑动删除Item时），RecyclerView不会自动重新回调onBindViewHolder。
     * 这将导致匿名内部类（如OnClickListener）中的position还是原来的，而没有得到更新，发生点击错位的问题。
     * 要在匿名内部类（如OnClickListener）中得到当前Item的位置，请使用holder.getAdapterPosition()，
     * 通过ViewHolder动态获取当前Item的位置。而不要将position设为final或事实上是final的。
     */
    protected abstract void onViewHolderBind(RecyclerView.ViewHolder holder, int position, int viewType);
    protected abstract int getViewType(int position);
    protected abstract int getCount();
    protected abstract int getItemSpanSizeForGrid(int position, int viewType, int spanCount);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 65535) {
            return onViewHolderCreate(parent, viewType);
        } else {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            ViewGroup.MarginLayoutParams outerParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            frameLayout.setLayoutParams(outerParams);
            if (mUseMaterialProgress) {
                MaterialProgressView materialProgressView = new MaterialProgressView(parent.getContext());
                if (mColorSchemeColors == null || mColorSchemeColors.length <= 0) {
                    int colorAccentId = parent.getContext().getResources().getIdentifier("colorAccent", "color", parent.getContext().getPackageName());
                    int color;
                    if (colorAccentId > 0) {
                        color = parent.getContext().getResources().getColor(colorAccentId);
                    } else {
                        color = Color.parseColor("#607D8B");
                    }
                    materialProgressView.setColorSchemeColors(new int[]{color});
                } else {
                    materialProgressView.setColorSchemeColors(mColorSchemeColors);
                }
                FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                innerParams.setMargins(0, UIUtils.dp2px(parent.getContext(), 6), 0, UIUtils.dp2px(parent.getContext(), 6));
                innerParams.gravity = Gravity.CENTER;
                materialProgressView.setLayoutParams(innerParams);
                materialProgressView.setId(android.R.id.secondaryProgress);
                frameLayout.addView(materialProgressView);
                mMaterialProgressViewHolder = new MaterialProgressViewHolder(frameLayout);
                return mMaterialProgressViewHolder;
            } else {
                ProgressBar progressBar = new ProgressBar(parent.getContext());
                FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(UIUtils.dp2px(parent.getContext(), 40), UIUtils.dp2px(parent.getContext(), 40));
                innerParams.setMargins(0, UIUtils.dp2px(parent.getContext(), 6), 0, UIUtils.dp2px(parent.getContext(), 6));
                innerParams.gravity = Gravity.CENTER;
                progressBar.setLayoutParams(innerParams);
                progressBar.setId(android.R.id.progress);
                frameLayout.addView(progressBar);
                mProgressViewHolder = new ProgressViewHolder(frameLayout);
                return mProgressViewHolder;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == getCount() ? 65535 : getViewType(position);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (!mIsLoading && getCount() > 0 && position >= getCount() - mThreshold && hasMoreElements(null)) {
            mIsLoading = true;
            onLoadMore(null);
        }
        if (position == getCount()) {
            if (!mUseMaterialProgress && holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).mProgressBar.setVisibility(mIsLoading ? View.VISIBLE : View.GONE);
            } else if (mUseMaterialProgress && holder instanceof MaterialProgressViewHolder) {
                ((MaterialProgressViewHolder) holder).mProgressBar.setVisibility(mIsLoading ? View.VISIBLE : View.GONE);
            }
        } else {
            onViewHolderBind(holder, holder.getAdapterPosition(), getViewType(holder.getAdapterPosition()));
            if (mOnItemClickLitener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(holder, holder.itemView, holder.getAdapterPosition(), getViewType(holder.getAdapterPosition()));
                    }
                });
            }
            if (mOnItemLongClickLitener != null) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return mOnItemLongClickLitener.onItemLongClick(holder, holder.itemView, holder.getAdapterPosition(), getViewType(holder.getAdapterPosition()));
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return getCount() + 1;
    }

    public void setLoadingFalse() {
        mIsLoading = false;
        if (!mUseMaterialProgress && mProgressViewHolder != null) {
            mProgressViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        } else if (mUseMaterialProgress && mMaterialProgressViewHolder != null) {
            mMaterialProgressViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    protected OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    protected OnItemLongClickLitener mOnItemLongClickLitener;

    public void setOnItemLongClickLitener(OnItemLongClickLitener mOnItemLongClickLitener) {
        this.mOnItemLongClickLitener = mOnItemLongClickLitener;
    }

    protected abstract class ProgressSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        protected int spanCount;

        protected ProgressSpanSizeLookup(int spanCount) {
            this.spanCount = spanCount;
        }

        @Override
        public int getSpanSize(int position) {
            int viewType = getItemViewType(position);
            if (viewType == 65535) {
                return spanCount;
            } else {
                int itemSpanSize = getItemSpanSize(position, viewType, spanCount);
                if (itemSpanSize < 1) itemSpanSize = 1;
                if (itemSpanSize > spanCount) itemSpanSize = spanCount;
                return itemSpanSize;
            }
        }

        protected abstract int getItemSpanSize(int position, int viewType, int spanCount);
    }

    public ProgressSpanSizeLookup getSpanSizeLookup(int spanCount) {
        return new ProgressSpanSizeLookup(spanCount) {
            @Override
            protected int getItemSpanSize(int position, int viewType, int spanCount) {
                return getItemSpanSizeForGrid(position, viewType, spanCount);
            }
        };
    }

    public void onListChanged() {
        notifyDataSetChanged();
        setLoadingFalse();
    }

    public void onListSetUp(int listSize) {
        notifyItemRangeInserted(0, listSize);
        setLoadingFalse();
    }

    public void onListCleared(int oldDataSize) {
        notifyItemRangeRemoved(0, oldDataSize);
        setLoadingFalse();
    }

    public void onAdded() {
        notifyItemInserted(getCount() - 1);
        setLoadingFalse();
    }

    public void onAdded(int position) {
        notifyItemInserted(position);
        setLoadingFalse();
    }

    public void onRemoved(int position) {
        notifyItemRemoved(position);
        setLoadingFalse();
    }

    public void onRemoved() {
        onRemovedLast();
    }

    public void onRemovedLast() {
        notifyItemRemoved(getCount());
        setLoadingFalse();
    }

    public void onRemoveAll(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
        setLoadingFalse();
    }

    public void onSet(int position) {
        notifyItemChanged(position);
        setLoadingFalse();
    }

    public void onSetAll(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
        setLoadingFalse();
    }

    public void onAddedAll(int position, int newDataSize) {
        notifyItemRangeInserted(position, newDataSize);
        setLoadingFalse();
    }

    public void onAddedAll(int newDataSize) {
        notifyItemRangeInserted(getCount() - newDataSize, newDataSize);
        setLoadingFalse();
    }
}
