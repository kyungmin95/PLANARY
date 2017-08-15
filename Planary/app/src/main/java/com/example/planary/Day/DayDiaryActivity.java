package com.example.planary.Day;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.planary.R;

public class DayDiaryActivity extends Activity {
    int year, month, day;
    Intent mintent;
    TextView dayDate;
    String dd; //DB에 사용하기 위한 날짜 저장하는 변수
    DayDiaryDB mHelper;
    EditText memoCont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_diary);

        //intent를 얻어와 날짜를 설정한 뒤 해당 날짜에 저장 돼있는 일기 내용을 edittext에 올림
        mintent = getIntent();
        year = mintent.getExtras().getInt("year");
        month = mintent.getExtras().getInt("month");
        day = mintent.getExtras().getInt("day");
        mHelper = new DayDiaryDB(this);
        setDayDate();
        memoCont = (EditText)findViewById(R.id.daydi_cont);
        makeMemo();

        //취소 버튼을 누르면 바뀐 내용을 저장하지 않고 그냥 intent를 종료
        findViewById(R.id.daydi_can).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, mintent);
                finish();
            }
        });
        //내용 지우기 버튼을 누르면 edittext 안의 내용이 지워짐
        findViewById(R.id.daydi_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memoCont.setText("");
            }
        });
    }

    public void editDiary(View v) {  // 체크 모양을 누르면 메모 내용을 DB에 추가하거나 수정하고 intent를 종료함
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String mecont = memoCont.getText().toString();
        int mId = -1;
        //해당 날짜에 일기 가 있는지 DB를 탐색
        Cursor c = db.rawQuery("select _id from pladaydi where date = '"+ dd +"';", null);
        while(c.moveToNext()) {
            mId = c.getInt(0);
        }
        db = mHelper.getWritableDatabase();
        if(mecont.length() != 0) {
            if(mId == -1) { //DB 탐색 결과가 없으면 해당 내용을 DB에 insert함
                String queryadd = String.format("insert into %s values(null, '%s', '%s');", "pladaydi", dd, mecont);
                db.execSQL(queryadd);
            }
            else {  //DB 탐색 결과가 있으면 해당 내용을 DB에 update 함
                String queryupd = String.format("update %s set dicont='%s' where _id = %d;", "pladaydi", mecont, mId);
                db.execSQL(queryupd);
            }
        }
        else {      //mecont 에 내용이 아무것도 없으면 DB 내용 삭제
            db.delete("pladaydi", "_id=" + mId, null);
        }
        db.close();
        c.close();
        //날짜를 전달하면서 intent 종료
        mintent.putExtra("cYear", year);
        mintent.putExtra("cMonth", month);
        mintent.putExtra("cDay", day);
        setResult(RESULT_OK, mintent);
        finish();
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수. 데이터베이스에 사용하기 위해 String 타입으로 dd도 지정.
        dayDate = (TextView) findViewById(R.id.daydi_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일");
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
    }

    public void memoChangeDate(View v){ //picker를 사용해 날짜 선택하도록 함
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        dpf.show();
    }

    //Picker를 사용해 선택한 날짜로 year, month, day를 갱신, 바꾼 날짜에 따라 리스트도 갱신
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate();
            makeMemo();
        }
    };

    public void makeMemo() {    //지정한 날짜에 따라 저장된 일기 DB를 가져와 edittext에 넣는 함수
        memoCont.setText("");
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '"+ dd +"';", null);
        while(c.moveToNext()) {
            memoCont.setText(c.getString(0));
        }
        c.close();
        db.close();
    }
}
