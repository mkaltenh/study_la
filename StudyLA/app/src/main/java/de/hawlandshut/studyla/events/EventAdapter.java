package de.hawlandshut.studyla.events;

/**
* AdapterKlasse EventAdapter
* Wird zur Darstellung der Recyclerview verwendet
* Regelt die Darstellung der einzelnen Event-Elemente
* @Fragment: EventFragment
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {

    private ArrayList<Event> eventList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name) TextView mName;
        @BindView(R.id.date) TextView mDate;
        @BindView(R.id.profile_picture) ImageView mPicture;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public EventAdapter(ArrayList<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.mName.setText(event.getName());

        //Get Date and prepare Calendar
        Date today = new Date();
        Date startDate = event.getDate();
        Date endDate = event.getEndDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        //Set Date Text and handle special cases
        SimpleDateFormat sdf = new SimpleDateFormat( "EEE dd.MM.yyyy", Locale.GERMAN);
        SimpleDateFormat sdfOngoing = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        if((startDate.equals(today) && endDate.after(today)) || (startDate.before(today) && endDate.after(today))){
            Context context = holder.mDate.getContext();
            String ongoing = context.getResources().getString(R.string.event_ongoing) + sdfOngoing.format(endDate);
            holder.mDate.setText(ongoing);
        }
        else if(startDate.equals(today)){
            holder.mDate.setText(R.string.event_today);
        }
        else if(DateUtils.isToday(startDate.getTime() - (1000 * 60 * 60 * 24))){
            holder.mDate.setText(R.string.event_tomorrow);
        }
        else if(endDate != null){
            String fullDate = sdf.format(startDate) + " - " + sdf.format(endDate);
            holder.mDate.setText(fullDate);
        }
        else{
            holder.mDate.setText(sdf.format(startDate));
        }

        //Set Profile Picture of FB Group
        if(event.getPicUrl() != null){
            Context context = holder.mPicture.getContext();
            Picasso.with(context).load(event.getPicUrl()).into(holder.mPicture);}
        else{
            holder.mPicture.setImageResource(R.drawable.logo_hawlandshut);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
