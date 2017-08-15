package com.example.planary;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.planary.Day.DayAdapter;
import com.example.planary.Day.DayDB;
import com.example.planary.Day.DayDiaryActivity;
import com.example.planary.Day.DayDiaryDB;
import com.example.planary.Day.DayTodolistActivity;

import java.util.Calendar;

public class DayActivity extends Activity {
    int year, month, day;
    TextView dayDate;
    Calendar c;
    String dd;
    DayAdapter todolistAA;
    DayDB helper;
    DayDiaryDB mHelper;
    ListView todolistLV;
    TextView dayDiary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_main);
        getCurrentDate();
        setDayDate();  //getCurrentDate로 오늘 날짜를 가져온 뒤, 그 값을 dayDate에 입력(아무 버튼도 누르지 않은 기본 상태)

        //전날로 날짜를 옮기는 함수
        findViewById(R.id.day_left).setOnClickListener(new View.OnClickListener() { // '<' 버튼 눌렀을 때. 전날로 날짜 이동
            @Override
            public void onClick(View v) {
                day -= 1;
                if(day == 0) { //전달로 넘어가야 하는 날짜가 되면 month를 하나 줄이고, 그에 맞게 day 값도 수정
                    switch (month) {
                        case 5:case 7:case 10:case 12:
                            day = 30; break;
                        case 3:
                            if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) day = 29;
                            else day = 28;
                            break;
                        default:
                            day = 31; break;
                    }
                    month -= 1;
                }
                if(month == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                    year -= 1;
                    month = 12;
                }
                setDayDate();
                setDayTodolist();
                setDayDiary();
            }
        });

        //다음날로 날짜를 옮기는 함수
        findViewById(R.id.day_right).setOnClickListener(new View.OnClickListener() {  // '>' 버튼 눌렀을 때. 다음날로 날짜 이동
            @Override
            public void onClick(View v) {
                day += 1;
                switch (month) { //다음달로 넘어가야 하는 날짜가 되면 month를 하나 증가시키고, day 도 1일로 수정.
                    case 4:case 6:case 9:case 11:
                        if(day == 31) {
                            day = 1; month += 1;
                        }
                        break;
                    case 2:
                        if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){
                            if(day == 30) day = 1;
                        }
                        if(day == 29) day = 1;
                        if(day == 1) month += 1;
                        break;
                    default:
                        if(day == 32) {
                            day = 1; month += 1;
                        }
                        break;
                }
                if(month > 12) { //만약 month가 12월이 넘어가면 년도를 다음해로 수정하고, month를 1월로 지정.
                    year += 1; month = 1;
                }
                setDayDate();
                setDayTodolist();
                setDayDiary();
            }
        });

        //오늘로 날짜를 옮기는 함수
        findViewById(R.id.day_today).setOnClickListener(new View.OnClickListener() {  // '오늘' 버튼 눌렀을 때. 오늘로 날짜 이동
            @Override
            public void onClick(View v) {
                getCurrentDate();
                setDayDate();
                setDayTodolist();
                setDayDiary();
            }
        });

        todolistAA = new DayAdapter();
        //DB helper 생성
        helper = new DayDB(this);
        mHelper = new DayDiaryDB(this);
        //리스트를 직접 구성한 이미지로 보이게 하기 위해 새로 만든 어댑터와 연결
        todolistLV = (ListView)findViewById(R.id.daytodo_list);
        setDayTodolist(); //함수 이용해 리스트 생성
        todolistLV.setAdapter(todolistAA);

        //to do list의 리스트(배경)를 선택하면 to do list 추가 화면으로 넘어가는 intent 설정
        todolistLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent todolistIntent = new Intent(DayActivity.this, DayTodolistActivity.class);
                todolistIntent.putExtra("year", year);
                todolistIntent.putExtra("month", month);
                todolistIntent.putExtra("day", day);
                startActivityForResult(todolistIntent, 1);
            }
        });

        //일기 부분 클릭하면 수정 부분으로 넘어가는 intent 설정
        dayDiary = (TextView)findViewById(R.id.daydiary);
        setDayDiary();
        dayDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent diaryIntent = new Intent(DayActivity.this, DayDiaryActivity.class);
                diaryIntent.putExtra("year", year);
                diaryIntent.putExtra("month", month);
                diaryIntent.putExtra("day", day);
                startActivityForResult(diaryIntent, 1);
            }
        });
    }

    public void getCurrentDate() {  //오늘 날짜를 가져오는 함수. Calendar 사용.
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH)+1;
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수. 날짜를 가져옴과 동시에 DB에 검색에 쓰기 위한 dd도 함께 설정.
        dayDate = (TextView)findViewById(R.id.day_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일");
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
    }

    public void mChangeDate(View v) {  //<> 표시 말고도 날짜를 누르면 달력으로 날짜를 지정할 수 있도록 Picker 지정
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        dpf.show();
    }
    //Picker 에 의해 설정된 날짜 적용
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate();
            setDayTodolist();
            setDayDiary();
        }
    };

    //todolist 항목이 없없어서 리스트뷰가 나타나지 않을 때, + 버튼과 배경을 누르면 일정 추가 화면으로 intent 하는 함수
    public void intentTodo(View v) {
        Intent todolistIntent = new Intent(DayActivity.this, DayTodolistActivity.class);
        todolistIntent.putExtra("year", year);
        todolistIntent.putExtra("month", month);
        todolistIntent.putExtra("day", day);
        startActivityForResult(todolistIntent, 1);
    }

    @Override   //intent가 finish 된 뒤 실행. extra 값을 가져와 날짜를 바꾸고 그에 따라 daydate 와 todolist 내용을 바꿈.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            year = data.getExtras().getInt("cYear");
            month = data.getExtras().getInt("cMonth");
            day = data.getExtras().getInt("cDay");
            setDayDate();
            setDayTodolist();
            setDayDiary();
        }
    }

    public void setDayTodolist() { //todolist 에 DB 내용에서 가져온 content 값을 가져옴.
        int count = 0;
        ImageView todoadd = (ImageView)findViewById(R.id.daytodo_add);
        todolistLV = (ListView)findViewById(R.id.daytodo_list);
        todolistAA.clearItem();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select _id, content, checked from pladaytodo where date = '"+ dd + "';", null);
        while(c.moveToNext()) {
            todolistAA.addItem(c.getInt(0), c.getString(1), c.getString(2));
            count++;
        }
        if(count == 0 ){
            todoadd.setVisibility(View.VISIBLE);
        }
        else todoadd.setVisibility(View.GONE);
        c.close();
        db.close();
        todolistAA.notifyDataSetChanged();
    }

    public void setDayDiary() { //일기 화면에 DB에서 해당 날짜에 해당하는 dicont 값을 넣음.
        dayDiary.setText("");
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '"+ dd + "';", null);
        while(c.moveToNext()) {
            dayDiary.setText(c.getString(0));
        }
        c.close();
        db.close();
    }
}