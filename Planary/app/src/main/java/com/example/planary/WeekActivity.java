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

public class WeekActivity extends Activity {
    int year, month, day, dow; //해당 년,월,일,요일을 저장하는 변수
    int MDate, SDate; //해당 주의 월요일, 일요일의 일을 저장하는 변수
    int Mmonth, Smonth; //해당 주의 월요일, 일요일의 월을 저장하는 변수
    int WYear; //주에 따른 년도를 저장하는 변수
    String wd; //DB 검색을 위한 해당 주의 월요일을 저장하는 변수
    TextView weekp;
    Calendar c;
    ListView bLV, gLV, pLV, vLV;
    LinearLayout bLL, gLL, pLL, vLL;
    WeekListAdapter blAdapter, glAdapter, plAdapter, vlAdapter;
    WeekDB wHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    // + 버튼을 누르면 날짜에 대한 정보를 넘기면서 일정 추가 화면으로 넘어감
                Intent addTodoIntent = new Intent(WeekActivity.this, WeekAddTodoActivity.class);
                addTodoIntent.putExtra("year", year);
                addTodoIntent.putExtra("month", month);
                addTodoIntent.putExtra("day", day);
                addTodoIntent.putExtra("dayofweek", dow);
                startActivityForResult(addTodoIntent, 1);
            }
        });
        //오늘 날짜에 따라 주 지정하고 리스트 불러옴
        weekp = (TextView)findViewById(R.id.week_date);
        wHelper = new WeekDB(this); //DB Helper 생성
        getCurrentWeek();
        getFEDate();
        setWeekP();

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

        setTodoList(); //리스트 생성

        //전주로 이동하는 함수
        findViewById(R.id.week_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cday = day - 7;
                if(cday < 1) { //일주일 전으로 이동했을 때 전달로 넘어가야 하는 날짜가 되면 month를 하나 줄이고, 그에 맞게 day 값도 수정
                    switch (month) {
                        case 5:case 7:case 10:case 12:
                            day = 30 + cday; break;
                        case 3:
                            if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) day = 29 + cday;
                            else day = 28 + cday;
                            break;
                        default:
                            day = 31 + cday; break;
                    }
                    month -= 1;
                }
                else day -= 7;
                if(month == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                    year -= 1;
                    month = 12;
                }
                getFEDate();
                setWeekP();
                setTodoList();
            }
        });

        //다음주로 이동하는 함수
        findViewById(R.id.week_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cday = day + 7;
                switch (month) { //일주일 뒤로 이동했을 때 다음달로 넘어가야 하는 날짜가 되면 month를 하나 증가시키고, day 알맞게 수정
                    case 4:case 6:case 9:case 11:
                        if(cday > 30) {
                            day = cday - 30; month += 1;
                        }
                        else day += 7;
                        break;
                    case 2:
                        if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){
                            if(cday > 29) { day = cday - 29; month += 1;}
                            else day += 7;
                        }
                        if(cday > 28) { day = cday - 28; month += 1;}
                        else day += 7;
                        break;
                    default:
                        if(cday > 31) {
                            day = cday - 31; month += 1;
                        }
                        else day += 7;
                        break;
                }
                if(month > 12) { //만약 month가 12월이 넘어가면 년도를 다음해로 수정하고, month를 1월로 지정.
                    year += 1; month = 1;
                }
                getFEDate();
                setWeekP();
                setTodoList();
            }
        });

        //오늘 버튼을 누르면 오늘이 있는 주로 이동
        findViewById(R.id.week_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentWeek();
                getFEDate();
                setWeekP();
                setTodoList();
            }
        });

        //각각 배경색에 해당하는 리스트를 클릭함에 따라 edit 하는 화면으로 intent 하는 클릭리스너 등록
        //edit 하는 경우에는 주 변경 기능이 없으므로 해당 주의 년, 월요일/일요일 을 intent로 보냄
        bLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //파란 배경 리스트 아이템 클릭 시, color를 B 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                editTodoIntent.putExtra("WYear", WYear);
                editTodoIntent.putExtra("Mmonth", Mmonth);
                editTodoIntent.putExtra("Mday", MDate);
                editTodoIntent.putExtra("Smonth", Smonth);
                editTodoIntent.putExtra("Sday", SDate);
                editTodoIntent.putExtra("color", "B");
                startActivityForResult(editTodoIntent, 1);
            }
        });
        gLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //초록색 배경 리스트 아이템 클릭 시, color를 G 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                editTodoIntent.putExtra("WYear", WYear);
                editTodoIntent.putExtra("Mmonth", Mmonth);
                editTodoIntent.putExtra("Mday", MDate);
                editTodoIntent.putExtra("Smonth", Smonth);
                editTodoIntent.putExtra("Sday", SDate);
                editTodoIntent.putExtra("color", "G");
                startActivityForResult(editTodoIntent, 1);
            }
        });
        pLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //분홍색 배경 리스트 아이템 클릭 시, color를 P 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                editTodoIntent.putExtra("WYear", WYear);
                editTodoIntent.putExtra("Mmonth", Mmonth);
                editTodoIntent.putExtra("Mday", MDate);
                editTodoIntent.putExtra("Smonth", Smonth);
                editTodoIntent.putExtra("Sday", SDate);
                editTodoIntent.putExtra("color", "P");
                startActivityForResult(editTodoIntent, 1);
            }
        });
        vLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { //보라색 배경 리스트 아이템 클릭 시, color를 V 로 하고 인텐트 실행
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
                editTodoIntent.putExtra("WYear", WYear);
                editTodoIntent.putExtra("Mmonth", Mmonth);
                editTodoIntent.putExtra("Mday", MDate);
                editTodoIntent.putExtra("Smonth", Smonth);
                editTodoIntent.putExtra("Sday", SDate);
                editTodoIntent.putExtra("color", "V");
                startActivityForResult(editTodoIntent, 1);
            }
        });
    }

    public void getFEDate() {   //오늘 날짜에 맞춰 주의 월요일과 일요일을 가져옴
        MDate = day; SDate = day;
        Mmonth = month; Smonth = month;
        WYear = year;
        if(dow == 1) {  //오늘이 일요일인 경우
            int cday = MDate - 6;
            if(cday < 1) { //일주일 전으로 이동했을 때 전달로 넘어가야 하는 날짜가 되면 month를 하나 줄이고, 그에 맞게 day 값도 수정
                switch (Mmonth) {
                    case 5:case 7:case 10:case 12:
                        MDate = 30 + cday; break;
                    case 3:
                        if((WYear % 4 == 0 && WYear % 100 != 0) || WYear % 400 == 0) MDate = 29 + cday;
                        else MDate = 28 + cday;
                        break;
                    default:
                        MDate = 31 + cday; break;
                }
                Mmonth -= 1;
            }
            else MDate -= 6;
            if(Mmonth == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                WYear -= 1;
                Mmonth = 12;
            }
        }
        else {  //오늘이 월~토 인 경우
            for (int i = dow; i > 2; i--) {
                MDate--;
                if (MDate < 1) { //1일 이전으로 넘어가면 경우에 따라 MDate 와 Mmonth를 재 설정
                    switch (month) {
                        case 5:
                        case 7:
                        case 10:
                        case 12:
                            MDate = 30;
                            break;
                        case 3:
                            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) MDate = 29;
                            else MDate = 28;
                            break;
                        default:
                            MDate = 31;
                            break;
                    }
                    Mmonth -= 1;
                    if (Mmonth == 0) { //만약 1월에서 전달로 가 Mmonth가 0이 되면 Mmonth 를 12월로 지정.
                        WYear -= 1; Mmonth = 12;
                    }
                }
            }
            for (int i = dow; i <= 7; i++) {
                SDate++;
                switch (month) { //29,30,31일을 넘어가면 경우에 따라 SDate, Smonth를 재설정
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        if (SDate == 31) {
                            SDate = 1;
                            Smonth += 1;
                        }
                        break;
                    case 2:
                        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                            if (SDate == 30) SDate = 1;
                        }
                        if (SDate == 29) SDate = 1;
                        if (SDate == 1) Smonth += 1;
                        break;
                    default:
                        if (SDate == 32) {
                            SDate = 1;
                            Smonth += 1;
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
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH)+1;
        day = c.get(Calendar.DAY_OF_MONTH);
        dow = c.get(Calendar.DAY_OF_WEEK);
    }

    public void setWeekP() {    //주의 월요일과 일요일을 보여주기 위해 textView 에 set 하고, DB에 사용하기 위해 해당 월요일을 wd 에 저장
        weekp.setText(WYear + "   " + Mmonth + "/" + MDate + " ~ " + Smonth + "/" + SDate);
        int syear = WYear; int smonth = Mmonth; int sdate = MDate;
        wd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sdate);
    }

    @Override   //intent가 finish 된 뒤 실행. extra 값을 가져와 날짜를 바꾸고 그에 따라 해당 주 와 todolist 내용을 바꿈.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {   //+로 내용 add 한 경우에는 주를 변경할 수 있으니 그에 맞게 날짜를 받아와 주를 변경하고 리스트 생성
            year = data.getExtras().getInt("cYear");
            month = data.getExtras().getInt("cMonth");
            day = data.getExtras().getInt("cDay");
            getFEDate();
            setWeekP();
            setTodoList();
        }
        else { //그냥 edit 한 경우에는 주 변동이 없으므로 받는 데이터 없이 그냥 인텐트 종료하고, 리스트 재생성
            setTodoList();
        }
    }

    //DB에서 해당 주의 todolist 를 가져와 리스트에 넣는 함수. 각각의 색에 따라 select 문을 실행하고 각각 색에 맞춰 리스트를 넣는다.
    public void setTodoList() {
        int count = 0; //해당 리스트 내용이 있는지 없는지를 구분하기 위한 변수.
        SQLiteDatabase db = wHelper.getReadableDatabase();
        //파란색 배경. 파란색 배경에 해당하는(color = "B") todolist를 DB에서 가져와 리스트 생성
        blAdapter.clearItem();
        Cursor c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'B'", null);
        while(c.moveToNext()) {
            blAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2));
            count++; //결과값이 나올 때 마다 count를 증가시킴
        }
        if (count == 0) { //count가 0이면 todolist가 없다는 것이고, 그럴 경우 해당 배경과 리스트가 보이지 않게 함
            bLL.setVisibility(GONE);
        }
        else { //todolist 가 있으면 해당 배경을 보이게 하고 리스트를 넣음. 다음 배경색에 리스트가 있는지 구분 위해 count 값 다시 0으로 초기화.
            bLL.setVisibility(View.VISIBLE);
            blAdapter.notifyDataSetChanged();
            count = 0;
        }

        //초록색 배경. 초록색 배경에 해당하는(color = "G") todolist 를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        glAdapter.clearItem();
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'G'", null);
        while(c.moveToNext()) {
            glAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2));
            count++;
        }
        if (count == 0) {
            gLL.setVisibility(GONE);
        }
        else {
            gLL.setVisibility(View.VISIBLE);
            glAdapter.notifyDataSetChanged();
            count = 0;
        }

        //분홍색 배경. 분홍색 배경에 해당하는(color = "P") todolist를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        plAdapter.clearItem();
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'P'", null);
        while(c.moveToNext()) {
            plAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2));
            count++;
        }
        if (count == 0) {
            pLL.setVisibility(GONE);
        }
        else {
            pLL.setVisibility(View.VISIBLE);
            plAdapter.notifyDataSetChanged();
            count = 0;
        }

        //보라색 배경. 보라색 배경에 해당하는(color = "V") todolist를 DB에서 가져와 리스트 생성. 방식은 파란색과 동일.
        vlAdapter.clearItem();
        c = db.rawQuery("select _id, content, checked from plaweektodo where date = '"+ wd + "'" +
                " AND color = 'V'", null);
        while(c.moveToNext()) {
            vlAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2));
            count++;
        }
        if (count == 0) {
            vLL.setVisibility(GONE);
        }
        else {
            vLL.setVisibility(View.VISIBLE);
            vlAdapter.notifyDataSetChanged();
            count = 0;
        }

        c.close();
        db.close();
    }

    public void weIntentTodo(View v) { //각각 색의 배경을 눌렀을 때 todolist 를 수정하는 화면으로 이동하는 함수
        String col="";
        switch (v.getId()) { //누른 배경의 색을 col에 저장한 뒤 intent로 값을 보냄. 이 색의 값을 default로 함.
            case R.id.week_todo_b:
                col = "B";break;
            case R.id.week_todo_g:
                col = "G";break;
            case R.id.week_todo_p:
                col = "P";break;
            case R.id.week_todo_v:
                col = "V";break;
        }
        //필요한 정보들 인텐트로 보내고 인텐트 실행
        Intent editTodoIntent = new Intent(WeekActivity.this, WeekEditTodoActivity.class);
        editTodoIntent.putExtra("WYear", WYear);
        editTodoIntent.putExtra("Mmonth", Mmonth);
        editTodoIntent.putExtra("Mday", MDate);
        editTodoIntent.putExtra("Smonth", Smonth);
        editTodoIntent.putExtra("Sday", SDate);
        editTodoIntent.putExtra("color", col);
        startActivityForResult(editTodoIntent, 1);
    }
}
