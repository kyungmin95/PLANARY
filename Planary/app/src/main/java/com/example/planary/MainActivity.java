package com.example.planary;

import android.app.TabActivity;
import android.content.Context;
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
        TabHost.TabSpec spec;
        LayoutInflater.from(this).inflate(R.layout.activity_main, mTab.getTabContentView(), true);

        this.setNewTab(this, mTab, "home", R.drawable.home, R.id.tv1);
        this.setNewTab(this, mTab, "month", R.drawable.month, R.id.tv1);
        this.setNewTab(this, mTab, "week", R.drawable.week, R.id.tv1);
        this.setNewTab(this, mTab, "day", R.drawable.day, R.id.tv1);
        this.setNewTab(this, mTab, "memo", R.drawable.memo, R.id.tv1);
        this.setNewTab(this, mTab, "pen", R.drawable.pen, R.id.tv1);
    }

    private void setNewTab(Context context, TabHost tabHost, String tag, int icon, int contentID ){ //새로 탭을 만들어 추가하는 함수
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(getTabIndicator(tabHost.getContext(), icon));
        tabSpec.setContent(contentID);
        tabHost.addTab(tabSpec);
    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }
}
