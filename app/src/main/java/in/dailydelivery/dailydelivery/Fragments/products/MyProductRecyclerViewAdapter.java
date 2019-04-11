package in.dailydelivery.dailydelivery.Fragments.products;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.List;

import in.dailydelivery.dailydelivery.Fragments.products.ProductDisplayFragment.ProductDisplayFragmentInteractionListener;
import in.dailydelivery.dailydelivery.Fragments.products.Products.Product;
import in.dailydelivery.dailydelivery.R;


public class MyProductRecyclerViewAdapter extends RecyclerView.Adapter<MyProductRecyclerViewAdapter.ViewHolder> {

    private final List<Product> mValues;
    private final ProductDisplayFragmentInteractionListener mListener;
    private Context context;
    private int deliverySlotInCart;

    public MyProductRecyclerViewAdapter(List<Product> items, Context context_, ProductDisplayFragmentInteractionListener listener, int deliverySlotInCart) {
        mValues = items;
        context = context_;
        mListener = listener;
        this.deliverySlotInCart = deliverySlotInCart;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false);

        TextView productNameTV = view.findViewById(R.id.productNameTextView);
        TextView productDesTV = view.findViewById(R.id.productDescriptionTextView);
        TextView productQtyTv = view.findViewById(R.id.productQtyTextView);
        TextView mrpTV = view.findViewById(R.id.mrpTextView);
        TextView ddPriceTV = view.findViewById(R.id.ddPriceTextView);
        Button addBtn = view.findViewById(R.id.addBtn);
        Button rptBtn = view.findViewById(R.id.repeatBtn);
        NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        TextView qtyTV = view.findViewById(R.id.qtyTV);
        LinearLayoutCompat qtyLinLay = view.findViewById(R.id.qtyLinLay);
        ImageView imageView = view.findViewById(R.id.imageView);
        return new ViewHolder(view, productNameTV, productDesTV, productQtyTv, mrpTV, ddPriceTV, addBtn, numberPicker, qtyTV, rptBtn, qtyLinLay, imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.productNameTV.setText(mValues.get(position).getProductName());
        holder.productDesTV.setText(mValues.get(position).getProductDes());
        holder.productQtyTv.setText(mValues.get(position).getProductQty());
        holder.mrpTV.setText("MRP: " + String.valueOf(mValues.get(position).getMrp()));
        holder.mrpTV.setPaintFlags(holder.mrpTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.ddPriceTV.setText("DD Price: " + String.valueOf(mValues.get(position).getDdPrice()));

        //Load image using glide
        Glide.with(this.context)
                .load(mValues.get(position).getThumbnailUrl())
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        if (holder.mItem.isIn_cart()) {
            holder.addBtn.setVisibility(View.GONE);
            holder.numberPicker.setValue(holder.mItem.getQty());
            holder.addBtn.setEnabled(false);
            holder.qtyLinLay.setVisibility(View.VISIBLE);
        }


        holder.numberPicker.setValueChangedListener(new ValueChangedListener() {
            @Override
            public void valueChanged(int value, ActionEnum action) {
                //Log.d("DD","Value changed to " + value);
                if (value == 0) {
                    //Log.d("DD","Entering If condition ");
                    // Remove item from cart
                    holder.qtyLinLay.setVisibility(View.GONE);
                    holder.addBtn.setEnabled(true);
                    holder.addBtn.setVisibility(View.VISIBLE);
                }
                mListener.productDisplayFragmentInteraction(holder.mItem, value);
                holder.mItem.setQty(value);
                //notifyDataSetChanged();
            }
        });

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("DD","delivery slot in cart: " + deliverySlotInCart + "; item delivery slot: "+ holder.mItem.getDeliverySlot());
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                //if (deliverySlotInCart == 0 || deliverySlotInCart == holder.mItem.getDeliverySlot()) {
                mListener.productDisplayFragmentInteraction(holder.mItem, 1);
                holder.addBtn.setEnabled(false);
                holder.addBtn.setVisibility(View.GONE);
                holder.numberPicker.setValue(1);
                holder.qtyLinLay.setVisibility(View.VISIBLE);
                    /*} else {
                        //show the user status with an alert dailogue
                        String message;
                        if (deliverySlotInCart == 1) {
                            //Milk in cart
                            message = "Milk already in Cart!Water cannot be added due to different delivery timings.\n" +
                                    "Complete your Milk order first and order water seperately again";
                        } else {
                            //Water in cart
                            message = "Water already in Cart!Milk cannot be added due to different delivery timings.\n" +
                                    "Complete your Water order first and order milk seperately again";
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Cannot Add Product!!")
                                .setMessage(message);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        //Toast.makeText(context,"Delivery Slots Mismatch for Product and Products in Cart.")

                    //mListener.productDisplayFragmentInteraction(holder.mItem, 1);
                }*/
            }
        });

        holder.rptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.repeatBtnClicked(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView productNameTV;
        public final TextView productDesTV;
        public final TextView productQtyTv;
        public final TextView qtyTV;
        public final TextView mrpTV;
        public final TextView ddPriceTV;
        public final Button addBtn;
        public final Button rptBtn;
        public final NumberPicker numberPicker;
        public final LinearLayoutCompat qtyLinLay;
        public final ImageView imageView;

        public Product mItem;

        public ViewHolder(View view, TextView productNameTV, TextView productDesTV, TextView productQtyTv, TextView mrpTV, TextView ddPriceTV, Button addBtn, NumberPicker numberPicker, TextView addedToCartTV, Button rptBtn, LinearLayoutCompat qtyLinLay, ImageView imageView) {
            super(view);
            mView = view;
            this.productNameTV = productNameTV;
            this.productDesTV = productDesTV;
            this.productQtyTv = productQtyTv;
            this.mrpTV = mrpTV;
            this.ddPriceTV = ddPriceTV;
            this.addBtn = addBtn;
            this.numberPicker = numberPicker;
            this.qtyTV = addedToCartTV;
            this.rptBtn = rptBtn;
            this.qtyLinLay = qtyLinLay;
            this.imageView = imageView;
        }

    }
}
