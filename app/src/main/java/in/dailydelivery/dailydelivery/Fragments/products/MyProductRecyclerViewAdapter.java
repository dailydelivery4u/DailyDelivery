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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.List;

import in.dailydelivery.dailydelivery.Fragments.products.ProductDisplayFragment.ProductDisplayFragmentInteractionListener;
import in.dailydelivery.dailydelivery.Fragments.products.Products.Product;
import in.dailydelivery.dailydelivery.R;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Product} and makes a call to the
 * specified {@link ProductDisplayFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyProductRecyclerViewAdapter extends RecyclerView.Adapter<MyProductRecyclerViewAdapter.ViewHolder> {

    private final List<Product> mValues;
    private final ProductDisplayFragmentInteractionListener mListener;
    private Context context;

    public MyProductRecyclerViewAdapter(List<Product> items, Context context_, ProductDisplayFragmentInteractionListener listener) {
        mValues = items;
        context = context_;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false);

        TextView productNameTV = view.findViewById(R.id.productNameTextView);
        TextView productDesTV = view.findViewById(R.id.productDescriptionTextView);
        TextView mrpTV = view.findViewById(R.id.mrpTextView);
        TextView ddPriceTV = view.findViewById(R.id.ddPriceTextView);
        Button addBtn = view.findViewById(R.id.addBtn);
        NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        TextView qtyTV = view.findViewById(R.id.qtyTV);
        LinearLayoutCompat qtyLinLay = view.findViewById(R.id.qtyLinLay);
        ImageView imageView = view.findViewById(R.id.imageView);
        return new ViewHolder(view, productNameTV, productDesTV, mrpTV, ddPriceTV, addBtn, numberPicker, qtyTV, qtyLinLay, imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.productNameTV.setText(mValues.get(position).getProductName());
        holder.productDesTV.setText(mValues.get(position).getProductDes());
        holder.mrpTV.setText("MRP: " + String.valueOf(mValues.get(position).getMrp()));
        holder.mrpTV.setPaintFlags(holder.mrpTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.ddPriceTV.setText("DD Price: " + String.valueOf(mValues.get(position).getDdPrice()));

        //Load image using glide
        Glide.with(this.context)
                .load(mValues.get(position).getThumbnailUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);

        if (holder.mItem.isIn_cart()) {
            holder.addBtn.setText("ADDED");
            holder.numberPicker.setValue(holder.mItem.getQty());
            holder.addBtn.setEnabled(false);
            holder.qtyLinLay.setVisibility(View.VISIBLE);
        }

        holder.numberPicker.setValueChangedListener(new ValueChangedListener() {
            @Override
            public void valueChanged(int value, ActionEnum action) {
                //Log.d("DD","Value changed to " + value);
                if (value == 0) {
                    // Remove item from cart
                    holder.qtyLinLay.setVisibility(View.GONE);
                    holder.addBtn.setEnabled(true);
                    holder.addBtn.setText("ADD");
                }
                mListener.productDisplayFragmentInteraction(holder.mItem, value);
                holder.mItem.setQty(value);
                notifyDataSetChanged();
            }
        });

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.productDisplayFragmentInteraction(holder.mItem, 1);
                    holder.addBtn.setText("ADDED");
                    holder.numberPicker.setValue(1);
                    holder.addBtn.setEnabled(false);
                    holder.qtyLinLay.setVisibility(View.VISIBLE);
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
        public final TextView productNameTV;
        public final TextView productDesTV;
        public final TextView qtyTV;
        public final TextView mrpTV;
        public final TextView ddPriceTV;
        public final Button addBtn;
        public final NumberPicker numberPicker;
        public final LinearLayoutCompat qtyLinLay;
        public final ImageView imageView;

        public Product mItem;

        public ViewHolder(View view, TextView productNameTV, TextView productDesTV, TextView mrpTV, TextView ddPriceTV, Button addBtn, NumberPicker numberPicker, TextView addedToCartTV, LinearLayoutCompat qtyLinLay, ImageView imageView) {
            super(view);
            mView = view;
            this.productNameTV = productNameTV;
            this.productDesTV = productDesTV;
            this.mrpTV = mrpTV;
            this.ddPriceTV = ddPriceTV;
            this.addBtn = addBtn;
            this.numberPicker = numberPicker;
            this.qtyTV = addedToCartTV;
            this.qtyLinLay = qtyLinLay;
            this.imageView = imageView;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + productNameTV.getText() + "'";
        }
    }
}
