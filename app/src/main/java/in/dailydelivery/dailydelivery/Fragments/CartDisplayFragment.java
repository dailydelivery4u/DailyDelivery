package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.R;


interface onItemRemovedListner {
    void onItemRemoved(int product_id, int cat_id, int position);
}

interface onQtyChangedListner {
    void onQtyChanged(int pId, int catId, int qty, int position);
}


public class CartDisplayFragment extends Fragment implements onItemRemovedListner, onQtyChangedListner {
    AppDatabase db;
    List<Cart> cartItems;
    RecyclerView recyclerView;
    MycartDisplayRecyclerViewAdapter mycartDisplayRecyclerViewAdapter;
    TextView totalPriceTV;
    int totalPrice;
    Button proceedButton;
    private OnCartDisplayFragmentInteractionListener mListener;

    public CartDisplayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = AppDatabase.getAppDatabase(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartdisplay_list, container, false);
        totalPriceTV = view.findViewById(R.id.totalPriceTV);
        proceedButton = view.findViewById(R.id.proceedBtn);
        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        new GetCartData().execute();

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.size() > 0) {
                    OrderDetailsFragment.cartList = cartItems;
                    mListener.onCartDisplayFragmentInteraction(1);
                } else {
                    Toast.makeText(getActivity(), "Please Add Items in Cart to Proceed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCartDisplayFragmentInteractionListener) {
            mListener = (OnCartDisplayFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCartDisplayFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemRemoved(int product_id, int cat_id, int position) {
        cartItems.remove(position);
        new RemoveItemFromCart().execute(product_id, cat_id);
    }

    @Override
    public void onQtyChanged(int pId, int catId, int qty, int position) {
        cartItems.get(position).setProductqty(qty);
        new UpdateQty().execute(pId, catId, qty);
    }

    public void updateTotalPrice() {
        totalPrice = 0;
        for (int p = 0; p < cartItems.size(); p++) {
            totalPrice += cartItems.get(p).getProductDdprice() * cartItems.get(p).getProductqty();
        }
        totalPriceTV.setText("Total: Rs. " + String.valueOf(totalPrice));
    }


    public interface OnCartDisplayFragmentInteractionListener {
        void onCartDisplayFragmentInteraction(int proceed);
    }

    private class GetCartData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            cartItems = db.userDao().getAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mycartDisplayRecyclerViewAdapter = new MycartDisplayRecyclerViewAdapter(cartItems, getActivity(), mListener, CartDisplayFragment.this, CartDisplayFragment.this);
            recyclerView.setAdapter(mycartDisplayRecyclerViewAdapter);
            totalPrice = 0;
            for (int p = 0; p < cartItems.size(); p++) {
                totalPrice += cartItems.get(p).getProductDdprice() * cartItems.get(p).getProductqty();
            }
            totalPriceTV.setText("Total: Rs. " + String.valueOf(totalPrice));
        }
    }

    private class RemoveItemFromCart extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            db.userDao().deleteProd(integers[0], integers[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateTotalPrice();
        }
    }

    private class UpdateQty extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            db.userDao().updateQty(integers[0], integers[1], integers[2]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateTotalPrice();
        }
    }

}