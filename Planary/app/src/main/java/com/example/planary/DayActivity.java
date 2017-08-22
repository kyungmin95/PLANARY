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

public class DayActivity extends Activity { //Day의 가장 메인 화면
    int year, month, day; //해당 내용 표시할 날짜(년,월,일)를 저장하기 위한 변수
    TextView dayDate; //해당 날짜를 화면에 보여주는 텍스트뷰
    Calendar c; //날짜를 가져오기 위해 사용할 Calendar
    String dd; //DB에 select 할 때 사용히기 위한 날짜를 String 타입으로 저장하는 변수
    DayAdapter todolistAA; //todolist 화면의 리스트뷰에 내용을 연결해 보여주기 위한 어댑터
    DayDB helper; //todolist DB 를 사용하기 위한 helper
    DayDiaryDB mHelper; //Diary DB를 사용하기 위한 mHelper
    ListView todolistLV; //todolist 의 리스트 내용을 보여주는 리스트뷰
    TextView dayDiary; //일기의 내용을 보여주는 텍스트뷰
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_main); //day_main 레이아웃과 연결
        getCurrentDate(); //오늘 날짜 가져와 year, month, day에 값 저장
        setDayDate();  //가져온 오늘 날짜를 dayDate에 입력(아무 버튼도 누르지 않은 기본 상태)

        //전날로 날짜를 옮기는 함수. < 버튼 눌렀을 때.
        findViewById(R.id.day_left).setOnClickListener(new View.OnClickListener() { // '<' 버튼 눌렀을 때. 전날로 날짜 이동
            @Override
            public void onClick(View v) {
                day -= 1; //일을 하나 줄임
                if(day == 0) { //일이 0이면 전달로 넘어가야 하는 날짜. month를 하나 줄이고, 그에 맞게 day 값도 수정
                    switch (month) { //경우에 따라 day의 값을 변경
                        case 5:case 7:case 10:case 12: //5,7,10,12월의 전달은(4,6,9,11월) 30일로 끝나기 때문에 day를 30으로 지정
                            day = 30; break;
                        case 3: //전달이 2월인 경우
                            if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) day = 29; //년이 윤년이면 day 를 29로 지정
                            else day = 28; //윤년이 아니면 day 를 28로 지정
                            break;
                        default: //그 외의 경우는 전달이 31일로 끝나므로 day를 31로 지정
                            day = 31; break;
                    }
                    month -= 1; //day 지정이 끝나면 전달로 이동했다는 의미이므로 month도 1을 줄임
                }
                if(month == 0) { //만약 1월에서 전달로 가 month가 0이 되면 년도를 작년으로 수정하고, month 를 12월로 지정.
                    year -= 1;
                    month = 12;
                }
                setDayDate(); //바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
                setDayTodolist(); //바뀐 날짜에 따라 todolist 내용 수정
                setDayDiary(); //바뀐 날짜에 따라 diary 내용 수정
            }
        });

        //다음날로 날짜를 옮기는 함수. > 버튼 눌렀을 경우
        findViewById(R.id.day_right).setOnClickListener(new View.OnClickListener() {  // '>' 버튼 눌렀을 때. 다음날로 날짜 이동
            @Override
            public void onClick(View v) {
                day += 1; //다음날로 이동하는 것이므로 day에 1을 추가
                switch (month) { //다음달로 넘어가야 하는 날짜가 되면 month를 하나 증가시키고, day 도 1일로 수정.
                    case 4:case 6:case 9:case 11: //4,6,9,11월은 마지막 날이 30일 이므로 day가 31이 되면 month를 하나 추가하고 day를 1로 수정
                        if(day == 31) {
                            day = 1; month += 1;
                        }
                        break;
                    case 2: //2월의 경우
                        if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){ //윤년이면 마지막날이 29일이므로 day가 30이되면 day를 1로 수정
                            if(day == 30) day = 1;
                        }
                        if(day == 29) day = 1; //윤년이 아니면 마지막날이 28일이므로 day가 29가 되면 day를 1로 수정
                        if(day == 1) month += 1;//day가 1로 수정이 되었다면 월이 바꼈다는 이야기이므로 month를 하나 추가
                        break;
                    default: //나머지의 경우 마지막날이 31일이므로 day가 32가 되면 day를 1로 수정하고 month도 하나 증가
                        if(day == 32) {
                            day = 1; month += 1;
                        }
                        break;
                }
                if(month > 12) { //만약 month가 12월이 넘어가면 년도를 다음해로 수정하고, month를 1월로 지정.
                    year += 1; month = 1;
                }
                setDayDate();//바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
                setDayTodolist(); //바뀐 날짜에 따라 todolist 내용 수정
                setDayDiary(); //바뀐 날짜에 따라 diary 내용 수정
            }
        });

        //오늘로 날짜를 옮기는 함수. 오늘 버튼 눌렀을 경우 실행.
        findViewById(R.id.day_today).setOnClickListener(new View.OnClickListener() {  // '오늘' 버튼 눌렀을 때. 오늘로 날짜 이동
            @Override
            public void onClick(View v) {
                getCurrentDate(); //년,월,일을 오늘 날짜로 변경
                setDayDate(); //바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
                setDayTodolist(); //바뀐 날짜에 따라 todolist 내용 수정
                setDayDiary(); //바뀐 날짜에 따라 diary 내용 수정
            }
        });

        todolistAA = new DayAdapter(); //todolist 생성 위해 어댑터 생성
        //todolist DB와 diary DB의 DB helper 생성
        helper = new DayDB(this);
        mHelper = new DayDiaryDB(this);
        todolistLV = (ListView)findViewById(R.id.daytodo_list);
        setDayTodolist(); //함수 이용해 리스트 생성
        todolistLV.setAdapter(todolistAA); //리스트를 직접 구성한 이미지로 보이게 하기 위해 새로 만든 어댑터와 연결

        //to do list의 리스트(배경)를 선택하면 to do list 추가 화면으로 넘어가는 intent 설정
        todolistLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent todolistIntent = new Intent(DayActivity.this, DayTodolistActivity.class); //DayTodolistActivity로 인텐트 넘김
                todolistIntent.putExtra("year", year);
                todolistIntent.putExtra("month", month);
                todolistIntent.putExtra("day", day);
                //년, 월, 일의 값을 인텐트에 전달
                startActivityForResult(todolistIntent, 1);
                //todolist 추가 할 때 날짜를 바꾼다면 그 바꾼 날짜를 반영하기 위해 결과값을 받는 인텐트로 인텐트 시작
            }
        });

        //일기 부분 클릭하면 수정 부분으로 넘어가는 intent 설정
        dayDiary = (TextView)findViewById(R.id.daydiary);
        setDayDiary(); //함수 이용해 해당 날에 맞는 일기 생성
        //일기에 해당하는 텍스트뷰 누르는 경우
        dayDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent diaryIntent = new Intent(DayActivity.this, DayDiaryActivity.class); //DayDiaryActivity로 인텐트 넘김
                diaryIntent.putExtra("year", year);
                diaryIntent.putExtra("month", month);
                diaryIntent.putExtra("day", day);
                //년, 월, 일의 값을 인텐트에 전달
                startActivityForResult(diaryIntent, 1);
                //일기 수정,추가 할 때 날짜를 바꾼다면 그 바꾼 날짜를 반영하기 위해 결과값을 받는 인텐트로 인텐트 시작
            }
        });
    }

    public void getCurrentDate() {  //오늘 날짜를 가져오는 함수. Calendar 사용.
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR); //Calendar로 오늘의 년도 가져옴
        month = c.get(Calendar.MONTH)+1; //Calendar로 오늘의 월 가져옴. 0부터 시작하므로 +1을 해줌
        day = c.get(Calendar.DAY_OF_MONTH); //Calendar로 오늘의 일 가져옴
    }

    public void setDayDate() {  //TextView에 년, 월, 일을 입력하는 함수. 날짜를 가져옴과 동시에 DB에 검색에 쓰기 위한 dd도 함께 설정.
        dayDate = (TextView)findViewById(R.id.day_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일"); //날짜를 가져와 날짜를 보여주는 텍스트뷰에 날짜 설정
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
        //DB 에서 select 하는데 쓰기 위해 String 타입의 변수에 년,월,일을 String 타입으로 바꿔 붙여서 저장(20170830 같이)
    }

    public void mChangeDate(View v) {  //<> 표시 말고도 날짜를 누르면 달력으로 날짜를 지정할 수 있도록 Picker 지정
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        //Picker를 생성. 현재 year, month, day가 표시되도록 값을 전달해 주는데 month는 0부터 시작하므로 -1을 시켜줘서 전달
        dpf.show(); //Picker를 실행
    }
    //Picker 에 의해 선택된 날짜로 설정된 날짜 적용
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            //Picker로부터 받아온 cyear, cmonth, cdayOfMonth를 year, month, day에 저장. month는 0부터 시작하므로 1 추가.
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate(); //바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
            setDayTodolist(); //바뀐 날짜에 따라 todolist 내용 수정
            setDayDiary();  //바뀐 날짜에 따라 diary 내용 수정
        }
    };

    //todolist 항목이 없없어서 리스트뷰가 나타나지 않을 때, + 버튼과 배경을 누르면 일정 추가 화면으로 intent 하는 함수
    public void intentTodo(View v) {
        Intent todolistIntent = new Intent(DayActivity.this, DayTodolistActivity.class); //DayTodolistActivity로 인텐트 넘김
        todolistIntent.putExtra("year", year);
        todolistIntent.putExtra("month", month);
        todolistIntent.putExtra("day", day);
        //년, 월, 일의 값을 인텐트에 전달
        startActivityForResult(todolistIntent, 1);
        //todolist 추가 할 때 날짜를 바꾼다면 그 바꾼 날짜를 반영하기 위해 결과값을 받는 인텐트로 인텐트 시작
    }

    @Override   //intent가 finish 된 뒤 실행. extra 값을 가져와 날짜를 바꾸고 그에 따라 daydate 와 todolist 내용을 바꿈.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) { //인텐트 종료된 코드가 RESULT_OK 인 경우, 인텐트로부터 받은 년,월,일 을 year, month, day에 저장
            year = data.getExtras().getInt("cYear");
            month = data.getExtras().getInt("cMonth");
            day = data.getExtras().getInt("cDay");
            setDayDate(); //바뀐 날짜에 따라 텍스트뷰에 보여지는 날짜 수정
            setDayTodolist(); //바뀐 날짜에 따라 todolist 내용 수정
            setDayDiary(); //바뀐 날짜에 따라 diary 내용 수정
        }
    }

    public void setDayTodolist() { //todolist 에 DB 내용에서 가져온 content 값을 가져옴.
        int count = 0; //select 한 결과값이 있는지 없는지 확인하기 위한 변수. 0으로 초기화.
        ImageView todoadd = (ImageView)findViewById(R.id.daytodo_add); //select 값이 없으면 보여줄 + 이미지를 id로 찾음
        todolistLV = (ListView)findViewById(R.id.daytodo_list); //select 값을 보여줄 리스트뷰를 id로 찾음
        todolistAA.clearItem(); //리스트가 중복되는일이 없도록 일단 초기화 시킴
        SQLiteDatabase db = helper.getReadableDatabase(); //select 하기 위해 helper를 사용해 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select _id, content, checked from pladaytodo where date = '"+ dd + "';", null);
        //커서를 이용해 해당 날짜(dd)에 해당하는 todolist의 _id,content,checked를 뽑아냄
        while(c.moveToNext()) {
            todolistAA.addItem(c.getInt(0), c.getString(1), c.getString(2)); //나오는 결과값을 어댑터의 아이템에 추가
            count++; //결과값이 나오면 count를 증가시킴
        }
        if(count == 0 ){ //count가 0이면 결과값이 없다는 이야기이므로 + 이미지가 보이게 만듦
            todoadd.setVisibility(View.VISIBLE);
        }
        else todoadd.setVisibility(View.GONE); //count가 0이 아니면 결과값이 있다는 이야기이므로 + 이미지를 없앰
        c.close(); //커서 닫음
        db.close(); //db 닫음
        todolistAA.notifyDataSetChanged(); //변경된 사항을 어댑터에 알려줌
    }

    public void setDayDiary() { //일기 화면에 DB에서 해당 날짜에 해당하는 dicont 값을 넣음.
        dayDiary.setText(""); //일단은 일기 화면을 초기화시킴
        SQLiteDatabase db = mHelper.getReadableDatabase(); //select 문을 사용하기 위해 SQLiteDatabase를 생성
        Cursor c = db.rawQuery("select dicont from pladaydi where date = '"+ dd + "';", null);
        //커서 이용해 해당 요일(dd)dp 해당하는 일기 내용(dicont) 가져옴
        while(c.moveToNext()) {
            dayDiary.setText(c.getString(0)); //추출해 나온 값을 일기 화면에 보여줌
        }
        c.close(); //커서 닫음
        db.close(); //db 닫음
    }
}