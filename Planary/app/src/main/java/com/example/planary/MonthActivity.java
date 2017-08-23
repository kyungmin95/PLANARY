package com.example.planary;


import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.example.planary.Day.DayDB;
import com.example.planary.Day.DayDiaryDB;
import com.example.planary.Month.CalendarAdapter;
import com.example.planary.Month.DayInfo;

import java.util.ArrayList;
import java.util.Calendar;

public class MonthActivity extends Activity {

    GridView gridView;
    ArrayList<DayInfo> list;
    CalendarAdapter calendarAdapter;
    Calendar c; //현재 날짜를 가져오기 위해 Calendar 사용
    int year, month; //년, 월을 저장하는 변수 지정
    TextView dayDate; //사용자가 원하는 년, 월, 일을 저장해 띄우는 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.month_main);

        gridView = (GridView) findViewById(R.id.gridview);

        getCurrentDate();
        setDayDate();  //getCurrentDate로 오늘 날짜를 가져온 뒤, 그 값을 dayDate에 입력(아무 버튼도 누르지 않은 기본 상태)

        //오늘날짜 세팅
        System.currentTimeMillis();
        list = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //이번달의 캘린더를 생성
        c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        getCalendar(c);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //버튼클릭 시 반응(이전 달, 다음 달, 오늘 버튼)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.month_left: // 이전 달로 이동
                month -= 1; //month 하나 줄임
                if (month == 0) { //month가 0이 되면 년도를 하나 줄이고 month를 12로 수정
                    year -= 1;
                    month = 12;
                }
                c = getLastMonth(c);
                getCalendar(c);
                break;
            case R.id.month_right: // 다음 달로 이동
                month += 1; //month 하나 증가
                if (month == 13) { //month가 13이 되면 year를 하나 증가시키고 month를 1로 수정
                    year += 1;
                    month = 1;
                }
                c = getNextMonth(c);
                getCalendar(c);
                break;
            case R.id.day_today: //오늘날짜가 있는 달로 이동
                getCurrentDate();
                setDayDate();
                getCalendar(c);
                break;
        }
    }

    //오늘 날짜를 가져오는 함수. Calendar 사용.
    public void getCurrentDate() {
        c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
    }

    //TextView에 년, 월, 일을 입력하고 데이터베이스에서 사용하기 위한 sdate 설정
    public void setDayDate() {
        dayDate = (TextView) findViewById(R.id.day_date);
        dayDate.setText(c.get(Calendar.YEAR) + "년 " + (c.get(Calendar.MONTH) + 1) + "월 ");
    }

    /*달력세팅
    * @param calendar 달력에 보여지는 이번달의 Calendar 객체
    * */
    private void getCalendar(Calendar calendar) {
        int lastMonthStartDay;
        int dayOfMonth;
        int thisMonthLastDay;

        list.clear();

        dayOfMonth = c.get(Calendar.DAY_OF_WEEK); //이번달의 시작, 1일의 요일을 숫자로 넣음(1:일요일~7:토요일 기준)
        thisMonthLastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH); //이번 달의 마지막 날의 숫자를 구함

        c.add(Calendar.MONTH, -1); //한달 전
        lastMonthStartDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);  //지난달의 마지막 일자를 구함

        c.add(Calendar.MONTH, 1); //한달 후
        lastMonthStartDay -= (dayOfMonth - 1) - 1; //이번 달에서 보여질 지난 달 한 주를 계산

        setDayDate();//캘린더 년월 표시

        DayInfo day;

        /*캘린더에 들어갈 숫자 입력
        * 저번달의 날짜를 회색으로 표시
        * 이번달의 날짜들을 표시
        * 다음달의 날짜들을 표시(총 7*6 =42칸을 기준으로 함)*/
        for (int i = 0; i < dayOfMonth - 1; i++) { //이번 달에서 보여질 지난 달의 달력 세팅
            int date = lastMonthStartDay + i;
            String sdate = Integer.toString(year) + Integer.toString(month - 1) + Integer.toString(date);
            //DB 탐색 위한 String 타입의 sdate 생성. 지난달을 표시하는 부분이니 month를 하나 줄인 sdate를 생성한다.
            //day에 해당 내용들을 삽입하고 list 에 add
            day = new DayInfo();
            day.setDay(Integer.toString(date));
            day.setInMonth(false);
            day.setDiary(getDiary(sdate));
            day.setTodoCount(getTodo(sdate));
            list.add(day);
        }
        for (int i = 1; i <= thisMonthLastDay; i++) { //이번 달 1일부터 마지막 날까지 달력 세팅
            String sdate = Integer.toString(year) + Integer.toString(month) + Integer.toString(i);//DB 탐색 위한 String 타입의 sdate 생성.
            //day에 내용 삽입하고 list 에 add
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(true);
            day.setDiary(getDiary(sdate));
            day.setTodoCount(getTodo(sdate));
            list.add(day);
        }
        //총 42칸의 달력에서 이번 달의 마지막 날짜와 시작일을 이용해 계산 후, 남은 날을 달력에 세팅(다음 달)
        for (int i = 1; i < 42 - (thisMonthLastDay + dayOfMonth - 1) + 1; i++) {
            String sdate = Integer.toString(year) + Integer.toString(month + 1) + Integer.toString(i);
            //DB 탐색 위한 sdate 생성. 다음달을 표시하는 부분이니 month를 하나 증가시킨 sdate를 생성한다.
            //day에 내용 삽입하고 list 에 add
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(false);
            day.setDiary(getDiary(sdate));
            day.setTodoCount(getTodo(sdate));
            list.add(day);
        }
        initCalendarAdapter();
    }

    /*지난달의 Calendar 객체 반환
    * @param calendar
    * @return LastMonthCalendar
    * */
    private Calendar getLastMonth(Calendar calendar) {
        calendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, -1);
        setDayDate();
        return calendar;
    }

    /*다음달의 Calendar 객체 반환
    * @param calendar
     * @return NextMonthCalendar
    * */
    private Calendar getNextMonth(Calendar calendar) {
        calendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, +1);
        setDayDate();
        return calendar;
    }

    private void initCalendarAdapter() {
        calendarAdapter = new CalendarAdapter(this, R.layout.content_month_item, list);
        gridView.setAdapter(calendarAdapter);
    }

    //인자로 받은 date 에 해당하는 일기가 있는지 탐색하고, 일기가 있으면 true 없으면 false 를 반환
    public boolean getDiary(String date) {
        String sdate = date; //인자로 받은 date 값을 sdate 에 넣음
        DayDiaryDB dHelper = new DayDiaryDB(this); //day의 diary DB를 가져와 dHelper 만듦
        SQLiteDatabase db = dHelper.getReadableDatabase(); //select 위한 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '" + sdate + "';", null);
        //커서 이용해 date가 sdate 인 데이터의 dicont를 가져옴
        int count = 0; //해당 내용이 있는지 없는지를 구분하기 위한 변수. 0으로 초기화.
        while (c.moveToNext()) {
            count++; //select 한 결과가 있으면 count를 증가
        }
        c.close(); //커서 닫음
        db.close(); //db 닫음
        if (count == 0) return false; //count가 0이면 일기가 없다는 이야기이므로 false 를 반환
        else return true; //count가 0이 아니면 일기가 있다는 이야기이므로 true를 반환
    }

    //인자로 받은 date에 있는 todolist 중 체크되지 않은(checkde 가 F) todolist의 갯수를 반환하는 함수
    public int getTodo(String date) {
        String sdate = date; //인자로 받은 date 값을 sdate 에 넣음
        int count = 0; //체크되지 않은 todolist 갯수 저장하기 위한 변수. 0으로 초기화.
        DayDB helper = new DayDB(this); //day의 todolist DB를 가져와 helper 생성
        SQLiteDatabase db = helper.getReadableDatabase(); //select 위한 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select content from pladaytodo where date = '" + sdate + "'" + " AND checked = 'F';", null);
        //커서 이용해 date가 sdate 이고 체크상태 여부가 F인 데이터의 content 를 가져옴
        while (c.moveToNext()) {
            count++; //결과가 나올때마다 count 1씩 증가
        }
        return count; //count 값을 반환
    }
}

