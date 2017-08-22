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

public class WeekAddTodoActivity extends Activity { //todolist를 추가하는 화면
    int year, month, day, dow; //년,월,일,요일을 저장하는 변수
    int MDate, SDate, Mmonth, Smonth, WYear; //해당 주의 월,일요일에 대한 정보를 저장하고, 년을 저장하는 변수
    String wd; //DB에서 날짜를 구별하기 위해 사용하는 변수
    Intent intent; //인텐트를 전달받아 저장하기 위한 인텐트
    TextView weekap; //주 기간을 보여주기 위한 textview
    ImageView blue, green, pink, violet; //배경색 선택을 위한 imageview. 파란색, 초록색, 분홍색, 보라색 있음.
    String col; //체크된 상태의 색 저장하는 변수
    EditText addEdit; //todolist 내용 추가하고 수정하기 위한 edittext
    ListView addList; //이 화면에서 새로 만든 todolist 를 보여주기 위한 리스트뷰
    WeekDB helper; //WeekDB 사용하기 위한 helper
    WeekTodoAdapter wTodoAdapter; //리스트뷰에 원하는 모습으로 내용이 보이게 하기 위한 어댑터
    WeekTodoItem dei; //클릭한 리스트의 내용을 저장하는 아이템
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_add_todo); //week_add_todo 레이아웃과 연결

        intent = getIntent();//인텐트 받아옴
        year = intent.getExtras().getInt("year"); //년을 받아와 year에 저장
        month = intent.getExtras().getInt("month"); //월을 받아와 month에 저장
        day = intent.getExtras().getInt("day"); //일을 받아와 day에 저장
        dow = intent.getExtras().getInt("dayofweek"); //요일을 받아와 dow에 저장
        //id를 가져와 변수에 적용시킴
        weekap = (TextView)findViewById(R.id.week_adate);
        blue = (ImageView)findViewById(R.id.week_alistc_b);
        green = (ImageView)findViewById(R.id.week_alistc_g);
        pink = (ImageView)findViewById(R.id.week_alistc_p);
        violet = (ImageView)findViewById(R.id.week_alistc_v);

        getFEDate(); //받아온 날짜에 대해 주를 구함
        setWeekP(); //주를 구해서 textView 에 적용
        setCheck("B");  //default 값으로 파란색 배경에 체크되게 설정

        //일정들을 추가한 뒤 체크박스 모양(확인)을 누르면 DB에서 해당 데이터가 NEW 인지 OLD인지 체크하는 nn 을 OLD인 O 로 변경하고 인텐트 종료
        //주를 변경시켰으면 변경시킨 내용에 대한 정보도 함께전달
        //일정들 추가한 뒤 체크박스 모양(확인) 누르면
        findViewById(R.id.weektodoa_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("cYear", year); //변경시킨 year 값을 인텐트에 저장
                intent.putExtra("cMonth", month); //변경시킨 month 값을 인텐트에 저장
                intent.putExtra("cDay", day); //변경시킨 day 값을 인텐트에 저장
                setResult(RESULT_OK, intent); //RESULT_OK 값을 인텐트에 저장
                SQLiteDatabase db = helper.getReadableDatabase(); //update 위한 SQLiteDatabase 생성
                String queryupd = String.format("update %s set nn='O' where nn = 'N';", "plaweektodo");
                //지금까지 DB에 추가했던 모든 값들은 nn 이 'N'(NEW) 이다. 추가하는 화면 내에서 추가한 모든 데이터들을 보여주기 위해서였는데
                //이제 확인 버튼을 누르면 추가하는 화면에서 벗어나게 되므로 nn 값을 'O'(OLD)로 변경해주어야 한다.
                //따라서 nn 값이 'N'인 모든 데이터의 nn값을 'O' 로 updaet 해준다.
                db.execSQL(queryupd); //update 문 실행
                db.close(); //db 닫음
                finish(); //인텐트 종료
            }
        });

        //전주로 이동하는 함수. < 버튼 누른 경우.
        findViewById(R.id.week_aleft).setOnClickListener(new View.OnClickListener() {
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
                makeList(); //변경한 날짜에 따른 주에 해당하는 리스트를 재생성
            }
        });

        //다음주로 이동하는 함수. > 버튼 누른 경우
        findViewById(R.id.week_aright).setOnClickListener(new View.OnClickListener() {
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
                makeList(); //변경한 날짜에 따른 주에 해당하는 리스트를 재생성
            }
        });

        addEdit = (EditText)findViewById(R.id.weektodoa_edit); //edittext id 값으로 가져옴
        addList = (ListView)findViewById(R.id.weektodoa_list); //리스트뷰 id 값으로 가져옴
        helper = new WeekDB(this); //WeekDB helper 생성
        wTodoAdapter = new WeekTodoAdapter(); //일정 수정 할 수 있도록 만들어 놓은 어댑터 생성
        addList.setAdapter(wTodoAdapter);//리스트뷰에 어댑터 연결
        //클릭한 리스트 내용을 수정할 수 있도록 해당 내용을 dei 에다가 넣고 addEdit 에 클릭 리스트 내용 넣음
        addList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = wTodoAdapter.getItem(position); //클릭한 리스트의 position 위치에 있는 아이템을 가져와 dei에 넣음
                addEdit.setText(dei.getContent_ed()); //dei로부터 content를 가져와 edittext 에 넣음
            }
        });

    }

    public void getFEDate() { //해당하는 날짜의 주를 가져옴(WeekActivity의 함수와 동일)
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

    public void setWeekP() {    //주의 월요일과 일요일을 보여주기 위해 textView 에 set 하고, DB에 사용하기 위해 해당 월요일을 wd 에 저장
        weekap.setText(WYear + "   " + Mmonth + "/" + MDate + " ~ " + Smonth + "/" + SDate);
        //2017   8/28 ~ 9/3   형식으로 기간을 보여주는 textview 에다가 기간을 set 해줌.
        int syear = WYear; int smonth = Mmonth; int sdate = MDate;
        wd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sdate);
        //DB에 사용하기 위해 해당 주의 월요일을 String 타입으로 변환해서 합쳐 String 타입인 wd 에 저장
    }

    public void setCheck(String color) {//인자로 받은 값만 체크표시를 하고 나머지는 체크표시를 없애는 함수
        //일단 모든 배경색에서 체크표시를 없앰
        blue.setVisibility(View.GONE);
        green.setVisibility(View.GONE);
        pink.setVisibility(View.GONE);
        violet.setVisibility(View.GONE);
        switch (color) {
            case "B": //인자로 받은 값이 "B"인 경우
                blue.setVisibility(View.VISIBLE); col = "B";break; //blue 에 있는 체크모양을 보이게 하고 col에 "B" 를 넣음
            case "G": //인자로 받은 값이 "G"인 경우
                green.setVisibility(View.VISIBLE); col = "G";break; //green 에 있는 체크모양을 보이게 하고 col에 "G" 를 넣음
            case "P": //인자로 받은 값이 "P"인 경우
                pink.setVisibility(View.VISIBLE); col = "P";break; //pink 에 있는 체크모양을 보이게 하고 col에 "P" 를 넣음
            case "V": //인자로 받은 값이 "V"인 경우
                violet.setVisibility(View.VISIBLE); col = "V";break; //violet 에 있는 체크모양을 보이게 하고 col에 "V" 를 넣음
        }
    }

    public void clickCheck(View v) {    //배경색을 선택함에 따라 체크표시를 바꾸고 해당 색에 따른 리스트를 생성하는 함수
        switch (v.getId()) {
            case R.id.week_alist_b: //파란색 배경 선택하면
                setCheck("B");break; //"B"를 인자로 주고 setCheck 함수 실행. 파란색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_alist_g: //초록색 배경 선택하면
                setCheck("G");break; //"G"를 인자로 주고 setCheck 함수 실행. 초록색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_alist_p: //분홍색 배경 선택하면
                setCheck("P");break; //"P"를 인자로 주고 setCheck 함수 실행. 분홍색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_alist_v: //보라색 배경 선택하면
                setCheck("V");break; //"V"를 인자로 주고 setCheck 함수 실행. 보라색 배경에만 체크표시 생김. col값도 변경됨.
        }
        makeList(); //선택된 col값에 따라 리스트 생성
    }

    public void makeList() {    //선택 배경 색(col 이 갖고있는 색)에 해당하는 데이터들을 가져와 리스트를 설정
        wTodoAdapter.clearItem(); //중복 막기 위해 리스트뷰 초기화
        SQLiteDatabase db = helper.getReadableDatabase(); //select 위해 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select _id, content from plaweektodo where date = '"+ wd +
                "' AND color = '" + col + "' AND nn = 'N'", null);
        //이 화면에서 추가한 내용만 리스트뷰에 보이게 하기 위해 nn이 'N'이고, 현재 선택된 배경색인 col 색이 color 이고 date 가 wd 인
        //데이터를 커서를 이용해 추출해 id, content 값 가져옴
        while(c.moveToNext()) {
            wTodoAdapter.addItem(c.getInt(0), c.getString(1)); //id, content 값을 어댑터 아이템에 넣음
        }
        c.close(); //커서 닫음
        db.close(); //db 닫음
        wTodoAdapter.notifyDataSetChanged(); //변경된 사항 어댑터에 알려줌
    }

    public void addTodo(View v) { //+, 수정 버튼을 누를때 실행. DB에 내용을 삽입, 수정 하고 리스트 다시 재설정
        SQLiteDatabase db = helper.getWritableDatabase(); //추가,수정 위해 SQLiteDatabase 생성
        String str_cont; //eidttext 에 쓴 내용을 저장하기 위한 변수
        switch(v.getId()) {
            case R.id.weektodoa_add: //내용 추가 시
                str_cont = addEdit.getText().toString(); //eidttext 내용을 str_cont 에 넣음
                if (str_cont.length() == 0) //edittext 에 내용이 없으면 그냥 종료
                    break;
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s');",
                        "plaweektodo", wd, str_cont, "F", col, "N");
                //edittext에 내용이 있으면 date에 주의 월요일인 wd, content에 str_cont 내용, 체크여부인 checked는 F,
                //color는 현재 선택되어 있는 색인 col, 새로 추가된 데이터인지 아닌지 확인하는 nn은 N으로 데이터를 insert 함
                db.execSQL(queryadd); //insert문 실행
                break;
            case R.id.weektodoa_upd: //내용 수정 시
                str_cont = addEdit.getText().toString(); //eidttext 내용을 str_cont 에 넣음
                if (str_cont.length() == 0) //edittext 에 내용이 없으면 그냥 종료
                    break;
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "plaweektodo", str_cont, dei.getEd_id());
                //리스트 클릭시 해당 아이템의 값을 넣은 dei로부터 id 값을 가져와 content를 str_cont로 수정함
                db.execSQL(queryupd); //update 문을 실행
                break;
        }
        addEdit.setText(""); //모든 작업이 끝나면 edittext 에 있는 내용을 없앰
        makeList(); //변경된 내용을 바탕으로 다시 리스트뷰 생성
        helper.close(); //helper 닫음
        db.close(); //db 닫음
    }
}
