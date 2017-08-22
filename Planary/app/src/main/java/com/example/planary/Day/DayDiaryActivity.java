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

public class DayDiaryActivity extends Activity { //Day의 다이어리 수정,추가 화면
    int year, month, day; //다이어리 저장되는 날짜를 위한 year, month, day 변수 생섯ㅇ
    Intent mintent; //받아온 인텐트를 저장하기위한 인텐트
    TextView dayDate; //해당 날짜를 화면에 보여주기 위한 텍스트뷰
    String dd; //DB에 사용하기 위한 날짜 저장하는 변수
    DayDiaryDB mHelper; //DayDiary DB를 쓰기 위한 mHelper
    EditText diaCont; //내용을 쓰기 위한 editText
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_diary); //day_diary 레이아웃과 연결

        //intent를 얻어와 날짜를 설정한 뒤 해당 날짜에 저장 돼있는 일기 내용을 edittext에 올림
        mintent = getIntent(); //인텐트를 얻어옴
        year = mintent.getExtras().getInt("year");
        month = mintent.getExtras().getInt("month");
        day = mintent.getExtras().getInt("day");
        //인텐트에 저장되어 있는 년, 월, 일을 year, month, day에 저장
        mHelper = new DayDiaryDB(this); //DayDiaryDB 사용 위해 mHelper 생성
        setDayDate(); //텍스트뷰에 해당 날짜 set 함
        diaCont = (EditText)findViewById(R.id.daydi_cont); //
        makeDiary(); //DB로부터 내용을 가져와 edittext에 내용 저장

        //취소 버튼(뒤로 가기 화살표)을 누르면 바뀐 내용을 저장하지 않고 그냥 intent를 종료
        findViewById(R.id.daydi_can).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, mintent); //RESULT_CANCELED 값을 전달하며 인텐트를 종료해, 종료시 이전 페이지에서 아무 변화도 없게 함
                finish(); //인텐트 종료
            }
        });
        //내용 지우기 버튼을 누르면 edittext 안의 내용이 지워짐
        findViewById(R.id.daydi_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaCont.setText("");
            }
        });
    }

    public void editDiary(View v) {  // 체크 모양을 누르면 일기 내용을 DB에 추가하거나 수정하고 intent를 종료함
        SQLiteDatabase db = mHelper.getReadableDatabase(); //select문을 사용하기 위해 getReadableDatabase() 로 SQLiteDatabase 생성
        String dicont = diaCont.getText().toString(); //edittext에 쓴 내용을 가져와 dicont 에 넣음
        int mId = -1; //내용 업데이트를 하기 위해 해당 일기의 id 값을 저장할 변수를 설정. -1 로 초기화.
        //커서를 사용해 해당 날짜에 일기가 있는지 DB를 탐색. 일기가 있으면 해당 일기의 _id 값을 반환.
        Cursor c = db.rawQuery("select _id from pladaydi where date = '"+ dd +"';", null);
        while(c.moveToNext()) {
            mId = c.getInt(0); //일기가 있어서 나오는 _id 값을 mId에 저장
        }
        db = mHelper.getWritableDatabase(); //insert, update, delet 하기 위해 getWritableDatabase()로 SQListDatabase 생성
        if(dicont.length() != 0) { //edittext에 내용이 있있는 경우
            if(mId == -1) { //mId가 -1이면 DB 탐색 결과가 없는것이고 그 날짜에 일기가 없다는 의미이므로 edittext 내용을 DB에 insert함
                String queryadd = String.format("insert into %s values(null, '%s', '%s');", "pladaydi", dd, dicont);
                //date에 dd값을, dicont에 dicont 값을 넣어 DB에 insert 함
                db.execSQL(queryadd); //insert문 실행
            }
            else {  //mId가 -1이 아니면 DB 탐색 결과가 있고 그 날짜에 일기가 있다는 의미이므로 edittext 내용을 DB에 update 함
                String queryupd = String.format("update %s set dicont='%s' where _id = %d;", "pladaydi", dicont, mId);
                //id가 mId인 데이터에 dicont 내용을 dicont로 update 함
                db.execSQL(queryupd);  //update문 실행
            }
        }
        else {      //dicont 에(edittext에) 내용이 아무것도 없으면 DB에 mId를 사용해 내용 삭제
            db.delete("pladaydi", "_id=" + mId, null);
        }
        db.close(); //db 닫음
        c.close(); //커서 닫음
        //날짜를 전달하면서 intent 종료
        mintent.putExtra("cYear", year);
        mintent.putExtra("cMonth", month);
        mintent.putExtra("cDay", day);
        setResult(RESULT_OK, mintent); //RESULT_OK를 전달하며 종료해 이전 화면에서 변경된 날짜가 적용되도록 함
        finish(); //인텐트 종료
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수. 데이터베이스에 사용하기 위해 String 타입으로 dd도 지정.
        dayDate = (TextView) findViewById(R.id.daydi_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일"); //textView를 id 값으로 가져와 년, 월, 일 입력
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
        //데이터베이스에 사용하기 위해 year, month, day 값을 String으로 변경해 붙여서 String 타입으로 dd에 저장
    }

    public void memoChangeDate(View v){ //날짜를 누르면 picker를 사용해 날짜 선택하도록 함
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        //Picker를 생성. 현재 year, month, day가 표시되도록 값을 전달해 주는데 month는 0부터 시작하므로 -1을 시켜줘서 전달
        dpf.show(); //Picker 를 실행
    }

    //Picker를 사용해 선택한 날짜로 year, month, day를 갱신, 바꾼 날짜에 따라 리스트도 갱신
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            //Picker로부터 받아온 cyear, cmonth, cdayOfMonth를 year, month, day에 저장. month는 0부터 시작하므로 1 추가.
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate(); //바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
            makeDiary(); //바뀐 날짜에 따라 일기에 보여지는 내용 수정
        }
    };

    public void makeDiary() {    //지정한 날짜에 따라 저장된 일기 DB를 가져와 edittext에 넣는 함수
        diaCont.setText(""); //일단 edittext의 내용 초기화
        SQLiteDatabase db = mHelper.getReadableDatabase(); //select 하기 위해 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '"+ dd +"';", null);
        //커서를 사용해 select 문 실행. 해당 날짜에 쓰여진 일기 내용을 가져옴.
        while(c.moveToNext()) {
            diaCont.setText(c.getString(0)); //가져온 일기 내용을 edittext에 넣음
        }
        c.close();//커서 닫음
        db.close();//db 닫음
    }
}
