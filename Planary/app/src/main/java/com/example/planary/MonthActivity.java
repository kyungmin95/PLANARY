package com.example.planary;


import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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

    //버튼클릭 시 반응
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.month_left: // 이전 달로 이동
                month -= 1; //month 하나 줄임
                if(month == 0) { //month가 0이 되면 년도를 하나 줄이고 month를 12로 수정
                    year -= 1; month = 12;
                }
                c = getLastMonth(c);
                getCalendar(c);
                break;
            case R.id.month_right: // 다음 달로 이동
                month += 1; //month 하나 증가
                if(month == 13) { //month가 13이 되면 year를 하나 증가시키고 month를 1로 수정
                    year += 1; month = 1;
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

    public void getCurrentDate() {  //오늘 날짜를 가져오는 함수. Calendar 사용.
        c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하고 데이터베이스에서 사용하기 위한 sdate 설정
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

        /*이번달의 시작일의 요일 구하기*/
        dayOfMonth = c.get(Calendar.DAY_OF_WEEK);
        thisMonthLastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        c.add(Calendar.MONTH, -1);
        Log.e("지난달 마지막일", c.get(Calendar.DAY_OF_MONTH) + "");
        //지난달의 마지막 일자를 구함
        lastMonthStartDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        c.add(Calendar.MONTH, 1);
        Log.e("이번달 시작일", calendar.get(Calendar.DAY_OF_MONTH) + "");

        lastMonthStartDay -= (dayOfMonth - 1) - 1;

        //캘린더 년월 표시
        dayDate.setText(c.get(Calendar.YEAR) + "년 " + (c.get(Calendar.MONTH) + 1) + "월 ");

        DayInfo day;
        Log.e("DayOfMonth", dayOfMonth + "");

        /*캘린더에 들어갈 숫자 입력
        * 저번달의 날짜를 회색으로 표시
        * 이번달의 날짜들을 표시
        * 다음달의 날짜들을 표시(총 7*6 =42칸을 기준으로 함)*/
        for (int i = 0; i < dayOfMonth - 1; i++) {
            int date = lastMonthStartDay + i;
            String sdate = Integer.toString(year) + Integer.toString(month-1) + Integer.toString(date);
            //DB 탐색 위한 sdate 생성. 지난달을 표시하는 부분이니 month를 하나 줄인 sdate를 생성한다.
            //day에 해당 내용들을 삽입하고 list 에 add
            day = new DayInfo();
            day.setDay(Integer.toString(date));
            day.setInMonth(false);
            day.setDiary(getDiary(sdate));
            day.setTodoCount(getTodo(sdate));
            list.add(day);
        }
        for (int i = 1; i <= thisMonthLastDay; i++) {
            String sdate = Integer.toString(year) + Integer.toString(month) + Integer.toString(i);//DB 탐색 위한 sdate 생성.
            //day에 내용 삽입하고 list 에 add
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(true);
            day.setDiary(getDiary(sdate));
            day.setTodoCount(getTodo(sdate));
            list.add(day);
        }
        for (int i = 1; i < 42 - (thisMonthLastDay + dayOfMonth - 1) + 1; i++) {
            String sdate = Integer.toString(year) + Integer.toString(month+1) + Integer.toString(i);
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

    /*다음음달의 Calendar 객체 반환
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

    public boolean getDiary(String date) { //인자로 받은 date 에 해당하는 일기가 있는지 탐색하고, 일기가 있으면 true 없으면 false 를 반환
        String sdate = date;
        DayDiaryDB dHelper = new DayDiaryDB(this); //day의 diary DB를 가져와 dHelper 만듦
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '" + sdate + "';", null);
        int count = 0; //해당 내용이 있는지 없는지를 구분하기 위한 변수. 0으로 초기화.
        while (c.moveToNext()) {
            count++; //select 한 결과가 있으면 count를 증가
        }
        c.close();
        db.close();
        if(count == 0 ) return false; //count가 0이면 일기가 없다는 이야기이므로 false 를 반환
        else return true; //count가 0이 아니면 일기가 있다는 이야기이므로 true를 반환
    }

    public int getTodo(String date) { //인자로 받은 date에 있는 todolist 중 체크되지 않은(checkde 가 F) todolist의 갯수를 반환하는 함수
        String sdate = date;
        int count = 0; //체크되지 않은 todolist 갯수 저장하기 위한 변수. 0으로 초기화.
        DayDB helper = new DayDB(this); //day의 todolist DB를 가져와 helper 생성
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select content from pladaytodo where date = '" + sdate + "'" + " AND checked = 'F';", null);
        while (c.moveToNext()) {
            count++; //결과가 나올때마다 count 1씩 증가
        }
        return count; //count 값을 반환
    }

}

