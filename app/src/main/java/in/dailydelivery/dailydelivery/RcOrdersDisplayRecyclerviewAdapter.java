package in.dailydelivery.dailydelivery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class RcOrdersDisplayRecyclerviewAdapter extends RecyclerView.Adapter<RcOrdersDisplayRecyclerviewAdapter.ViewHolder> {

    List<RcOrderDetails> items;
    private int dayOfWeek;

    public RcOrdersDisplayRecyclerviewAdapter(List<RcOrderDetails> items, int dayOfWeek) {
        this.items = items;
        this.dayOfWeek = dayOfWeek;
    }

    @NonNull
    @Override
    public RcOrdersDisplayRecyclerviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_list, parent, false);
        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView desTV = view.findViewById(R.id.desTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView slotTV = view.findViewById(R.id.deliverySlotTV);
        TextView statusTV = view.findViewById(R.id.statusTV);
        return new RcOrdersDisplayRecyclerviewAdapter.ViewHolder(view, nameTV, desTV, priceTV, slotTV, statusTV);
    }

    @Override
    public void onBindViewHolder(@NonNull RcOrdersDisplayRecyclerviewAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        //get quantity for the day of week
        int qty = 1;
        switch (dayOfWeek) {
            case 1:
                qty = holder.mItem.getMon();
                break;
            case 2:
                qty = holder.mItem.getTue();
                break;
            case 3:
                qty = holder.mItem.getWed();
                break;
            case 4:
                qty = holder.mItem.getThu();
                break;
            case 5:
                qty = holder.mItem.getFri();
                break;
            case 6:
                qty = holder.mItem.getSat();
                break;
            case 7:
                qty = holder.mItem.getSun();
                break;
        }
        int ddPrice = holder.mItem.getPrice() * qty;
        holder.nameTV.setText(holder.mItem.getName() + "(" + qty + " Nos. )");
        holder.desTV.setText(holder.mItem.getDes());
        holder.priceTV.setText("Rs. " + ddPrice);
        if (holder.mItem.getDeliverySlot() == 0) {
            holder.slotTV.setText("Delivery: 5:30AM to 7:30AM");
        } else if (holder.mItem.getDeliverySlot() == 1) {
            holder.slotTV.setText("Delivery: 6 PM to 8 PM");
        }
        String status;
        switch (holder.mItem.getStatus()) {
            case 1:
                status = "Scheduled";
                break;
            case 2:
                status = "Paused (Vacation)";
                break;
            case 3:
                status = "On Hold (Insufficient Credit Balance)";
                break;
            case 4:
                status = "Confirmed";
                break;
            default:
                status = "";
                break;
        }
        status = "Status: " + status;
        holder.statusTV.setText(status);
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
        public final TextView statusTV;

        public ViewHolder(View mView, TextView nameTV, TextView desTV, TextView priceTV, TextView slotTV, TextView statusTV) {
            super(mView);
            this.mView = mView;
            this.nameTV = nameTV;
            this.desTV = desTV;
            this.priceTV = priceTV;
            this.slotTV = slotTV;
            this.statusTV = statusTV;
        }

        public RcOrderDetails mItem;
    }

    public void updateData(List<RcOrderDetails> list, int dayOfWeek) {
        items.clear();
        items = list;
        this.dayOfWeek = dayOfWeek;
        notifyDataSetChanged();
    }
}
