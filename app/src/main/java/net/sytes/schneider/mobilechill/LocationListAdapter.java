package net.sytes.schneider.mobilechill;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.sytes.schneider.mobilechill.database.LocationEntity;

import java.util.List;

/**
 * Created by Timo Hasenbichler on 02.01.2018.
 */

public class LocationListAdapter extends ArrayAdapter<LocationEntity> {

    private List<LocationEntity> locationEntityList;
    private int layoutResourceId;
    private Context context;
    private int layoutTextview;

    public LocationListAdapter(@NonNull Context context, int resource, @NonNull List<LocationEntity> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.locationEntityList = objects;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if(row == null){
            LayoutInflater vi = LayoutInflater.from(getContext());
            row = vi.inflate(R.layout.location_list_item,null);

        }
/*
        LocationEntity locationEntity = getItem(position);

        if(locationEntity != null){
            TextView displayName = (TextView) row.findViewById(R.id.locationNameTextView);

            if(displayName!=null){
                displayName.setText(locationEntity.getDisplayName());
            }


        }*/

        ViewHolder holder = new ViewHolder();
        //setupItem(holder);
        locationEntityList.get(position).setDisplayName(locationEntityList.get(position).getName()+"     TEST");
        if(locationEntityList.get(position).getDisplayName()!=null) {
            Log.i("DER SHIT IS NET NULL","MAH HE");

           // viewHolder.textview= (TextView) rowView.findViewById(R.id.txt);
           // viewHolder.button= (Button) rowView.findViewById(R.id.bt);
           holder.displayName = (TextView) row.findViewById(R.id.locationNameTextView);
           holder.removeListEntryButton = (ImageButton) row.findViewById(R.id.remove_btn_id);

            //(CharSequence) locationEntityList.get(position);
        }
        holder.displayName.setText( locationEntityList.get(position).getDisplayName());
        holder.removeListEntryButton.setTag(locationEntityList.get(position));
        row.setTag(holder);
        /*
        ViewHolder holder = new ViewHolder();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);


        holder.locationEntity = locationEntityList.get(position);

        holder.removeListEntryButton = (ImageButton)row.findViewById(R.id.remove_btn_id);
        //holder.removeListEntryButton.setTag(holder.locationEntity);

        holder.displayName = (TextView)row.findViewById(R.id.locationNameTextView);
        //holder.locationEntity = (TextView)row.findViewById(R.id.atomPay_value);

        row.setTag(holder);

        setupItem(holder);
    */


       // return super.getView(position, convertView, parent);
        return row;
    }


    public static class ViewHolder {
        public LocationEntity locationEntity;
       public TextView displayName;
        public ImageButton removeListEntryButton;
    }

}
