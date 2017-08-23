package com.example.planary.Month;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
        DayInfo day = list.get(position); //position 위치에 있는 리스트를 가져와 day에 넣음
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.content_month_item, parent, false);
            holder = new ViewHolder();
            holder.tvItemDay = (TextView) convertView.findViewById(R.id.tv_item);
            holder.tvTodo = (TextView)convertView.findViewById(R.id.tv_todo);
            holder.diaryImg = (ImageView) convertView.findViewById(R.id.imageView2);
            holder.checkImg = (ImageView) convertView.findViewById(R.id.check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvItemDay.setText("" + getItem(position));

        //day로부터 체크되지 않은 todolist의 갯수를 받아 화면에 띄움. 갯수가 없으면 아무것도 띄우지 않고, 갯수가 있으면 화면에 띄운다.
        if(day.getTodoCount() == 0) { //체크안된 todolist 갯수가 없는 경우
            holder.tvTodo.setText(""); //아무 숫자도 나타나지 않음
            holder.checkImg.setVisibility(View.INVISIBLE); //이미지도 없앰
        }
        else { //체크안된 todolist 갯수가 있는 경우
            holder.checkImg.setVisibility(View.VISIBLE); //이미지 보임
            holder.tvTodo.setText(Integer.toString(day.getTodoCount())); //갯수를 화면에 나타냄
        }

        //다이어리가 있으면 이미지를 보이게 하고, 다이어리가 없으면 이미지를 보이지 않게 한다.
        if(day.isDiary()) holder.diaryImg.setVisibility(View.VISIBLE);
        else holder.diaryImg.setVisibility(View.INVISIBLE);


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
            } else { //해당 달이 아닌 전달, 다음달인 경우 색을 전부 LTGRAY로 지정
                holder.tvItemDay.setTextColor(Color.LTGRAY);
                holder.tvTodo.setTextColor(Color.LTGRAY);
                holder.diaryImg.setColorFilter(Color.LTGRAY);
                holder.checkImg.setColorFilter(Color.LTGRAY);
            }
        }
        notifyDataSetChanged();
        return convertView;
    }

    public class ViewHolder {
        TextView tvItemDay;
        TextView tvTodo;
        public ImageView diaryImg;
        ImageView checkImg;
    }
}
