package in.dailydelivery.dailydelivery.Fragments.categories;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.dailydelivery.dailydelivery.R;

public class CategoryTypesRecyclerViewAdapter extends RecyclerView.Adapter<CategoryTypesRecyclerViewAdapter.ViewHolder> {

    private final List<CatDataModel> mValues;
    //SnapHelper snapHelper;
    private final CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener mListener;
    private Context context;
    private RecyclerView.RecycledViewPool recycledViewPool;

    public CategoryTypesRecyclerViewAdapter(List<CatDataModel> mValues, Context context, CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener mListener) {
        this.mValues = mValues;
        this.context = context;
        this.mListener = mListener;
        recycledViewPool = new RecyclerView.RecycledViewPool();
        //snapHelper = new GravitySnapHelper(Gravity.START);
    }

    @Override
    public CategoryTypesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cat_type, parent, false);
        return new CategoryTypesRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryTypesRecyclerViewAdapter.ViewHolder holder, int position) {
        String sectionHeader = mValues.get(position).getCatTypeHeader();
        ArrayList categoriesInSection = mValues.get(position).getAllCategoriesInSection();
        holder.catHeaderTextView.setText(sectionHeader);

        MycategoryDisplayRecyclerViewAdapter adapter = new MycategoryDisplayRecyclerViewAdapter(categoriesInSection, mListener, context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.catDisplayRecyclerView.setLayoutManager(layoutManager);
        holder.catDisplayRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));
        //recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(5,3));
        holder.catDisplayRecyclerView.setAdapter(adapter);
        holder.catDisplayRecyclerView.setRecycledViewPool(recycledViewPool);
        //snapHelper.attachToRecyclerView(holder.catDisplayRecyclerView);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView catHeaderTextView;
        public final RecyclerView catDisplayRecyclerView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            catHeaderTextView = view.findViewById(R.id.txtHeader);
            catDisplayRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        }
    }
}
