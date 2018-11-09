package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;

import org.joda.time.DateTime;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.R;

public class OrderDetailsFragment extends Fragment {

    private Spinner spinner;
    private OnOrderDetailsFragmentInteractionListener mListener;
    public static List<Cart> cartList;
    private int deliveryState;
    private CalendarView calendarView;
    DateTime today = new DateTime();
    DateTime tomo = today.plusDays(1);
    Button placeOrderBtn;
    String dateSelected;

    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    public static OrderDetailsFragment newInstance(String param1, String param2) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);
        spinner = view.findViewById(R.id.delivery_time_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.delivery_slots, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        calendarView = view.findViewById(R.id.calendarView);
        placeOrderBtn = view.findViewById(R.id.placeOrderBtn);
        determineDeliveryState();
        setCalenderLimits();
        setTimeSlot(new DateTime().withMillis(calendarView.getDate()).getDayOfMonth());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                setTimeSlot(dayOfMonth);
                int month_n = month + 1;
                dateSelected = String.valueOf((dayOfMonth < 10 ? ("0" + dayOfMonth) : dayOfMonth) + "-" + (month_n < 10 ? ("0" + month_n) : month_n) + "-" + year);
                //Toast.makeText(getActivity(),"Date: " + dateSelected,Toast.LENGTH_SHORT).show();

            }
        });
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrderBtn.setEnabled(false);
                mListener.onOrderDetailsFragmentInteraction(dateSelected, spinner.getSelectedItemPosition());
            }
        });
        return view;
    }


    private void setTimeSlot(int day) {
        if (day >= tomo.getDayOfMonth()) {
            if (cartContainsWater()) {
                spinner.setSelection(1);
                spinner.setEnabled(false);
            } else if (deliveryState == 3) {
                if (day >= tomo.getDayOfMonth() + 1) {
                    spinner.setSelection(0);
                    spinner.setEnabled(true);
                } else {
                    spinner.setSelection(1);
                    spinner.setEnabled(false);
                }
            } else {
                spinner.setSelection(0);
                spinner.setEnabled(true);
            }
        } else if (calendarView.getDate() < tomo.withTimeAtStartOfDay().getMillis()) {
            if (deliveryState == 1) {
                spinner.setSelection(1);
                spinner.setEnabled(false);
            } else {
                spinner.setSelection(0);
                spinner.setEnabled(true);
            }
        }
    }

    private void setCalenderLimits() {
        if (deliveryState == 1) {
            calendarView.setMinDate(today.getMillis());
            calendarView.setDate(today.getMillis());
            int date = Integer.parseInt(today.dayOfMonth().getAsString());
            int month = Integer.parseInt(today.monthOfYear().getAsString());
            int year = Integer.parseInt(today.year().getAsString());
            dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);
            //Log.d("dd", "Date selected" + dateSelected);
        } else {
            calendarView.setMinDate(tomo.getMillis());
            calendarView.setDate(tomo.getMillis());
            //dateSelected = String.valueOf((dayOfMonth<10?("0"+dayOfMonth):dayOfMonth) +"-"+(month<10?("0"+month):month)+"-"+year);
            int date = Integer.parseInt(tomo.dayOfMonth().getAsString());
            int month = Integer.parseInt(tomo.monthOfYear().getAsString());
            int year = Integer.parseInt(tomo.year().getAsString());
            dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);
            //Log.d("dd", "Date selected" + dateSelected);
        }
    }

    private void determineDeliveryState() {
        DateTime now = new DateTime();
        int hourOfDay = now.hourOfDay().get();
        if (hourOfDay > 0 && hourOfDay < 16) {
            deliveryState = 1;
        } else if (hourOfDay >= 16 && hourOfDay <= 22) {
            deliveryState = 2;
        } else if (hourOfDay > 22 && hourOfDay < 24) {
            deliveryState = 3;
        }
    }

    private boolean cartContainsWater() {
        for (Cart c : cartList) {
            if (c.getCatId() == 250) return true;
        }
        return false;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOrderDetailsFragmentInteractionListener) {
            mListener = (OnOrderDetailsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderDetailsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnOrderDetailsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onOrderDetailsFragmentInteraction(String date, int deliverySlot);
    }
}
