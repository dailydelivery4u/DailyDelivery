package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.R;

//import android.util.Log;

public class OrderDetailsFragment extends Fragment {

    public static List<Cart> cartList;
    //private Spinner spinner;
    TextView deliverySlotTV;
    DateTime today = new DateTime();
    DateTime tomo = today.plusDays(1);
    DateTime dayAfterTom = today.plusDays(2);
    Button placeOrderBtn;
    String dateSelected;
    int delivery_slot;
    private OnOrderDetailsFragmentInteractionListener mListener;
    private int deliveryState;
    private CalendarView calendarView;

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
       /* spinner = view.findViewById(R.id.delivery_time_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.delivery_slots, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);*/
        deliverySlotTV = view.findViewById(R.id.deliverySlotTV);
        calendarView = view.findViewById(R.id.calendarView);
        placeOrderBtn = view.findViewById(R.id.placeOrderBtn);
        delivery_slot = cartList.get(0).getDelivery_slot();
        determineDeliveryState();
        setCalenderLimits();
        setTimeSlot(new DateTime().withMillis(calendarView.getDate()).getDayOfMonth());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                //setTimeSlot(dayOfMonth);
                int month_n = month + 1;
                dateSelected = String.valueOf((dayOfMonth < 10 ? ("0" + dayOfMonth) : dayOfMonth) + "-" + (month_n < 10 ? ("0" + month_n) : month_n) + "-" + year);
                //Toast.makeText(getActivity(),"Date: " + dateSelected,Toast.LENGTH_SHORT).show();

            }
        });
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrderBtn.setEnabled(false);
                mListener.onOrderDetailsFragmentInteraction(dateSelected, delivery_slot);
            }
        });
        mListener.setActionBarTitle("Order Details");
        return view;
    }

    private void setTimeSlot(int day) {
        /*if (day >= tomo.getDayOfMonth()) {
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
        spinner.setSelection(delivery_slot - 1);
        spinner.setEnabled(false);*/
        if (delivery_slot == 1) {
            deliverySlotTV.setText(getText(R.string.delivery_slot_1));
        } else if (delivery_slot == 2) {
            deliverySlotTV.setText(getText(R.string.delivery_slot_2));
        }
    }

    private void setCalenderLimits() {
        if (delivery_slot == 1) {
            //Milk order - Min delivery tomorrow if before 10 PM
            if (deliveryState == 1 || deliveryState == 2) {
                calendarView.setMinDate(tomo.getMillis());
                calendarView.setDate(tomo.getMillis());
                //dateSelected = String.valueOf((dayOfMonth<10?("0"+dayOfMonth):dayOfMonth) +"-"+(month<10?("0"+month):month)+"-"+year);
                int date = Integer.parseInt(tomo.dayOfMonth().getAsString());
                int month = Integer.parseInt(tomo.monthOfYear().getAsString());
                int year = Integer.parseInt(tomo.year().getAsString());
                dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);

            } else if (deliveryState == 3) {
                //Order cannot be placed for tomo. should be placed only for day after tomo
                calendarView.setMinDate(dayAfterTom.getMillis());
                calendarView.setDate(dayAfterTom.getMillis());
                //dateSelected = String.valueOf((dayOfMonth<10?("0"+dayOfMonth):dayOfMonth) +"-"+(month<10?("0"+month):month)+"-"+year);
                int date = Integer.parseInt(dayAfterTom.dayOfMonth().getAsString());
                int month = Integer.parseInt(dayAfterTom.monthOfYear().getAsString());
                int year = Integer.parseInt(dayAfterTom.year().getAsString());
                dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);
            }
        } else if (delivery_slot == 2) {
            //water order
            if (deliveryState == 1) {
                calendarView.setMinDate(today.getMillis());
                calendarView.setDate(today.getMillis());
                //dateSelected = String.valueOf((dayOfMonth<10?("0"+dayOfMonth):dayOfMonth) +"-"+(month<10?("0"+month):month)+"-"+year);
                int date = Integer.parseInt(today.dayOfMonth().getAsString());
                int month = Integer.parseInt(today.monthOfYear().getAsString());
                int year = Integer.parseInt(today.year().getAsString());
                dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);

            } else if (deliveryState == 3 || deliveryState == 2) {
                //Order cannot be placed for tomo. should be placed only for day after tomo
                calendarView.setMinDate(tomo.getMillis());
                calendarView.setDate(tomo.getMillis());
                //dateSelected = String.valueOf((dayOfMonth<10?("0"+dayOfMonth):dayOfMonth) +"-"+(month<10?("0"+month):month)+"-"+year);
                int date = Integer.parseInt(tomo.dayOfMonth().getAsString());
                int month = Integer.parseInt(tomo.monthOfYear().getAsString());
                int year = Integer.parseInt(tomo.year().getAsString());
                dateSelected = String.valueOf((date < 10 ? ("0" + date) : date) + "-" + (month < 10 ? ("0" + month) : month) + "-" + year);
            }
            //Log.d("DD", "Date selected: " + dateSelected);
        }
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
        void onOrderDetailsFragmentInteraction(String date, int deliverySlot);

        void setActionBarTitle(String title);

    }
}
