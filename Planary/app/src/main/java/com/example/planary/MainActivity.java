package com.example.planary;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
    TabHost mTab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        TabHost mTab = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.activity_main, mTab.getTabContentView(), true); //activity_main.xml과 inflate
        //탭을 추가
        mTab.addTab(mTab.newTabSpec("month").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.month))
                .setContent(new Intent(this, ExActivity.class)));
        mTab.addTab(mTab.newTabSpec("week").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.week))
                .setContent(new Intent(this, WeekActivity.class)));
        mTab.addTab(mTab.newTabSpec("day").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.day))
                .setContent(new Intent(this, DayActivity.class))); //해당 탭 누르면 DayActivity와 내용이 연결
        mTab.addTab(mTab.newTabSpec("memo").setIndicator(getTabIndicator(mTab.getContext(), R.drawable.memo))
                .setContent(new Intent(this, ExActivity.class)));
    }

    private View getTabIndicator(Context context, int icon) {   //탭의 디자인을 지정하기 위해 탭 디자인 설정 layout과 연결시키는 함수
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }
}
