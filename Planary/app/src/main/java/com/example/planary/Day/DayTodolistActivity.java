package com.example.planary.Day;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.example.planary.R;

public class DayTodolistActivity extends Activity { //Day의 todolist 추가, 수정, 삭제 화면
    int year, month, day; //해당 날짜를 저장하는 year, month, day 변수
    Intent intent; //받아온 인텐트를 저장할 인텐트
    Button dayDate; //todolist 날짜를 변경하기 위한 버튼
    EditText editCont; //todolist 를 추가, 수정하기 위해 내용을 띄울 edittext
    ListView edList; //리스트의 내용을 보여주기 위한 리스트뷰
    DayDB edhelper; //DB로부터 내용을 수정, 삭제, 삽입하기 위한 DayDB helper
    DayEdTodoAD todoedAA; //원하는 리스트 형태를 가진 어댑터 생성
    String dd; //DB에 사용하기 위한 날짜를 String 타입으로 저장
    DayEdTodoItem dei; //리스트뷰에서 클릭한 부분의 내용을 저장하기 위한 아이템 dei 생성
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_add_todolist); //day_add_todolist와 레이아웃 연결

        intent = getIntent(); //인텐트를 받아옴
        year = intent.getExtras().getInt("year");
        month = intent.getExtras().getInt("month");
        day = intent.getExtras().getInt("day");
        //인텐트에 저장되어 있는 년, 월, 일을 year, month, day에 저장
        setDayDate(); //날짜 버튼에 해당 날짜를 set

        //완료 버튼(체크모양) 누르면 이전 DayActivity 화면으로 돌아감(intent finish). 현재 설정되어 있는 날짜를 intent로 보내며 intent 종료.
        findViewById(R.id.daytodoe_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("cYear", year);
                intent.putExtra("cMonth", month);
                intent.putExtra("cDay", day);
                //현재 설정되어 있는 년,월,일을 인텐트에 저장
                setResult(RESULT_OK, intent); //인텐트 종료 뒤 변경된 날짜를 적용하기 위해 이전 화면에 RESULT_OK 값을 보냄
                finish(); //인텐트 종료
            }
        });

        //todolist 내용 수정, 추가하기 위한 edittext id 값을 이용해 가져옴
        editCont = (EditText)findViewById(R.id.daytodoe_edit);
        //리스트 보여주기 위해 id 값을 이용해 가져옴
        edList = (ListView)findViewById(R.id.daytodoe_list);
        edhelper = new DayDB(this); //DayDB를 사용하기 위해 helper 생성
        todoedAA = new DayEdTodoAD(); //원하는 형태를 가진 리스트를 만들기 위해 새로 만든 어댑터를 생성
        makeList(); //해당 날짜에 맞는 리스트를 생성
        edList.setAdapter(todoedAA); //리스트와 어댑터 연결
        //해당 리스트 클릭 시 해당 리스트의 내용을 dei 에다가 저장. 리스트 내용 수정에 사용.
        edList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = todoedAA.getItem(position); //position 위치 리스트 가져와 dei 에 넣음
                editCont.setText(dei.getContent_ed()); //edittext에 dei의 content_ed를 가져와 넣음
            }
        });
    }

    public void changeDate(View v) {  //날짜를 수정하기 위해 Picker를 부르는 함수. 버튼을 누르면 실행함.
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        //Picker를 생성. 현재 year, month, day가 표시되도록 값을 전달해 주는데 month는 0부터 시작하므로 -1을 시켜줘서 전달
        dpf.show(); //Picker를 실행
    }

    //Picker를 사용해 선택한 날짜로 year, month, day를 갱신, 바꾼 날짜에 따라 리스트도 갱신
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            //Picker로부터 받아온 cyear, cmonth, cdayOfMonth를 year, month, day에 저장. month는 0부터 시작하므로 1 추가.
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate(); //날짜 버튼에 해당 날짜를 set
            makeList(); //해당 날짜에 맞는 리스트를 생성
        }
    };

    public void setDayDate() {  //버튼에 년, 월, 일을 입력하는 함수. 데이터베이스에 사용하기 위해 String 타입으로 dd도 지정.
        dayDate = (Button) findViewById(R.id.daytodoe_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일"); //버튼 id로 가져와 년, 월, 일 입력
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
        //DB에 사용하기 위해 해당 날짜를 String 타입으로 변경해 이어 붙인 내용을 String 타입의 dd 에 저장
    }

    public void makeList() {    //디비에서 날짜에 해당하는 내용을 가져와 리스트에 추가하고 어댑터에 알려주는 함수
        todoedAA.clearItem(); //내용이 중복되어 뜨지 않게 아이템들 초기화
        SQLiteDatabase db = edhelper.getReadableDatabase(); //select 위해 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select _id, content from pladaytodo where date = '"+ dd +"';", null);
        //커서 이용해 select 문 실행해 날짜에 맞는 todolist의 id 와 content 가져옴
        while(c.moveToNext()) {
            todoedAA.addItem(c.getInt(0), c.getString(1)); //select해 나온 결과값들을 어댑터 아이템에 넣음
        }
        c.close(); //커서 닫음
        db.close(); //db 닫음
        todoedAA.notifyDataSetChanged(); //변경된 내용 어댑터에 알림
    }

    public void editTodo(View v) {  //추가, 수정 버튼에 대한 DB가 작동하는 함수
        SQLiteDatabase db = edhelper.getWritableDatabase();//insert, update 하기 위해 SQLiteDatabase 생성
        String str_cont; //수정, 추가하기 위해 edittext 에 쓴 내용을 저장하기 위한 변수
        switch(v.getId()) {
            case R.id.daytodoe_add: //todolist 추가시
                str_cont = editCont.getText().toString(); //edittext에 있는 내용 str_cont에 넣음
                if (str_cont.length() == 0) //edittext의 내용이 없으면 그냥 종료
                    break;
                //edittext의 내용이 있으면 그 내용을 DB에 insert 함. 체크박스 체크 여부는 기본으로 F로 설정한다.
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s');", "pladaytodo", dd, str_cont, "F");
                //date에 dd값을, content에 str_cont 값을, checked는 기본으로 "F"값을 주고 DB에 insert함
                db.execSQL(queryadd); //insert문 실행
                break;
            case R.id.daytodoe_upd: //todolist 수정시
                str_cont = editCont.getText().toString(); //edittext에 있는 내용 str_cont 에 넣음
                if (str_cont.length() == 0) //edittext의 내용이 없으면 그냥 종료
                    break;
                //edittext의 내용이 있으면 그 내용을 DB에 update 함. 위에서 클릭한 리스트의 내용을 저장한 dei로부터 id 값을 가져와 수정.
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "pladaytodo", str_cont, dei.getEd_id());
                //dei로부터 가져온 아이디값을 가진 데이터에서 content 값을 str_cont로 update 함
                db.execSQL(queryupd); //update문 실행
                break;
        }
        editCont.setText(""); //모든 작업이 끝난 뒤 edittext 의 내용을 없앰
        makeList(); //변경된 내용에 맞게 리스트를 새로 생성
        edhelper.close(); //edhelper 닫음
        db.close(); //db 닫음
    }
}
