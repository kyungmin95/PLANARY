package com.example.planary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DayActivity extends Activity {
    int year, month, day; //년, 월, 일을 저장하는 변수 지정
    TextView dayDate; //사용자가 원하는 년, 월, 일을 저장해 띄우는 TextView
    Calendar c;  //현재 날짜를 가져오기 위해 Calendar 사용
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_main);getCurrentDate();
        setDayDate();  //getCurrentDate로 오늘 날짜를 가져온 뒤, 그 값을 dayDate에 입력(아무 버튼도 누르지 않은 기본 상태)

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
            }
        });

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
            }
        });

        findViewById(R.id.day_today).setOnClickListener(new View.OnClickListener() {  // '오늘' 버튼 눌렀을 때. 오늘로 날짜 이동
            @Override
            public void onClick(View v) {
                getCurrentDate();
                setDayDate();
            }
        });

        CheckBox dayCheck01 = (CheckBox)findViewById(R.id.day_check01);
        CheckBox dayCheck02 = (CheckBox)findViewById(R.id.day_check02);
        //if(dayCheck01.isChecked()) dayCheck01.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG); 아직 작동 안함

    }

    public void getCurrentDate() {  //오늘 날짜를 가져오는 함수. Calendar 사용.
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH)+1;
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수
        dayDate = (TextView)findViewById(R.id.day_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일");
    }

    //public void setDayCheckBox(CheckBox cb, String str, String cbid) {  //DB를 사용한다면 쓰려 했던 체크박스 추가하는 함수인데, 일단 냅두자

    //}

    public void getCurrentDate_date() {
        //long now = System.currentTimeMillis();
        Date date = new Date();
        SimpleDateFormat CurYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat CurMonth = new SimpleDateFormat("MM");
        SimpleDateFormat CurDay = new SimpleDateFormat("dd");
        year = Integer.parseInt(CurYear.format(date));
        month = Integer.parseInt(CurMonth.format(date));
        day = Integer.parseInt(CurDay.format(date));
    }
}
