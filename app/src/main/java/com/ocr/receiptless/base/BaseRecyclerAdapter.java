package com.ocr.receiptless.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    protected List<T> mData;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected OnItemClickListener mClickListener;

    private RecyclerView.LayoutManager mLayoutManager;

    public BaseRecyclerAdapter(Context context, List<T> data) {
        this(context, data, null);
    }

    public BaseRecyclerAdapter(Context context, List<T> data, RecyclerView.LayoutManager layoutManager) {
        mContext = context;
        mLayoutManager = layoutManager;
        mData = data == null ? new ArrayList<T>() : data;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(mContext, mInflater.inflate(getItemLayoutId(viewType), parent, false));
        if (mClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, holder.getLayoutPosition());
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        bindData(holder, position, mData.get(position));
    }

    @Override
    public void onViewDetachedFromWindow(BaseRecyclerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    public void add(int pos, T item) {
        mData.add(pos, item);
        notifyItemInserted(pos);
    }

    public void delete(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void addMoreData(List<T> data) {
        int startPos = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(startPos, data.size());
    }

    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return bindViewType(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public abstract int getItemLayoutId(int viewType);

    public abstract void bindData(BaseRecyclerViewHolder holder, int position, T item);

    protected int bindViewType(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }
}

