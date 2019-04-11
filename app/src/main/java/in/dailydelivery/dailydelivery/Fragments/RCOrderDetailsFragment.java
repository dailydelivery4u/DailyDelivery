package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.angmarch.views.NiceSpinner;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.Fragments.products.Products;
import in.dailydelivery.dailydelivery.R;


public class RCOrderDetailsFragment extends Fragment {
    NumberPicker[] np;
    ImageView imageView;
    Products.Product product;
    TextView productNameTV;
    TextView productDesTV;
    TextView mrpTV;
    TextView ddPriceTV;
    TextView qtyNumberTv, qtyIncreaseBtn, qtyDecreaseBtn, qtyDay1NumberTV, qtyDay1DecreaseBtn, qtyDay1IncreaseBtn, qtyDay2NumberTV, qtyDay2DecreaseBtn, qtyDay2IncreaseBtn;
    TextView dailyTV, oncein2DaysTV, onceinMonthTV;
    //TextView deliverySlotTV, dsChangeTV;
    TextView startDateTV, sdChangeTV;
    Button placeRCOrderBtn;
    int minOrderDays, selectedFreq;
    RelativeLayout startDateRL, qtyMonthlyRL;
    LinearLayout dailyFrequencyLL, alternateDayFreqSelLL;
    AppCompatImageView questionMark;

    List<String> datesForSpinner;
    NiceSpinner datesSpinner;

    //    int deliverySlot = 0;
    //   int selectedSlot;
    int dateSelected = 0;

    String[] dates;
    DateTimeFormatter dtf, displayDTF;
    View.OnClickListener freqChangeOnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetSelected();
            switch (v.getId()) {
                case R.id.dailyTV:
                    selectedFreq = 1;
                    questionMark.setVisibility(View.GONE);
                    qtyMonthlyRL.setVisibility(View.GONE);
                    alternateDayFreqSelLL.setVisibility(View.GONE);
                    dailyFrequencyLL.setVisibility(View.VISIBLE);
                    break;
                case R.id.oncein2daysTV:
                    selectedFreq = 2;
                    questionMark.setVisibility(View.VISIBLE);
                    dailyFrequencyLL.setVisibility(View.GONE);
                    qtyMonthlyRL.setVisibility(View.GONE);
                    alternateDayFreqSelLL.setVisibility(View.VISIBLE);
                    break;
                /*case R.id.oncein3daysTV:
                    selectedFreq = 3;
                    break;
                case R.id.weeklyTV:
                    selectedFreq=4;
                    weekdaySelectionLL.setVisibility(View.VISIBLE);
                    break;
                case R.id.oncein2WeeksTV:
                    selectedFreq=5;
                    weekdaySelectionLL.setVisibility(View.VISIBLE);
                    break;*/
                case R.id.onceinMonthTV:
                    selectedFreq = 3;
                    questionMark.setVisibility(View.GONE);
                    alternateDayFreqSelLL.setVisibility(View.GONE);
                    dailyFrequencyLL.setVisibility(View.GONE);
                    qtyMonthlyRL.setVisibility(View.VISIBLE);
                    break;
            }
            highlightSelected();
        }
    };
    private RCOrderDetailsFragmentInteractionListener mListener;

    public RCOrderDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle b = getArguments();
            product = (Products.Product) b.getSerializable("product");
        }
        DateTime now = new DateTime();
        int hourOfDay = now.hourOfDay().get();
        if (hourOfDay >= 22 && hourOfDay < 24) {
            minOrderDays = 2;
        } else {
            minOrderDays = 1;
        }

        dates = new String[7];
        datesForSpinner = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            datesForSpinner.add(String.valueOf(i));
        }

        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        displayDTF = DateTimeFormat.fullDate();
        DateTime date = new DateTime().plusDays(minOrderDays);
        for (int i = 0; i < 7; i++) {
            dates[i] = date.toString(displayDTF);
            date = date.plusDays(1);
        }
    }

    /*WeekdaysDataSource.Callback weekDaysChangedCallBack = new WeekdaysDataSource.Callback() {
        @Override
        public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {

        }

        @Override
        public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {

        }
    };
*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RCOrderDetailsFragmentInteractionListener) {
            mListener = (RCOrderDetailsFragmentInteractionListener) context;
        } /*else {
            throw new RuntimeException(context.toString()
                    + " must implement RCOrderDetailsFragmentInteractionListener");
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rcorder_details, container, false);

        imageView = view.findViewById(R.id.imageView);
        productNameTV = view.findViewById(R.id.productNameTextView);
        productDesTV = view.findViewById(R.id.productDescriptionTextView);
        mrpTV = view.findViewById(R.id.mrpTextView);
        ddPriceTV = view.findViewById(R.id.ddPriceTextView);
        placeRCOrderBtn = view.findViewById(R.id.placeRcOrdBtn);
        //deliverySlotTV = view.findViewById(R.id.deliverySlotDisplayTV);
        startDateTV = view.findViewById(R.id.startDateDisplayTV);
        sdChangeTV = view.findViewById(R.id.sdChangeTV);
        //dsChangeTV = view.findViewById(R.id.dsChangeTV);

        qtyNumberTv = view.findViewById(R.id.qtyNumberTV);
        qtyIncreaseBtn = view.findViewById(R.id.qtyIncreaseBtn);
        qtyDecreaseBtn = view.findViewById(R.id.qtyDecreaseBtn);

        qtyDay1NumberTV = view.findViewById(R.id.qtyDay1NumberTV);
        qtyDay1IncreaseBtn = view.findViewById(R.id.qtyDay1IncreaseBtn);
        qtyDay1DecreaseBtn = view.findViewById(R.id.qtyDay1DecreaseBtn);

        qtyDay2NumberTV = view.findViewById(R.id.qtyDay2NumberTV);
        qtyDay2IncreaseBtn = view.findViewById(R.id.qtyDay2IncreaseBtn);
        qtyDay2DecreaseBtn = view.findViewById(R.id.qtyDay2DecreaseBtn);

        setQtyChangers();
        //Frequency Views
        dailyTV = view.findViewById(R.id.dailyTV);
        oncein2DaysTV = view.findViewById(R.id.oncein2daysTV);
        onceinMonthTV = view.findViewById(R.id.onceinMonthTV);
        startDateRL = view.findViewById(R.id.startDateRL);
        dailyFrequencyLL = view.findViewById(R.id.dailyFrequencyLL);
        qtyMonthlyRL = view.findViewById(R.id.qtyMonthlyRL);
        alternateDayFreqSelLL = view.findViewById(R.id.alternateDayFreqSelLL);
        questionMark = view.findViewById(R.id.questionMarkIV);
        selectedFreq = 1;

        dailyTV.setOnClickListener(freqChangeOnClickListner);
        oncein2DaysTV.setOnClickListener(freqChangeOnClickListner);
        onceinMonthTV.setOnClickListener(freqChangeOnClickListner);


        np = new NumberPicker[7];
        np[0] = view.findViewById(R.id.np0);
        np[1] = view.findViewById(R.id.np1);
        np[2] = view.findViewById(R.id.np2);
        np[3] = view.findViewById(R.id.np3);
        np[4] = view.findViewById(R.id.np4);
        np[5] = view.findViewById(R.id.np5);
        np[6] = view.findViewById(R.id.np6);

        //Set Values for all views
        for (int i = 0; i < 7; i++) {
            np[i].setMinValue(0);
            np[i].setMaxValue(10);
            np[i].setValue(1);
        }
        productNameTV.setText(product.getProductName());
        productDesTV.setText(product.getProductDes() + " - " + product.getProductQty());
        mrpTV.setText("Mrp: Rs." + String.valueOf(product.getMrp()));
        mrpTV.setPaintFlags(mrpTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        ddPriceTV.setText("DD Price: Rs." + String.valueOf(product.getDdPrice()));
        startDateTV.setText(new DateTime().plusDays(minOrderDays).toString(displayDTF));
        Glide.with(getActivity())
                .load(product.getThumbnailUrl())
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        //On Click Listners
        placeRCOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day1Qty = 0;
                if (selectedFreq == 2) {
                    day1Qty = Integer.parseInt(qtyDay1NumberTV.getText().toString());
                } else if (selectedFreq == 3) {
                    day1Qty = Integer.parseInt(qtyNumberTv.getText().toString());
                }
                mListener.rcOrderDetailsFragmentInteraction(new RcOrderDetails(product.getId(), product.getCat_id(), product.getProductName(), product.getProductDes(), product.getProductQty(), product.getDdPrice(), 1, product.getDeliverySlot(), new DateTime().plusDays(dateSelected).plusDays(minOrderDays).toString(dtf), np[0].getValue(), np[1].getValue(), np[2].getValue(), np[3].getValue(), np[4].getValue(), np[5].getValue(), np[6].getValue(), selectedFreq, day1Qty, Integer.parseInt(qtyDay2NumberTV.getText().toString()), (datesSpinner.getSelectedIndex() + 1)));
            }
        });
        /*dsChangeTV.setEnabled(false);
        if (product.getDeliverySlot() == 1) {
            deliverySlotTV.setText(R.string.delivery_slot_1);
        } else {
            deliverySlotTV.setText(R.string.delivery_slot_2);
        }*/
        /*
        dsChangeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle("Choose Delivery Slot");
                selectedSlot = deliverySlot;
                builder.setSingleChoiceItems(R.array.delivery_slots, selectedSlot, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSlot = which;
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deliverySlot = selectedSlot;
                        if (deliverySlot == 1) {
                            deliverySlotTV.setText(R.string.delivery_slot_0);
                        } else {
                            deliverySlotTV.setText(R.string.delivery_slot_1);
                        }
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mDialog = builder.create();
                mDialog.show();
            }
        });*/
        datesSpinner = view.findViewById(R.id.date_spinner);
        datesSpinner.attachDataSource(datesForSpinner);
        //datesSpinner.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorBrown));
        datesSpinner.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorDefaultBg));
        datesSpinner.setArrowTintColor(ContextCompat.getColor(getActivity(), R.color.colorBrown));
        sdChangeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle("Choose Start Date");
                builder.setSingleChoiceItems(dates, dateSelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dateSelected = which;
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DateTime dt = new DateTime().plusDays(dateSelected).plusDays(minOrderDays);
                        startDateTV.setText(dt.toString(displayDTF));
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mDialog = builder.create();
                mDialog.show();
            }
        });
        mListener.setActionBarTitle("Order Details");

        questionMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle("Alternate Day Delivery");
                builder.setMessage("If Start Date is Mar 27,\nthe delivery quantites will be:\nMar 27 -> First Day Qty\nMar 28 -> Next Day Qty\nMar 29 -> First Day Qty\nMar 30 -> Next Day Qty\nAnd so on...");
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mDialog = builder.create();
                mDialog.show();
            }
        });
        datesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(),"Value: " + position,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*WeekdaysDataSource wds = new WeekdaysDataSource((AppCompatActivity) getActivity(), R.id.weekdays_sample_2)
                .start(weekDaysChangedCallBack);*/
        /*WeekdaysDataSource wds = new WeekdaysDataSource((AppCompatActivity) getActivity(), R.id.weekdays_sample_2)
                .setDrawableType(WeekdaysDrawableProvider.MW_ROUND_RECT)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setSelectedDays(Calendar.MONDAY)
                .setFontBaseSize(16)
                .setFontTypeFace(Typeface.MONOSPACE)
                .setNumberOfLetters(3)
                .setSelectedColorRes(R.color.colorPink)
                .setTextColorSelectedRes(R.color.colorWhite)
                .setUnselectedColorRes(R.color.colorGray)
                .setTextColorUnselectedRes(R.color.colorBlack)
                .start(weekDaysChangedCallBack);*/
    }

    private void highlightSelected() {
        switch (selectedFreq) {
            case 1:
                dailyTV.setBackgroundResource(R.drawable.freq_selector_selected_bg);
                dailyTV.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 2:
                oncein2DaysTV.setBackgroundResource(R.drawable.freq_selector_selected_bg);
                oncein2DaysTV.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 3:
                onceinMonthTV.setBackgroundResource(R.drawable.freq_selector_selected_bg);
                onceinMonthTV.setTextColor(Color.parseColor("#FFFFFF"));
                break;
        }
    }

    private void resetSelected() {
        switch (selectedFreq) {
            case 1:
                dailyTV.setBackgroundResource(R.drawable.freq_selector_default_bg);
                dailyTV.setTextColor(Color.parseColor("#000000"));
                break;
            case 2:
                oncein2DaysTV.setBackgroundResource(R.drawable.freq_selector_default_bg);
                oncein2DaysTV.setTextColor(Color.parseColor("#000000"));
                break;
            case 3:
                onceinMonthTV.setBackgroundResource(R.drawable.freq_selector_default_bg);
                onceinMonthTV.setTextColor(Color.parseColor("#000000"));
                break;
        }
    }

    private void setQtyChangers() {
        qtyIncreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyNumberTv.getText().toString());
                if (val < 10)
                    qtyNumberTv.setText(String.valueOf(++val));
            }
        });
        qtyDecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyNumberTv.getText().toString());
                if (val > 1)
                    qtyNumberTv.setText(String.valueOf(--val));

            }
        });

        qtyDay1IncreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyDay1NumberTV.getText().toString());
                if (val < 10)
                    qtyDay1NumberTV.setText(String.valueOf(++val));
            }
        });
        qtyDay1DecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyDay1NumberTV.getText().toString());
                if (val > 0)
                    qtyDay1NumberTV.setText(String.valueOf(--val));

            }
        });

        qtyDay2IncreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyDay2NumberTV.getText().toString());
                if (val < 10)
                    qtyDay2NumberTV.setText(String.valueOf(++val));
            }
        });
        qtyDay2DecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(qtyDay2NumberTV.getText().toString());
                if (val > 0)
                    qtyDay2NumberTV.setText(String.valueOf(--val));
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface RCOrderDetailsFragmentInteractionListener {
        void rcOrderDetailsFragmentInteraction(RcOrderDetails rcOrderDetails);

        void setActionBarTitle(String title);
    }
}
