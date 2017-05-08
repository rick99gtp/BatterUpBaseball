package com.batterupbaseball;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.exhibition_team_select_row, parent, false);

        // 3. Get the two text view from the rowView
        TextView tvTeamName = (TextView) rowView.findViewById(R.id.tvTeamName);
        TextView tvBattingRating = (TextView) rowView.findViewById(R.id.tvBattingRating);
        TextView tvPitchingRating = (TextView) rowView.findViewById(R.id.tvPitchingRating);
        TextView tvFieldingRating = (TextView) rowView.findViewById(R.id.tvFieldingRating);

        // 4. Set the text for textView
        tvTeamName.setText(teamsArrayList.get(position).getTeamName());
        tvBattingRating.setText(Integer.toString(teamsArrayList.get(position).getBattingRating()));
        tvPitchingRating.setText(Integer.toString(teamsArrayList.get(position).getPitchingRating()));
        tvFieldingRating.setText(Integer.toString(teamsArrayList.get(position).getFieldingRating()));

        tvTeamName.setBackgroundColor(teamsArrayList.get(position).getTeamColor1());
        tvTeamName.setTextColor(teamsArrayList.get(position).getTeamColor2());

        // 5. return rowView
        return rowView;
    }
}
