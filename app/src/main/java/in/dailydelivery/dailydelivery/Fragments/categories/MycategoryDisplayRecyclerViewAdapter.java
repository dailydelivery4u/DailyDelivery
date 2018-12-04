package in.dailydelivery.dailydelivery.Fragments.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.Fragments.categories.Categories.category;
import in.dailydelivery.dailydelivery.Fragments.categories.CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener;
import in.dailydelivery.dailydelivery.R;

/**
 * {@link RecyclerView.Adapter} that can display a {@link category} and makes a call to the
 * specified {CategoryDisplayFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MycategoryDisplayRecyclerViewAdapter extends RecyclerView.Adapter<MycategoryDisplayRecyclerViewAdapter.ViewHolder> {

    private final List<category> mValues;
    private final CategoryDisplayFragmentInteractionListener mListener;

    public MycategoryDisplayRecyclerViewAdapter(List<category> items, CategoryDisplayFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
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
        holder.mIdView.setText(String.valueOf(mValues.get(position).getId()));
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
        public final TextView mIdView;
        public final TextView mContentView;
        public category mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
