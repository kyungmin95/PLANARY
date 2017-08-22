package com.example.planary;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
    TabHost mTab; //각각 페이지와 연결되는 tab을 만들기 위해 TabHost를 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost mTab = getTabHost(); //TabHost를 가져옴
        LayoutInflater.from(this).inflate(R.layout.activity_main, mTab.getTabContentView(), true); //activity_main.xml과 inflate
        //탭을 추가. month, week, day, memo로 4개의 탭이 필요하므로 4개의 탭을 추가하고, 연결시킬 액티비티도 연결시킴
        mTab.addTab(mTab.newTabSpec("month").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.month))
                .setContent(new Intent(this, MonthActivity.class))); //탭에 알맞은 이미지를 연결하고 내용은 MonthActivity와 연결
        mTab.addTab(mTab.newTabSpec("week").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.week))
                .setContent(new Intent(this, WeekActivity.class))); //탭에 알맞은 이미지를 연결하고, 내용은 WeekActivity와 연결
        mTab.addTab(mTab.newTabSpec("day").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.day))
                .setContent(new Intent(this, DayActivity.class))); //탭에 알맞은 이미지를 연결하고, 내용은 DayActivity와 연결
        mTab.addTab(mTab.newTabSpec("memo").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.memo))
                .setContent(new Intent(this, MemoActivity.class))); //탭에 알맞은 이미지를 연결하고, 내용은 MemoActivity와 연결
    }

    private View getTabIndicator(Context context, int icon) {   //탭의 디자인을 지정하기 위해 탭 디자인 설정 layout과 연결시키는 함수
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null); //탭 디자인이 구성되어 있는 tab_layout 과 연결
        ImageView iv = (ImageView) view.findViewById(R.id.imageView); //레이아웃에 있는 이미지뷰 id 값을 이용해 가져옴
        iv.setImageResource(icon); //인자로 받은 icon 모양을 해당 레이아웃 안에 있는 이미지뷰에 연결
        return view;
    }
}
