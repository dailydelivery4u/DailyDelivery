package in.dailydelivery.dailydelivery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.Vacation;

public class VacationsRVAdapter extends RecyclerView.Adapter<VacationsRVAdapter.ViewHolder> {


    List<Vacation> items;
    DateTimeFormatter dtf, dtf_display;
    VacationAdapterInterface mInterface;


    public VacationsRVAdapter(List<Vacation> items, VacationAdapterInterface mInterface) {
        this.items = items;
        this.mInterface = mInterface;
    }

    @NonNull
    @Override
    public VacationsRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vacation_list, parent, false);
        TextView dateTV = view.findViewById(R.id.vacationDatesTV);
        ImageView delBtn = view.findViewById(R.id.delBtn);
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        dtf_display = DateTimeFormat.mediumDate();
        return new ViewHolder(view, dateTV, delBtn);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        DateTime d1 = dtf.parseDateTime(holder.mItem.getStartDate());
        DateTime d2 = dtf.parseDateTime(holder.mItem.getEndDate());

        String vac = d1.toString(dtf_display) + " to " + d2.toString(dtf_display);
        holder.dateTV.setText(vac);
        if (d1.isAfterNow()) {
            holder.delBtn.setVisibility(View.VISIBLE);
            holder.delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInterface.onVacDel(holder.mItem.getVacId());
                }
            });
        } else {
            holder.delBtn.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface VacationAdapterInterface {
        void onVacDel(int vacId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView dateTV;
        public final ImageView delBtn;
        public Vacation mItem;

        public ViewHolder(@NonNull View itemView, TextView dateTV, ImageView delBtn) {
            super(itemView);
            this.mView = itemView;
            this.dateTV = dateTV;
            this.delBtn = delBtn;
        }
    }
}
