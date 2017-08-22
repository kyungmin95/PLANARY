package com.example.planary.Day;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Day의 todolist에 필요한 DB를 생성
public class DayDB extends SQLiteOpenHelper {
    public DayDB(Context context) {
        super(context, "pladaytodo.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //id(_id), 날짜(date), 내용(content), 체크박스 체크 여부(checked) 저장하는 테이블 생성
        db.execSQL("create table pladaytodo " +
                "(_id integer primary key autoincrement, date text not null, content text not null, checked text not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists pladaytodo;");
        onCreate(db);
    }
}
