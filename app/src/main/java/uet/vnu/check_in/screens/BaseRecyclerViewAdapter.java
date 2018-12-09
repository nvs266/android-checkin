package uet.vnu.check_in.screens;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected final Context mContext;
    protected List<T> mCollection;
    protected RecyclerViewItemListener<T> mRecyclerViewItemListener;
    protected RecyclerView mRecyclerView;

    protected BaseRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCollection == null || mRecyclerViewItemListener == null) {
                    return;
                }

                int position = holder.getAdapterPosition();
                mRecyclerViewItemListener.onRecyclerViewItemClicked(
                        mRecyclerView,
                        mCollection.get(position),
                        position
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCollection == null ? 0 : mCollection.size();
    }

    public void setRecyclerViewItemListener(RecyclerViewItemListener<T> listener) {
        if (listener == null) {
            return;
        }
        mRecyclerViewItemListener = listener;
    }

    public List<T> getCollection() {
        return mCollection;
    }

    public void addItem(T item) {
        if (item == null) {
            return;
        }

        if (mCollection == null) {
            mCollection = new ArrayList<>();
        }

        mCollection.add(item);
        notifyDataSetChanged();
    }

    public void addCollection(List<T> collection) {
        if (collection == null) {
            return;
        }

        if (mCollection == null) {
            mCollection = collection;
            notifyDataSetChanged();
            return;
        }

        mCollection.addAll(collection);
        notifyDataSetChanged();
    }

    public void clearCollection() {
        if (mCollection == null || mCollection.isEmpty()) {
            return;
        }

        mCollection.clear();
        notifyDataSetChanged();
    }

    public interface RecyclerViewItemListener<T> {
        void onRecyclerViewItemClicked(RecyclerView recyclerView, T data, int position);
    }
}
