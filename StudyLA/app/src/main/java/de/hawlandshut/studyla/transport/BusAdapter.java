package de.hawlandshut.studyla.transport;

/*
* AdapterKlasse BusAdapter
* Wird zur Darstellung der Recyclerview verwendet
* Regelt die Darstellung der einzelnen BusLine-Elemente
* @Fragment: TransportFragment
 */

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hawlandshut.studyla.R;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.MyViewHolder> {

    private ArrayList<BusLine> busList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.destination) TextView mDestination;
        @BindView(R.id.line) TextView mLine;
        @BindView(R.id.bus_icon) ImageView mPicture;
        @BindView(R.id.bus_root_view) RelativeLayout mRootLayout;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public BusAdapter(ArrayList<BusLine> busList) {
        this.busList = busList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BusLine busline = busList.get(position);
        holder.mDestination.setText("[" + busline.getTime() + "]  " + busline.getDestination());
        holder.mLine.setText("Linie " + busline.getLine());

        //Set Bus Picture
        if(busline.getType().equals("Stadtbus")){
            holder.mPicture.setImageResource(R.drawable.transport_stadtbus);
            }
        else if(busline.getType().equals("Regionalbus")){
            holder.mPicture.setImageResource(R.drawable.transport_regionalbus);
        }
        else{
            holder.mPicture.setImageResource(R.drawable.logo_hawlandshut);
        }

        //Background color
        if (position%2 == 0) {
            holder.mRootLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else {
            holder.mRootLayout.setBackgroundColor(Color.parseColor("#ECF3FA"));
        }

    }

    @Override
    public int getItemCount() {
        return busList.size();
    }
}
