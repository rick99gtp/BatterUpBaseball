package com.batterupbaseball;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Exhibition_Array_Adapter extends ArrayAdapter<teams>{

    private final Context context;
    private final ArrayList<teams> teamsArrayList;

    public Exhibition_Array_Adapter(Context context, ArrayList<teams> teamsArrayList) {
        super(context, R.layout.exhibition_team_select_row, teamsArrayList);

        this.context = context;
        this.teamsArrayList = teamsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Viewholder holder;

        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.exhibition_team_select_row, parent, false);

            holder = new Viewholder();
            holder.cv = (CardView) convertView.findViewById(R.id.cvTeam);
            holder.teamname = (TextView) convertView.findViewById(R.id.tvTeamName);
            holder.batting_rating = (TextView) convertView.findViewById(R.id.tvBattingRating);
            holder.pitching_rating = (TextView) convertView.findViewById(R.id.tvPitchingRating);
            holder.fielding_rating = (TextView) convertView.findViewById(R.id.tvFieldingRating);

            convertView.setTag(holder);
        }
        else {
            holder = (Viewholder) convertView.getTag();
        }

        holder.teamname.setText(teamsArrayList.get(position).getTeamName());
        holder.batting_rating.setText(Integer.toString(teamsArrayList.get(position).getBattingRating()));
        holder.pitching_rating.setText(Integer.toString(teamsArrayList.get(position).getPitchingRating()));
        holder.fielding_rating.setText(Integer.toString(teamsArrayList.get(position).getFieldingRating()));

        holder.teamname.setBackgroundColor(teamsArrayList.get(position).getTeamColor1());
        holder.teamname.setTextColor(teamsArrayList.get(position).getTeamColor2());

        // 5. return rowView
        return convertView;
    }

    static class Viewholder {
        TextView teamname;
        TextView batting_rating;
        TextView pitching_rating;
        TextView fielding_rating;
        CardView cv;
    }
}
