package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
    TextView deliverySlotTV, dsChangeTV;
    TextView startDateTV, sdChangeTV;
    Button placeRCOrderBtn;
    int minOrderDays;

    //    int deliverySlot = 0;
    //   int selectedSlot;
    int dateSelected = 0;

    String[] dates;
    DateTimeFormatter dtf;


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

        dates = new String[5];
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        DateTime date = new DateTime().plusDays(minOrderDays);
        for (int i = 0; i < 5; i++) {
            dates[i] = date.toString(dtf);
            date = date.plusDays(1);
        }

    }

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
        deliverySlotTV = view.findViewById(R.id.deliverySlotDisplayTV);
        startDateTV = view.findViewById(R.id.startDateDisplayTV);
        sdChangeTV = view.findViewById(R.id.sdChangeTV);
        dsChangeTV = view.findViewById(R.id.dsChangeTV);

        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");

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
        productDesTV.setText(product.getProductDes());
        mrpTV.setText("Mrp: Rs." + String.valueOf(product.getMrp()));
        mrpTV.setPaintFlags(mrpTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        ddPriceTV.setText("DD Price: Rs." + String.valueOf(product.getDdPrice()));
        startDateTV.setText(new DateTime().plusDays(minOrderDays).toString(dtf));
        Glide.with(getActivity())
                .load(product.getThumbnailUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        //On Click Listners
        placeRCOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.rcOrderDetailsFragmentInteraction(new RcOrderDetails(product.getId(), product.getCat_id(), product.getProductName(), product.getProductDes(), product.getDdPrice(), 1, product.getDeliverySlot(), new DateTime().plusDays(dateSelected).plusDays(minOrderDays).toString(dtf), np[0].getValue(), np[1].getValue(), np[2].getValue(), np[3].getValue(), np[4].getValue(), np[5].getValue(), np[6].getValue()));
            }
        });
        dsChangeTV.setEnabled(false);
        if (product.getDeliverySlot() == 1) {
            deliverySlotTV.setText(R.string.delivery_slot_1);
        } else {
            deliverySlotTV.setText(R.string.delivery_slot_2);
        }/*
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
                        startDateTV.setText(dt.toString(dtf));
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
        return view;
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
