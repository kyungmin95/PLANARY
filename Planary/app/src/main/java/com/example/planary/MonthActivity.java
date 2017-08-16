package com.example.planary;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.planary.Month.CalendarAdapter;
import com.example.planary.Month.DayInfo;

import java.util.ArrayList;
import java.util.Calendar;

public class MonthActivity extends Activity {

    GridView gridView;
    ArrayList<DayInfo> list;
    CalendarAdapter calendarAdapter;
    Calendar c; //현재 날짜를 가져오기 위해 Calendar 사용
    int year, month, day; //년, 월, 일을 저장하는 변수 지정
    TextView dayDate; //사용자가 원하는 년, 월, 일을 저장해 띄우는 TextView
    String sdate; //DB에서 사용하기 위한 날짜
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.month_main);

        gridView = (GridView) findViewById(R.id.gridview);

        getCurrentDate();
        setDayDate();  //getCurrentDate로 오늘 날짜를 가져온 뒤, 그 값을 dayDate에 입력(아무 버튼도 누르지 않은 기본 상태)

        intent = getIntent();

        //오늘날짜 세팅
        System.currentTimeMillis();
        list = new ArrayList<>();


        //그리드뷰 아이템 클릭 시 일정 표시, 팝업으로 일정을 보여줌
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }

        });
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
                c = getLastMonth(c);
                getCalendar(c);
                break;
            case R.id.month_right: // 다음 달로 이동
                c = getNextMonth(c);
                getCalendar(c);
                break;
            case R.id.day_today: //오늘 날짜 반환
                //   String sToday = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
                //  if (sToday.equals(gridView.getAdapter().getItem(pos) {

                //    holder.tvItemDay.setTextColor(Color.rgb(150, 190, 233));
                //}
                getCurrentDate();
                setDayDate();
                getCalendar(c);
                break;
        }
    }

    public void getCurrentDate() {  //오늘 날짜를 가져오는 함수. Calendar 사용.
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수
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
            day = new DayInfo();
            day.setDay(Integer.toString(date));
            day.setInMonth(false);
            list.add(day);
        }
        for (int i = 1; i <= thisMonthLastDay; i++) {
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(true);
            list.add(day);
        }
        for (int i = 1; i < 42 - (thisMonthLastDay + dayOfMonth - 1) + 1; i++) {
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(false);
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

    // MonthAddActivity에서 보낸 intent를 받는 함수. 해당 날짜의 스케줄을 달력에 표시함
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            year = data.getExtras().getInt("cYear");
            month = data.getExtras().getInt("cMonth");
            day = data.getExtras().getInt("cDay");
            sdate = Integer.toString(year) + Integer.toString(month) + Integer.toString(day);
        }
    }
}

