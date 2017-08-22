package com.example.planary.Week;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//필요한 DB 생성
public class WeekDB extends SQLiteOpenHelper {
    public WeekDB(Context context) {
        super(context, "plaweek.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table plaweektodo " +
                "(_id integer primary key autoincrement, date text not null, content text not null, checked text not null," +
                "color text not null, nn text not null);");
    }
    //id, 날짜(date. 기간 중 월요일 날짜를 저장), 내용(content), 체크박스 체크 여부(checked), 일정 배경색(color-B,G,P,V 중 하나),
    //add 들어가 내용을 추가한것인지 edit 로 들어가 내용을 추가한 것인지 구분하기 위한 변수(nn-N,O중 하나) 저장하는 테이블 생성
    //(add로 들어가서 추가한거면 N, edit이면 O)

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists plaweektodo;");
        onCreate(db);
    }
}
