package com.example.planary.Week;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.planary.R;

public class WeekAddTodoActivity extends Activity {
    int year, month, day, dow; //년,월,일,요일을 저장하는 변수
    int MDate, SDate, Mmonth, Smonth, WYear; //해당 주의 월,일요일에 대한 정보를 저장하고, 년을 저장하는 변수
    String wd; //DB에서 날짜를 구별하기 위해 사용하는 변수
    Intent intent;
    TextView weekap;
    ImageView blue, green, pink, violet;
    String col; //체크된 상태의 색 저장하는 변수
    EditText addEdit;
    ListView addList;
    WeekDB helper;
    WeekTodoAdapter wTodoAdapter;
    WeekTodoItem dei;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_add_todo);
        //인텐트로 날짜 대한 데이터를 받아오고, id를 가져와 변수에 적용시킴
        intent = getIntent();
        year = intent.getExtras().getInt("year");
        month = intent.getExtras().getInt("month");
        day = intent.getExtras().getInt("day");
        dow = intent.getExtras().getInt("dayofweek");
        weekap = (TextView)findViewById(R.id.week_adate);
        blue = (ImageView)findViewById(R.id.week_alistc_b);
        green = (ImageView)findViewById(R.id.week_alistc_g);
        pink = (ImageView)findViewById(R.id.week_alistc_p);
        violet = (ImageView)findViewById(R.id.week_alistc_v);

        //받아온 날짜에 대해 주를 구하고 textView 에 적용
        getFEDate();
        setWeekP();
        setCheck("B");  //default 값으로 파란색 배경에 체크되게 설정

        //일정들을 추가한 뒤 체크박스 모양(확인)을 누르면 DB에서 해당 데이터가 NEW 인지 OLD인지 체크하는 nn 을 OLD인 O 로 변경하고 인텐트 종료
        //주를 변경시켰으면 변경시킨 내용에 대한 정보도 함께전달
        findViewById(R.id.weektodoa_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("cYear", year);
                intent.putExtra("cMonth", month);
                intent.putExtra("cDay", day);
                setResult(RESULT_OK, intent);
                SQLiteDatabase db = helper.getReadableDatabase();
                String queryupd = String.format("update %s set nn='O' where nn = 'N';", "plaweektodo");
                db.execSQL(queryupd);
                db.close();
                finish();
            }
        });

        //전주로 이동하는 함수
        findViewById(R.id.week_aleft).setOnClickListener(new View.OnClickListener() {
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
                makeList();
            }
        });

        //다음주로 이동하는 함수
        findViewById(R.id.week_aright).setOnClickListener(new View.OnClickListener() {
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
                makeList();
            }
        });

        //일정 수정 할 수 있도록 만들어 놓은 어댑터 설정
        addEdit = (EditText)findViewById(R.id.weektodoa_edit);
        addList = (ListView)findViewById(R.id.weektodoa_list);
        helper = new WeekDB(this);
        wTodoAdapter = new WeekTodoAdapter();
        addList.setAdapter(wTodoAdapter);
        //클릭한 리스트 내용을 수정할 수 있도록 해당 내용을 dei 에다가 넣고 addEdit 에 클릭 리스트 내용 넣음
        addList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = wTodoAdapter.getItem(position);
                addEdit.setText(dei.getContent_ed());
            }
        });

    }

    public void getFEDate() { //해당하는 날짜의 주를 가져옴(WeekActivity의 함수와 동일)
        MDate = day; SDate = day;
        Mmonth = month; Smonth = month;
        WYear = year;
        if(dow == 1) { //요일이 일요일인경우
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
        else { //요일이 월~토 인 경우
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

    public void setWeekP() {    //주의 월요일과 일요일을 보여주기 위해 textView 에 set 하고, DB에 사용하기 위해 해당 월요일을 wd 에 저장
        weekap.setText(WYear + "   " + Mmonth + "/" + MDate + " ~ " + Smonth + "/" + SDate);
        int syear = WYear; int smonth = Mmonth; int sdate = MDate;
        wd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sdate);
    }

    public void setCheck(String color) {//인자로 받은 값만 체크표시를 하고 나머지는 체크표시를 없애는 함수
        blue.setVisibility(View.GONE);
        green.setVisibility(View.GONE);
        pink.setVisibility(View.GONE);
        violet.setVisibility(View.GONE);
        switch (color) {
            case "B":
                blue.setVisibility(View.VISIBLE); col = "B";break;
            case "G":
                green.setVisibility(View.VISIBLE); col = "G";break;
            case "P":
                pink.setVisibility(View.VISIBLE); col = "P";break;
            case "V":
                violet.setVisibility(View.VISIBLE); col = "V";break;
        }
    }

    public void clickCheck(View v) {    //배경색을 선택함에 따라 체크표시를 바꾸고 해당 색에 따른 리스트를 생성하는 함수
        switch (v.getId()) {
            case R.id.week_alist_b:
                setCheck("B");break;
            case R.id.week_alist_g:
                setCheck("G");break;
            case R.id.week_alist_p:
                setCheck("P");break;
            case R.id.week_alist_v:
                setCheck("V");break;
        }
        makeList();
    }

    public void makeList() {    //선택 배경 색에 해당하는 데이터들을 가져와 리스트를 설정
        wTodoAdapter.clearItem();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select _id, content from plaweektodo where date = '"+ wd +
                "' AND color = '" + col + "' AND nn = 'N'", null);
        while(c.moveToNext()) {
            wTodoAdapter.addItem(c.getInt(0), c.getString(1));
        }
        c.close();
        db.close();
        wTodoAdapter.notifyDataSetChanged();
    }

    public void addTodo(View v) { //+, 수정 버튼을 누를때 실행. DB에 내용을 삽입, 수정 하고 리스트 다시 재설정
        SQLiteDatabase db = helper.getWritableDatabase();
        String str_cont;
        switch(v.getId()) {
            case R.id.weektodoa_add: //내용 추가 시
                str_cont = addEdit.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s');",
                        "plaweektodo", wd, str_cont, "F", col, "N");
                db.execSQL(queryadd);
                break;
            case R.id.weektodoa_upd: //내용 수정 시
                str_cont = addEdit.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "plaweektodo", str_cont, dei.getEd_id());
                db.execSQL(queryupd);
                break;
        }
        addEdit.setText("");
        makeList();
        helper.close();
        db.close();
    }
}
