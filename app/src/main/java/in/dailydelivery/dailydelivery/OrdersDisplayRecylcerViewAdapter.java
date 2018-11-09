package in.dailydelivery.dailydelivery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;

public class OrdersDisplayRecylcerViewAdapter extends RecyclerView.Adapter<OrdersDisplayRecylcerViewAdapter.ViewHolder> {
    List<OneTimeOrderDetails> items;

    public OrdersDisplayRecylcerViewAdapter(List<OneTimeOrderDetails> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrdersDisplayRecylcerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_list, parent, false);
        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView desTV = view.findViewById(R.id.desTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView slotTV = view.findViewById(R.id.deliverySlotTV);
        return new ViewHolder(view, nameTV, desTV, priceTV, slotTV);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersDisplayRecylcerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        int ddPrice = items.get(position).getPrice() * items.get(position).getQty();
        holder.nameTV.setText(items.get(position).getName() + "(" + items.get(position).getQty() + " Nos. )");
        holder.desTV.setText(items.get(position).getDes());
        holder.priceTV.setText("Rs. " + ddPrice);
        if (items.get(position).getDeliverySlot() == 1) {
            holder.slotTV.setText("Delivery: 5:30AM to 7:30AM");
        } else if (items.get(position).getDeliverySlot() == 2) {
            holder.slotTV.setText("Delivery: 6 PM to 8 PM");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameTV;
        public final TextView desTV;
        public final TextView priceTV;
        public final TextView slotTV;

        public ViewHolder(View mView, TextView nameTV, TextView desTV, TextView priceTV, TextView slotTV) {
            super(mView);
            this.mView = mView;
            this.nameTV = nameTV;
            this.desTV = desTV;
            this.priceTV = priceTV;
            this.slotTV = slotTV;
        }

        public OneTimeOrderDetails mItem;
    }

    public void updateData(List<OneTimeOrderDetails> list) {
        items.clear();
        items = list;

        notifyDataSetChanged();
    }
}
