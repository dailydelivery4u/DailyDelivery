package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.dailydelivery.dailydelivery.R;
import in.dailydelivery.dailydelivery.Fragments.categories.Categories;
import in.dailydelivery.dailydelivery.Fragments.categories.Categories.category;

import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {CategoryDisplayFragmentInteractionListener}
 * interface.
 */
public class CategoryDisplayFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    SharedPreferences sharedPreferences;


    private CategoryDisplayFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategoryDisplayFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CategoryDisplayFragment newInstance(int columnCount) {
        CategoryDisplayFragment fragment = new CategoryDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        sharedPreferences = this.getActivity().getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorydisplay_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            for(int i=0;i<sharedPreferences.getInt("cat_num",0);i++){
                Categories.category cat = new category(String.valueOf(i),sharedPreferences.getString("cat_" +String.valueOf(i),"Product"));
                Categories.addItem(cat);
            }
            recyclerView.setAdapter(new MycategoryDisplayRecyclerViewAdapter(Categories.ITEMS, mListener));
        }
        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CategoryDisplayFragmentInteractionListener) {
            mListener = (CategoryDisplayFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CategoryDisplayFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface CategoryDisplayFragmentInteractionListener {
        // TODO: Update argument type and name
        void categoryFragmentInteraction(category item);
    }
}
