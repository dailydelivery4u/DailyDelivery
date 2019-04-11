package in.dailydelivery.dailydelivery.Fragments;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
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
    TextView totalPriceTV, deliveryDateTV;
    int totalPrice, deliveryState;
    Button proceedButton;
    RelativeLayout orderDateRL;
    DatePickerDialog.OnDateSetListener orderDateListner;
    DateTime orderDate, minOrderDate;
    DateTimeFormatter displayDTF, dtf;
    private OnCartDisplayFragmentInteractionListener mListener;

    public CartDisplayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = AppDatabase.getAppDatabase(getActivity());
        super.onCreate(savedInstanceState);
        minOrderDate = new DateTime().plusDays(1);
        orderDate = new DateTime().plusDays(1);
        displayDTF = DateTimeFormat.fullDate();
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        orderDateListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                orderDate = new DateTime(newDate);
                deliveryDateTV.setText(orderDate.toString(displayDTF));
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartdisplay_list, container, false);
        totalPriceTV = view.findViewById(R.id.totalPriceTV);
        proceedButton = view.findViewById(R.id.proceedBtn);
        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        deliveryDateTV = view.findViewById(R.id.deliveryDateTV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        determineDeliveryState();
        if (deliveryState == 1 || deliveryState == 2) {
            //Order Date can be tomorrow
        } else if (deliveryState == 3) {
            //Order cannot be placed for tomo. should be placed only for day after tomo
            orderDate = orderDate.plusDays(1);
            minOrderDate = orderDate.plusDays(1);
        }
        deliveryDateTV.setText(orderDate.toString(displayDTF));


        orderDateRL = view.findViewById(R.id.orderDateRL);
        orderDateRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDateDialog = new DatePickerDialog(getActivity(), orderDateListner, orderDate.getYear(), orderDate.getMonthOfYear() - 1, orderDate.getDayOfMonth());
                startDateDialog.getDatePicker().setMinDate(minOrderDate.getMillis());
                startDateDialog.setMessage("Choose Delivery Date");
                startDateDialog.show();
            }
        });

        new GetCartData().execute();

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.size() > 0) {
                    OrderDetailsFragment.cartList = cartItems;
                    mListener.onCartDisplayFragmentInteraction(orderDate.toString(dtf));
                } else {
                    Toast.makeText(getActivity(), "Please Add Items in Cart to Proceed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        mListener.setActionBarTitle("Cart");
        return view;
    }


    private void determineDeliveryState() {
        DateTime now = new DateTime();
        int hourOfDay = now.hourOfDay().get();
        if (hourOfDay >= 0 && hourOfDay < 16) {
            deliveryState = 1;
        } else if (hourOfDay >= 16 && hourOfDay < 22) {
            deliveryState = 2;
        } else if (hourOfDay >= 22 && hourOfDay < 24) {
            deliveryState = 3;
        }
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
        void onCartDisplayFragmentInteraction(String date);

        void setActionBarTitle(String title);
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