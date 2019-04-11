package in.dailydelivery.dailydelivery.Fragments;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.Fragments.CartDisplayFragment.OnCartDisplayFragmentInteractionListener;
import in.dailydelivery.dailydelivery.R;


public class MycartDisplayRecyclerViewAdapter extends RecyclerView.Adapter<MycartDisplayRecyclerViewAdapter.ViewHolder> {

    private final List<Cart> mValues;
    private final OnCartDisplayFragmentInteractionListener mListener;
    private Context context;
    private onItemRemovedListner monItemRemovedListner;
    private onQtyChangedListner monQtyChangedListner;

    public MycartDisplayRecyclerViewAdapter(List<Cart> items, Context context_, CartDisplayFragment.OnCartDisplayFragmentInteractionListener listener, onItemRemovedListner monItemRemovedListner, onQtyChangedListner monQtyChangedListner) {
        mValues = items;
        mListener = listener;
        context = context_;
        this.monItemRemovedListner = monItemRemovedListner;
        this.monQtyChangedListner = monQtyChangedListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_cartdisplay, parent, false);
        TextView productNameTV = view.findViewById(R.id.productNameTextView);
        TextView productDesTV = view.findViewById(R.id.productDescriptionTextView);
        TextView mrpTV = view.findViewById(R.id.mrpTextView);
        TextView ddPriceTV = view.findViewById(R.id.ddPriceTextView);
        NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        ImageView imageView = view.findViewById(R.id.imageView);


        return new ViewHolder(view, productNameTV, productDesTV, mrpTV, ddPriceTV, numberPicker, imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        int mrp = mValues.get(position).getProductMrp() * mValues.get(position).getProductqty();
        int ddPrice = mValues.get(position).getProductDdprice() * mValues.get(position).getProductqty();
        holder.mItem = mValues.get(position);
        holder.productNameTV.setText(mValues.get(position).getProductName());
        holder.productDesTV.setText(mValues.get(position).getProductDes() + " - " + mValues.get(position).getProductQtyDes());
        holder.mrpTV.setText("Rs. " + String.valueOf(mrp));
        holder.mrpTV.setPaintFlags(holder.mrpTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.ddPriceTV.setText("Rs. " + String.valueOf(ddPrice));
        holder.numberPicker.setValue(holder.mItem.getProductqty());
        holder.numberPicker.setMin(0);
        //Load image using glide
        Glide.with(this.context)
                .load(mValues.get(position).getProductTnUrl())
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);

        holder.numberPicker.setValueChangedListener(new ValueChangedListener() {
            @Override
            public void valueChanged(int value, ActionEnum action) {
                //Log.d("DD","Value changed to " + value);
                if (value == 0) {
                    monItemRemovedListner.onItemRemoved(mValues.get(position).getProductId(), mValues.get(position).getCatId(), position);
                    //mValues.remove(position);
                    notifyDataSetChanged();
                } else {
                    monQtyChangedListner.onQtyChanged(holder.mItem.getProductId(), holder.mItem.getCatId(), value, position);
                    holder.mrpTV.setText("Rs. " + (holder.mItem.getProductMrp() * value));
                    holder.ddPriceTV.setText("Rs. " + (holder.mItem.getProductDdprice() * value));
                    holder.mItem.setProductqty(value);
                    notifyDataSetChanged();
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
        public final TextView mrpTV;
        public final TextView ddPriceTV;
        public final NumberPicker numberPicker;
        public final ImageView imageView;
        public Cart mItem;

        public ViewHolder(View view, TextView productNameTV, TextView productDesTV, TextView mrpTV, TextView ddPriceTV, NumberPicker numberPicker, ImageView imageView) {
            super(view);
            mView = view;
            this.productNameTV = productNameTV;
            this.productDesTV = productDesTV;
            this.mrpTV = mrpTV;
            this.ddPriceTV = ddPriceTV;
            this.numberPicker = numberPicker;
            this.imageView = imageView;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + productDesTV.getText() + "'";
        }
    }
}
