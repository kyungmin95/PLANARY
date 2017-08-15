package com.example.planary.Day;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DayDiaryDB extends SQLiteOpenHelper {
    public DayDiaryDB(Context context) {
        super(context, "pladaydi.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //id(_id), 날짜(date), 내용(dicont) 저장하는 테이블 생성
        db.execSQL("create table pladaydi " +
                "(_id integer primary key autoincrement, date text not null, dicont text not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists pladaydi;");
        onCreate(db);
    }
}
