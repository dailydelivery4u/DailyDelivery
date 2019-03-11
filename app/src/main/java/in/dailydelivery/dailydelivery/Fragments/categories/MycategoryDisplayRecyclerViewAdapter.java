package in.dailydelivery.dailydelivery.Fragments.categories;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import in.dailydelivery.dailydelivery.Fragments.categories.Categories.category;
import in.dailydelivery.dailydelivery.Fragments.categories.CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener;
import in.dailydelivery.dailydelivery.R;

public class MycategoryDisplayRecyclerViewAdapter extends RecyclerView.Adapter<MycategoryDisplayRecyclerViewAdapter.ViewHolder> {

    private final List<category> mValues;
    private final CategoryDisplayFragmentInteractionListener mListener;
    private Context context;

    public MycategoryDisplayRecyclerViewAdapter(List<category> items, CategoryDisplayFragmentInteractionListener listener, Context context_) {
        mValues = items;
        mListener = listener;
        context = context_;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_categorydisplay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(String.valueOf(mValues.get(position).getId()));
        Glide.with(this.context)
                .load(mValues.get(position).getPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.catImageView);
        holder.mContentView.setText(mValues.get(position).getCatName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.categoryFragmentInteraction(holder.mItem);
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
        public final ImageView catImageView;
        public final TextView mContentView;
        public category mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            catImageView = view.findViewById(R.id.catImageView);
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
