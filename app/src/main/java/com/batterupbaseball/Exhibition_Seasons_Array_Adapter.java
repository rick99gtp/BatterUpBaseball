package com.batterupbaseball;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Exhibition_Seasons_Array_Adapter extends ArrayAdapter<season_class>{

    private final Context context;
    private final ArrayList<season_class> seasonsArrayList;
    private int selectedIndex;
    private int selectedColor = Color.parseColor("#ff0000");

    public Exhibition_Seasons_Array_Adapter(Context context, ArrayList<season_class> seasonsArrayList) {
        super(context, R.layout.exhibition_season_row, seasonsArrayList);

        this.context = context;
        this.seasonsArrayList = seasonsArrayList;
        selectedIndex = -1;
    }

    public void setSelectedIndex(int ind)
    {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Viewholder holder;

        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.exhibition_season_row, parent, false);

            holder = new Viewholder();
            holder.seasonName = (TextView) convertView.findViewById(R.id.tvSeasonName);

            convertView.setTag(holder);
        }
        else {
            holder = (Viewholder) convertView.getTag();
        }

        holder.seasonName.setText(seasonsArrayList.get(position).getSeasonName());

        holder.seasonName.setBackgroundColor(selectedColor);

        if(selectedIndex != -1 && position == selectedIndex)
        {
            holder.seasonName.setBackgroundColor(Color.RED);
        }
        else
        {
            holder.seasonName.setBackground(null);
        }

        // 5. return rowView
        return convertView;
    }

    static class Viewholder {
        TextView seasonName;
    }
}
