package com.example.planary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.planary.Week.WeekAddTodoActivity;
import com.example.planary.Week.WeekDB;
import com.example.planary.Week.WeekEditTodoActivity;
import com.example.planary.Week.WeekListAdapter;

import java.util.Calendar;

import static android.view.View.GONE;

public class WeekActivity extends Activity { //Week의 가장 메인 화면
    int year, month, day, dow; //해당 년,월,일,요일을 저장하는 변수
    int MDate, SDate; //해당 주의 월요일, 일요일의 일을 저장하는 변수
    int Mmonth, Smonth; //해당 주의 월요일, 일요일의 월을 저장하는 변수
    int WYear; //주에 따른 년도를 저장하는 변수
    String wd; //DB 검색을 위한 해당 주의 월요일을 저장하는 변수
    TextView weekp; //해당 주의 기간을 화면에 보여주는 textview
    Calendar c; //오늘 날짜를 가져오기 위한 Calendar
    ListView bLV, gLV, pLV, vLV; //배경이 파란색(bLV), 초록색(gLV), 분홍색(pLV), 보라색(vLV)인 리스트뷰
    LinearLayout bLL, gLL, pLL, vLL; //배경이 파란색(bLL), 초록색(gLL), 분홍색(pLL), 보라색(vLL)인 LinearLayout
    WeekListAdapter blAdapter, glAdapter, plAdapter, vlAdapter;
    //배경이 파란색(blAdapter), 초록색(glAdapter), 분홍색(plAdapter), 보라색(vlAdapter)인 리스트뷰와 연결하기 위한 어댑터
    WeekDB wHelper; //WeekDB를 사용하기 위한 helper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_main); //week_main 레이아웃과 연결

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    // 오른쪽 아래 + 버튼을 누르면 날짜에 대한 정보를 넘기면서 일정 추가 화면으로 넘어감
                Intent addTodoIntent = new Intent(WeekActivity.this, WeekAddTodoActivity.class);
                //WeekAddTodoActivity로 인텐트 연결하는 인텐트 생성
                addTodoIntent.putExtra("year", year);
                addTodoIntent.putExtra("month", month);
                addTodoIntent.putExtra("day", day);
                addTodoIntent.putExtra("dayofweek", dow);
                //해당 년,월,일,요일을 인텐트에 저장
                startActivityForResult(addTodoIntent, 1);
                //인텐트 종료되면 값을 인텐트에 저장된 값을 전달받고 함수 실행을 위해 값을 전달받는 인텐트로 시작
            }
        });
        weekp = (TextView)findViewById(R.id.week_date); //주를 보여주기 위한 textview를 id 를 사용해 가져옴
        wHelper = new WeekDB(this); //WeekDB Helper 생성
        getCurrentWeek(); //오늘 날짜와 요일을 가져옴
        getFEDate(); //가져온 날짜와 요일에 따라 이번주 기간을 가져옴(월요일, 일요일 날짜 가져옴)
        setWeekP(); //주를 보여주는 textview weekp에 해당 주를 set 함

        //각각 색에 따른 리스트와 리니어 레이아웃을 id 값을 이용해 설정
        bLV = (ListView)findViewById(R.id.week_list_b);
        gLV = (ListView)findViewById(R.id.week_list_g);
        pLV = (ListView)findViewById(R.id.week_list_p);
        vLV = (ListView)findViewById(R.id.week_list_v);
        bLL = (LinearLayout)findViewById(R.id.week_todo_b);
        gLL = (LinearLayout)findViewById(R.id.week_todo_g);
        pLL = (LinearLayout)findViewById(R.id.week_todo_p);
        vLL = (LinearLayout)findViewById(R.id.week_todo_v);
        //각각 배경색에 따른 어댑터 생성
        blAdapter = new WeekListAdapter();
        glAdapter = new WeekListAdapter();
        plAdapter = new WeekListAdapter();
        vlAdapter = new WeekListAdapter();
        //어댑터와 리스트 연결
        bLV.setAdapter(blAdapter);
        gLV.setAdapter(glAdapter);
        pLV.setAdapter(plAdapter);
        vLV.setAdapter(vlAdapter);

        setTodoList(); //각각 색에 따른 리스트 생성

        //전주로 이동하는 함수. < 버튼 누르는 경우.
        findViewById(R.id.week_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cday = day - 7; //주를 구할 때 기준으로 삼는 날짜를 1주 전으로 돌리기 위해 day 값에서 7을 뺀 값을 cday에 넣음
                if(cday < 1) { //일주일 전으로 이동했을 때 1보다 작으면 전달로 넘어가야 하므로 month를 하나 줄이고, 그에 맞게 day 값도 수정
                    switch (month) {
                        case 5:case 7:case 10:case 12: //5,7,10,12월인 경우 전달이 4,6,9,11월이므로 전달의 마지막 요일이 30일임
                            day = 30 + cday; break; //전달의 마지막 요일인 30일에서 cday 를 더해서 일주일 전의 day를 재설정
                        case 3: //전달이 2월인 경우
                            if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) day = 29 + cday;
                            //윤달인 경우 2월의 마지막 요일인 29일에 cday를 더해서 일주일 전의 day를 재설정
                            else day = 28 + cday; //윤달인 아닌 경우 2월의 마지막 요일인 28일에 cday를 더해서 일주일 전의 day를 재설정
                            break;
                        default: //그 외의 경우 전달의 마지막 요일이 31일.
                            day = 31 + cday; break; //전달의 마지막 요일인 31일에서 cday 를 더해서 일주일 전의 day를 재설정
                    }
                    month -= 1; //전달로 이동했으므로 month도 하나 줄임
                }
                else day -= 7; //cday가 1 이상이면 day 값을 1주일 전인 7일 전으로 아예 지정을 함
                if(month == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                    year -= 1;
                    month = 12;
                }
                getFEDate(); //변경한 날짜에 따른 해당 주의 기간을 가져옴
                setWeekP(); //해당 주 기간을 textview 에 넣어 화면에 보여줌
                setTodoList(); //변경한 날짜에 따른 주에 해당하는 리스트를 재생성
            }
        });

        //다음주로 이동하는 함수. > 버튼 누르는 경우.
        findViewById(R.id.week_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cday = day + 7; //주를 구할 때 기준으로 삼는 날짜를 1주 뒤로 돌리기 위해 day 값에서 7을 더한 값을 cday에 넣음
                switch (month) { //일주일 뒤로 이동했을 때 다음달로 넘어가야 하는 날짜가 되면 month를 하나 증가시키고, day 알맞게 수정
                    case 4:case 6:case 9:case 11: //월이 4,6,9,11월인 경우 마지막 날이 30일
                        if(cday > 30) { //마지막 날인 30일을 cday가 넘는 경우
                            day = cday - 30; month += 1; //cday에서 해당 월의 마지막 날인 30일을 뺀 값을 day에 저장하고 month도 하나 증가시킴
                        }
                        else day += 7; //cday가 마지막 날을 넘지 않으면 day도 그냥 7일을 추가한 날로 저장
                        break;
                    case 2: //2월인 경우
                        if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){ //윤달인 경우
                            if(cday > 29) { day = cday - 29; month += 1;}
                            //cday가 마지막 날인 29일을 넘으면 cday에서 마지막날인 29일을 뺀 값을 day에 저장하고 month를 하나 증가시킴
                            else day += 7; //cday가 마지막 날을 넘지 않으면 day도 그냥 7일을 추가한 날로 저장
                        }
                        //윤달이 아닌 경우
                        if(cday > 28) { day = cday - 28; month += 1;}
                        //cday가 마지막 날인 28일을 넘으면 cday에서 마지막날인 28일을 뺀 값을 day에 저장하고 month를 하나 증가시킴
                        else day += 7; //cday가 마지막 날을 넘지 않으면 day도 그냥 7일을 추가한 날로 저장
                        break;
                    default: //나머지 달의 경우 마지막 날이 31일
                        if(cday > 31) {  //마지막 날인 31일을 cday가 넘는 경우
                            day = cday - 31; month += 1;
                            //cday가 마지막 날인 31일을 넘으면 cday에서 마지막날인 31일을 뺀 값을 day에 저장하고 month를 하나 증가시킴
                        }
                        else day += 7; //cday가 마지막 날을 넘지 않으면 day도 그냥 7일을 추가한 날로 저장
                        break;
                }
                if(month > 12) { //만약 month가 12월이 넘어가면 년도를 다음해로 수정하고, month를 1월로 지정.
                    year += 1; month = 1;
                }
                getFEDate(); //변경한 날짜에 따른 해당 주의 기간을 가져옴
                setWeekP(); //해당 주 기간을 textview 에 넣어 화면에 보여줌
                setTodoList(); //변경한 날짜에 따른 주에 해당하는 리스트를 재생성
            }
        });

        //오늘 버튼을 누르면 오늘이 있는 주로 이동
        findViewById(R.id.week_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentWeek(); //오늘 날짜와 요일을 year, day, week, dow 에 저장
                getFEDate(); //오늘 날짜에 따른 해당 주의 기간을 가져옴
                setWeekP(); //해당 주 기간을 textview 에 넣어 화면에 보여줌
                setTodoList(); //오늘 날짜에 따른 주에 해당하는 리스트를 재생성
            }
        });

        //각각 배경색에 해당하는 리스트를 클릭함에 따라 edit 하는 화면으로 intent 하는 클릭리스너 등록
        //edit 하는 경우에는 주 변경 기능이 없으므로 해당 주의 년, 월요일/일요일 을 intent로 보냄
        bLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //파란 배경 리스트 아이템 클릭 시, color를 B 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //파란색 배경에 있는 리스트 클릭하면
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                //WeekEditTodoActivity 화면으로 이동하는 인텐트 설정
                editTodoIntent.putExtra("WYear", WYear); //주의 년도를 인텐트에 저장
                editTodoIntent.putExtra("Mmonth", Mmonth); //주의 월요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Mday", MDate); //주의 월요일 일을 인텐트에 저장
                editTodoIntent.putExtra("Smonth", Smonth); //주의 일요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Sday", SDate); //주의 일요일 일을 인텐트에 저장
                editTodoIntent.putExtra("color", "B"); //클릭한 배경색을 B(blue)로 해 인텐트에 저장. 이 색이 edit 화면에서 색 체크 default 값이 됨.
                startActivityForResult(editTodoIntent, 1); //인텐트 종료시 변경된 값을 반영하기 위해 결과값을 받는 인텐트로 액티비시 시작
            }
        });
        gLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //초록색 배경 리스트 아이템 클릭 시, color를 G 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //초록색 배경에 있는 리스트 클릭하면
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                //WeekEditTodoActivity 화면으로 이동하는 인텐트 설정
                editTodoIntent.putExtra("WYear", WYear); //주의 년도를 인텐트에 저장
                editTodoIntent.putExtra("Mmonth", Mmonth); //주의 월요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Mday", MDate); //주의 월요일 일을 인텐트에 저장
                editTodoIntent.putExtra("Smonth", Smonth); //주의 일요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Sday", SDate); //주의 일요일 일을 인텐트에 저장
                editTodoIntent.putExtra("color", "G"); //클릭한 배경색을 G(green)로 해 인텐트에 저장. 이 색이 edit 화면에서 색 체크 default 값이 됨.
                startActivityForResult(editTodoIntent, 1); //인텐트 종료시 변경된 값을 반영하기 위해 결과값을 받는 인텐트로 액티비시 시작
            }
        });
        pLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //분홍색 배경 리스트 아이템 클릭 시, color를 P 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //분홍색 배경에 있는 리스트 클릭하면
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                //WeekEditTodoActivity 화면으로 이동하는 인텐트 설정
                editTodoIntent.putExtra("WYear", WYear); //주의 년도를 인텐트에 저장
                editTodoIntent.putExtra("Mmonth", Mmonth); //주의 월요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Mday", MDate); //주의 월요일 일을 인텐트에 저장
                editTodoIntent.putExtra("Smonth", Smonth); //주의 일요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Sday", SDate); //주의 일요일 일을 인텐트에 저장
                editTodoIntent.putExtra("color", "P"); //클릭한 배경색을 P(pink)로 해 인텐트에 저장. 이 색이 edit 화면에서 색 체크 default 값이 됨.
                startActivityForResult(editTodoIntent, 1); //인텐트 종료시 변경된 값을 반영하기 위해 결과값을 받는 인텐트로 액티비시 시작
            }
        });
        vLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //보라색 배경 리스트 아이템 클릭 시, color를 V 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //보라색 배경에 있는 리스트 클릭하면
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                //WeekEditTodoActivity 화면으로 이동하는 인텐트 설정
                editTodoIntent.putExtra("WYear", WYear); //주의 년도를 인텐트에 저장
                editTodoIntent.putExtra("Mmonth", Mmonth); //주의 월요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Mday", MDate); //주의 월요일 일을 인텐트에 저장
                editTodoIntent.putExtra("Smonth", Smonth); //주의 일요일 월을 인텐트에 저장
                editTodoIntent.putExtra("Sday", SDate); //주의 일요일 일을 인텐트에 저장
                editTodoIntent.putExtra("color", "V"); //클릭한 배경색을 V(violet)로 해 인텐트에 저장. 이 색이 edit 화면에서 색 체크 default 값이 됨.
                startActivityForResult(editTodoIntent, 1); //인텐트 종료시 변경된 값을 반영하기 위해 결과값을 받는 인텐트로 액티비시 시작
            }
        });
    }

    public void getFEDate() {   //오늘 날짜에 맞춰 주의 월요일과 일요일을 가져옴
        MDate = day; SDate = day; //월요일, 일요일의 일을 저장하는 변수에 일단 day 값을 넣음
        Mmonth = month; Smonth = month; //월요일, 일요일의 월을 저장하는 변수에 일단 month 값을 넣음
        WYear = year; //년도는 year 값 넣음
        if(dow == 1) {  //오늘이 일요일인 경우
            int cday = MDate - 6; //월요일은 day로부터 6일 전이므로 MDate에서 6일 뺀 수를 cday에 넣음
            if(cday < 1) { //월요일을 구하기 위해 구한 값 cday가 1보다 작으면
                switch (Mmonth) {
                    case 5:case 7:case 10:case 12: //5,7,10,12월의 전달 4,6,9,11월은 마지막날이 30일
                        MDate = 30 + cday; break; //MDate에 전달의 마지막날 30일에 cday를 더한 값을 넣어 월요일 일을 구함
                    case 3: //전달이 2월인 경우
                        if((WYear % 4 == 0 && WYear % 100 != 0) || WYear % 400 == 0) MDate = 29 + cday;
                        //윤달인 경우 Mdate에 전달의 마지막날 29일에 cday를 더한 값을 넣어 월요일의 일을 구함
                        else MDate = 28 + cday; //윤달이 아닌 경우 Mdate에 전달의 마지막날 28일에 cday를 더한 값을 넣어 월요일의 일을 구함
                        break;
                    default: //그 외의 경우는 전달 마지막날이 31일인 경우
                        MDate = 31 + cday; break; //MDate에 전달의 마지막날 31일에 cday를 더한 값을 넣어 월요일 일을 구함
                }
                Mmonth -= 1; //전달로 이동했으므로 월요일의 month인 Mmonth 의 값을 하나 줄임
            }
            else MDate -= 6; //cday가 1 이상이면 월의 이동이 없으므로 그냥 MDate에 6일을 빼서 해당 주의 월요일 일을 구함
            if(Mmonth == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                WYear -= 1;
                Mmonth = 12;
            }
        }
        else {  //오늘이 월~토 인 경우
            //오늘이 있는 주의 월요일 구하기
            for (int i = dow; i > 2; i--) { //i를 오늘 요일인 dow 부터 시작해 월요일을 의미하는 2까지 반복하는데 i 값을 1씩 줄여가며 반복함
                MDate--; //월요일에 해당하는 일을 하나 줄임. i가 월요일 의미하는 2가 될때까지 계속 줄여서 월요일의 일로 만듦.
                if (MDate < 1) { //1일 이전으로 넘어가면 경우에 따라 MDate 와 Mmonth를 재 설정
                    switch (month) {
                        case 5:case 7:case 10:case 12: //5,7,10,12월인 경우 전달 4,6,9,11월의 마지막날이 30일
                            MDate = 30; //월요일의 일(MDate)을 30일로 재설정
                            break;
                        case 3: //전달이 2월인 경우
                            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) MDate = 29; //윤년이면 월요일의 일(MDate)을 29일로 재설정
                            else MDate = 28; //윤년이 아니면 월요일의 일(MDate)을 28일로 재설정
                            break;
                        default: //그 외의 경우 전달의 마지막 날이 31일임
                            MDate = 31; //월요일의 일(MDate)을 31일로 재설정
                            break;
                    }
                    Mmonth -= 1; //이전달로 넘어갔으니 월요일의 월을 하나 감소시킴
                    if (Mmonth == 0) { //만약 1월에서 전달로 가 Mmonth가 0이 되면 Mmonth 를 12월로 지정.
                        WYear -= 1; Mmonth = 12;
                    }
                }
            }
            //오늘이 있는 주의 일요일 구하기
            for (int i = dow; i <= 7; i++) {//i를 오늘의 요일부터 7까지 해서 1씩 증가시키며 반복문 실행.
                //7은 토요일을 의미하긴 하지만 일단 오늘 요일에서 일을 증가시킨 다음에 i값이 증가되는데 토요일인 7도 포함을 하므로
                //횟수를 따지면 결과적으로 일을 증가시키는 것을 한 번을 더 실행하게 되므로 일요일을 구할 수 있게됨.
                SDate++; //일요일의 일을 의미하는 SDate 값을 1 증가.
                switch (month) { //29,30,31일을 넘어가면 경우에 따라 SDate, Smonth를 재설정
                    case 4:case 6:case 9:case 11: //4,6,9,11월인 경우 마지막 날이 30일
                        if (SDate == 31) { //일이 31일이 되는 경우
                            SDate = 1; //일요일 일인 SDate를 1로 하고
                            Smonth += 1; //일요일 월인 Smonth를 하나 증가시킴
                        }
                        break;
                    case 2: //2월인 경우
                        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) { //윤년이면
                            if (SDate == 30) SDate = 1; //일이 30일이 되는 경우 일요일 일인 SDate를 1로 하고
                        }
                        if (SDate == 29) SDate = 1; //윤년이 아닌 경우 일이 29일이 되는 경우 일요일 일인 SDate를 1로 하고
                        if (SDate == 1) Smonth += 1; //월이 넘어가 SDate가 1이 되면 일요일 월인 Smonth를 하나 증가시킴
                        break;
                    default: //그 외의 경우는 마지막 날이 31일인 경우
                        if (SDate == 32) { //일이 32일이 되는 경우
                            SDate = 1; //일요일 일인 SDate를 1로 하고
                            Smonth += 1; //일요일 월인 Smonth를 하나 증가시킴
                        }
                        break;
                }
                if (Smonth > 12) { //만약 Smonth가 12월이 넘어가면 Smonth를 1월로 지정.
                    Smonth = 1; WYear +=1;
                }
            }
        }
    }

    public void getCurrentWeek() {  //오늘 날짜와 요일을 가져와 변수에 저장
        c = Calendar.getInstance(); //오늘날짜 가져와 c 에 넣음
        year = c.get(Calendar.YEAR); //오늘 year 저장
        month = c.get(Calendar.MONTH)+1; //오늘 month 저장. 0부터 시작해서 1 추가.
        day = c.get(Calendar.DAY_OF_MONTH); //오늘 day 저장
        dow = c.get(Calendar.DAY_OF_WEEK); //오늘 요일 저장
    }

    public void setWeekP() {    //주의 월요일과 일요일을 보여주기 위해 textView 에 set 하고, DB에 사용하기 위해 해당 월요일을 wd 에 저장
        weekp.setText(WYear + "   " + Mmonth + "/" + MDate + " ~ " + Smonth + "/" + SDate);
        //2017   8/28 ~ 9/3   형식으로 기간을 보여주는 textview 에다가 기간을 set 해줌.
        int syear = WYear; int smonth = Mmonth; int sdate = MDate;
        wd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sdate);
        //DB에 사용하기 위해 해당 주의 월요일을 String 타입으로 변환해서 합쳐 String 타입인 wd 에 저장
    }

    @Override   //intent가 finish 된 뒤 실행. extra 값을 가져와 날짜를 바꾸고 그에 따라 해당 주 와 todolist 내용을 바꿈.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {   //+로 내용 add 한 경우에는 주를 변경할 수 있으니 그에 맞게 날짜를 받아와 주를 변경하고 리스트 생성
            year = data.getExtras().getInt("cYear"); //바꾼 주에 포함되는 년을 가져와 year 에 저장
            month = data.getExtras().getInt("cMonth"); //바꾼 주에 포함되는 월을 가져와 month 에 저장
            day = data.getExtras().getInt("cDay"); //바꾼 주에 포함되는 일을 가져와 day 에 저장
            getFEDate(); //바꾼 주에 포함되는 일로부터 주의 기간을 구함
            setWeekP(); //바꾼 주 기간을 주를 보여주는 textview 에 설정
            setTodoList(); //바뀐 날짜에 맞춰 리스트 재생성
        }
        else { //그냥 edit 한 경우에는 주 변동이 없으므로 받는 데이터 없이 그냥 인텐트 종료하고, 리스트 재생성
            setTodoList(); //리스트 재생성
        }
    }

    //DB에서 해당 주의 todolist 를 가져와 리스트에 넣는 함수. 각각의 색에 따라 select 문을 실행하고 각각 색에 맞춰 리스트를 넣는다.
    public void setTodoList() {
        int count = 0; //해당 리스트 내용이 있는지 없는지를 구분하기 위한 변수.
        SQLiteDatabase db = wHelper.getReadableDatabase(); //select 하기 위해 SQLiteDatabase 생성
        //파란색 배경. 파란색 배경에 해당하는(color = "B") todolist를 DB에서 가져와 리스트 생성
        blAdapter.clearItem(); //중복 피하기 위해 어댑터의 아이템 초기화
        Cursor c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'B'", null);
        //커서를 이용해 date가 해당 주의 월요일(wd)이고 color 가 'B'인(blue) 데이터의 id, content, checked(체크여부) 가져옴
        while(c.moveToNext()) {
            blAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2)); //id, content, checked 값을 어댑터 아이템에 넣음
            count++; //결과값이 나올 때 마다 count를 증가시킴
        }
        if (count == 0) { //count가 0이면 todolist가 없다는 것이고, 그럴 경우 해당 배경과 리스트가 보이지 않게 함
            bLL.setVisibility(GONE);
        }
        else { //count가 0이 아니어서 todolist 가 있으면
            bLL.setVisibility(View.VISIBLE); //해당 배경을 보이게 하고
            blAdapter.notifyDataSetChanged(); //리스트를 넣음. 변경사항 어댑터에 알려줌.
            count = 0; //다음 배경색에 리스트가 있는지 구분 위해 count 값 다시 0으로 초기화.
        }

        //초록색 배경. 초록색 배경에 해당하는(color = "G") todolist 를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        glAdapter.clearItem(); //중복 피하기 위해 어댑터의 아이템 초기화
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'G'", null);
        //커서를 이용해 date가 해당 주의 월요일(wd)이고 color 가 'G'인(green) 데이터의 id, content, checked(체크여부) 가져옴
        while(c.moveToNext()) {
            glAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2)); //id, content, checked 값을 어댑터 아이템에 넣음
            count++; //결과값이 나올 때 마다 count를 증가시킴
        }
        if (count == 0) { //count가 0이면 todolist가 없다는 것이고, 그럴 경우 해당 배경과 리스트가 보이지 않게 함
            gLL.setVisibility(GONE);
        }
        else { //count가 0이 아니어서 todolist 가 있으면
            gLL.setVisibility(View.VISIBLE); //해당 배경을 보이게 하고
            glAdapter.notifyDataSetChanged(); //리스트를 넣음. 변경사항 어댑터에 알려줌.
            count = 0; //다음 배경색에 리스트가 있는지 구분 위해 count 값 다시 0으로 초기화.
        }

        //분홍색 배경. 분홍색 배경에 해당하는(color = "P") todolist를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        plAdapter.clearItem(); //중복 피하기 위해 어댑터의 아이템 초기화
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'P'", null);
        //커서를 이용해 date가 해당 주의 월요일(wd)이고 color 가 'P'인(pink) 데이터의 id, content, checked(체크여부) 가져옴
        while(c.moveToNext()) {
            plAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2)); //id, content, checked 값을 어댑터 아이템에 넣음
            count++; //결과값이 나올 때 마다 count를 증가시킴
        }
        if (count == 0) { //count가 0이면 todolist가 없다는 것이고, 그럴 경우 해당 배경과 리스트가 보이지 않게 함
            pLL.setVisibility(GONE);
        }
        else { //count가 0이 아니어서 todolist 가 있으면
            pLL.setVisibility(View.VISIBLE); //해당 배경을 보이게 하고
            plAdapter.notifyDataSetChanged(); //리스트를 넣음. 변경사항 어댑터에 알려줌.
            count = 0; //다음 배경색에 리스트가 있는지 구분 위해 count 값 다시 0으로 초기화.
        }

        //보라색 배경. 보라색 배경에 해당하는(color = "V") todolist를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        vlAdapter.clearItem(); //중복 피하기 위해 어댑터의 아이템 초기화
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'V'", null);
        //커서를 이용해 date가 해당 주의 월요일(wd)이고 color 가 'V'인(violet) 데이터의 id, content, checked(체크여부) 가져옴
        while(c.moveToNext()) {
            vlAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2)); //id, content, checked 값을 어댑터 아이템에 넣음
            count++; //결과값이 나올 때 마다 count를 증가시킴
        }
        if (count == 0) { //count가 0이면 todolist가 없다는 것이고, 그럴 경우 해당 배경과 리스트가 보이지 않게 함
            vLL.setVisibility(GONE);
        }
        else { //count가 0이 아니어서 todolist 가 있으면
            vLL.setVisibility(View.VISIBLE); //해당 배경을 보이게 하고
            vlAdapter.notifyDataSetChanged(); //리스트를 넣음. 변경사항 어댑터에 알려줌.
            count = 0; //count 값 다시 0으로 초기화.
        }

        c.close(); //커서 닫음
        db.close(); //db 닫음
    }

    public void weIntentTodo(View v) { //각각 색의 배경을 눌렀을 때 todolist 를 수정하는 화면으로 이동하는 함수
        String col=""; //일단 어떤 배경색을 눌렀는지 저장하는 col 값을 빈 상태로 초기화
        switch (v.getId()) { //누른 배경의 색을 col에 저장한 뒤 intent로 값을 보냄. 이 색의 값을 default로 함.
            case R.id.week_todo_b: //파란색 배경을 눌렀을 경우(id값이 파란 배경 아이디)
                col = "B";break; //col = "B"로 저장
            case R.id.week_todo_g: //초록색 배경을 눌렀을 경우(id값이 초록색 배경 아이디)
                col = "G";break; //col = "G"로 저장
            case R.id.week_todo_p: //분홍색 배경을 눌렀을 경우(id값이 분홍색 배경 아이디)
                col = "P";break; //col = "P"로 저장
            case R.id.week_todo_v: //보라색 배경을 눌렀을 경우(id값이 보라색 배경 아이디)
                col = "V";break; //col = "V"로 저장
        }
        //필요한 정보들 인텐트로 보내고 인텐트 실행
        Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
        //todolist 수정 화면인 WeekEditTodoActivity로 인텐트 연결
        editTodoIntent.putExtra("WYear", WYear); //해당 주의 년을 인텐트에 저장
        editTodoIntent.putExtra("Mmonth", Mmonth); //해당 주의 월요일 월을 인텐트에 저장
        editTodoIntent.putExtra("Mday", MDate); //해당 주의 월요일 일을 인텐트에 저장
        editTodoIntent.putExtra("Smonth", Smonth); //해당 주의 일요일 월을 인텐트에 저장
        editTodoIntent.putExtra("Sday", SDate); //해당 주의 일요일 일을 인텐트에 저장
        editTodoIntent.putExtra("color", col); //수정 화면 들어갔을 때 누른 배경색을 전달해 배경색 선택에서 해당 색이 체크되어있도록 함
        startActivityForResult(editTodoIntent, 1); //인텐트 종료되었을 때 변경사항을 적용하기 위해 값을 전달받는 인텐트를 실행
    }
}
