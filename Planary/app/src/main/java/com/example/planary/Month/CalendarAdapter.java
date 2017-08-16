package com.example.planary.Month;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.planary.R;

import java.util.ArrayList;

/*BaseAdapter를 상속받아서 구현한 CalendarAdapter*/

public class CalendarAdapter extends BaseAdapter {
    private ArrayList<DayInfo> list;
    private Context context;
    private int resource;
    private LayoutInflater inflater;

    /* Adpater 생성자
    * @param context 컨텍스트
    * @param textResource 레이아웃리소스
    * @param dayList 날짜정보가 들어있는 리스트*/

    public CalendarAdapter(Context context, int textResource, ArrayList<DayInfo> dayList) {
        this.context = context;
        this.list = dayList;
        this.resource = textResource;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DayInfo day = list.get(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.content_month_item, parent, false);
            holder = new ViewHolder();
            holder.tvItemDay = (TextView) convertView.findViewById(R.id.tv_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvItemDay.setText("" + getItem(position));


        /* 요일별 색깔지정
        * 일: 빨강
        * 토: 파랑
        * 평일: 검정
        * 해당 달 이외의 날: 회색*/

        if (day != null) {
            holder.tvItemDay.setText(day.getDay());

            if (day.isInMonth()) { //요일별 색
                if (position % 7 == 0) {
                    holder.tvItemDay.setTextColor(Color.RED);
                } else if (position % 7 == 6) {
                    holder.tvItemDay.setTextColor(Color.BLUE);
                } else {
                    holder.tvItemDay.setTextColor(Color.BLACK);
                }
            } else {
                holder.tvItemDay.setTextColor(Color.LTGRAY);
            }
        }
        return convertView;
    }

    public class ViewHolder {
        TextView tvItemDay;
    }
}
