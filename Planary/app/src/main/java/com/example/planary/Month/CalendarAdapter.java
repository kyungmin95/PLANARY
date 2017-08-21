package com.example.planary.Month;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.planary.Day.DayDiaryDB;
import com.example.planary.R;

import java.util.ArrayList;
import java.util.Calendar;

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
            holder.diaryImg = (ImageView) convertView.findViewById(R.id.imageView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvItemDay.setText("" + getItem(position));
        holder.diaryImg.setVisibility(View.VISIBLE);


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

        Calendar cal= Calendar.getInstance();
        System.currentTimeMillis();
        String sdate = Integer.toString(cal.YEAR) + Integer.toString(cal.MONTH + 1) + Integer.toString(Calendar.DAY_OF_MONTH);
        DayDiaryDB dHelper = new DayDiaryDB(context);
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '" + sdate + "';", null);
        int count = 0; //해당 리스트 내용이 있는지 없는지를 구분하기 위한 변수.
        while (c.moveToNext()) {
            if(c.getString(0) != null){count++;} //결과값이 나올 때마다 count를 증가시킴
        }
        if (count == 0) { //count = 0 이면 결과값이 없다는 것이므로 이미지를 보이지 않게한다.
            holder.diaryImg.setVisibility(View.INVISIBLE);
        } else  { //count = 0이 아니라면 결과값이 있다는 것이므로 이미지를 보이게하고 다시 count를 초기화한다
            holder.diaryImg.setVisibility(View.VISIBLE);
        }
        c.close();
        db.close();
        notifyDataSetChanged();
        return convertView;
    }

    public class ViewHolder {
        TextView tvItemDay;
        public ImageView diaryImg;
    }
}
