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

public class DayTodolistActivity extends Activity {
    int year, month, day;
    Intent intent;
    Button dayDate;
    EditText editCont;
    ListView edList;
    DayDB edhelper;
    DayEdTodoAD todoedAA;
    String dd;
    DayEdTodoItem dei;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_add_todolist);

        intent = getIntent();
        year = intent.getExtras().getInt("year");
        month = intent.getExtras().getInt("month");
        day = intent.getExtras().getInt("day");
        setDayDate();

        //완료 버튼 누르면 이전 Day main 화면으로 돌아감(intent finish)
        findViewById(R.id.daytodoe_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("cYear", year);
                intent.putExtra("cMonth", month);
                intent.putExtra("cDay", day);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //리스트를 만든 어댑터와 연결
        editCont = (EditText)findViewById(R.id.daytodoe_edit);
        edList = (ListView)findViewById(R.id.daytodoe_list);
        edhelper = new DayDB(this);
        todoedAA = new DayEdTodoAD();
        makeList();
        edList.setAdapter(todoedAA);
        edList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = todoedAA.getItem(position);
                editCont.setText(dei.getContent_ed());
            }
        });
    }

    public void changeDate(View v) {  //날짜를 수정하기 위해 Picker를 부르는 함수
        DatePickerDialog dpf = new DatePickerDialog(this, listener, year, month-1, day);
        dpf.show();
    }

    //Picker를 사용해 선택한 날짜로 year, month, day를 갱신, 바꾼 날짜에 따라 리스트도 갱신
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int cyear, int cmonth, int cdayOfMonth) {
            year = cyear;
            month = cmonth+1;
            day = cdayOfMonth;
            setDayDate();
            makeList();
        }
    };

    public void setDayDate() {  //버튼에 년, 월, 일을 입력하는 함수. 데이터베이스에 사용하기 위해 String 타입으로 dd도 지정.
        dayDate = (Button) findViewById(R.id.daytodoe_date);
        dayDate.setText(year+"년 "+month+"월 "+day+"일");
        int syear = year; int smonth = month; int sday = day;
        dd = Integer.toString(syear) + Integer.toString(smonth) + Integer.toString(sday);
    }

    public void makeList() {    //디비에서 날짜에 해당하는 내용을 가져와 리스트에 추가하고 어댑터에 알려주는 함수
        todoedAA.clearItem();
        SQLiteDatabase db = edhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select _id, content from pladaytodo where date = '"+ dd +"';", null);
        while(c.moveToNext()) {
            todoedAA.addItem(c.getInt(0), c.getString(1));
        }
        c.close();
        db.close();
        todoedAA.notifyDataSetChanged();
    }

    public void editTodo(View v) {  //추가, 수정 버튼에 대한 DB가 작동하는 함수
        SQLiteDatabase db = edhelper.getWritableDatabase();
        String str_cont;
        switch(v.getId()) {
            case R.id.daytodoe_add:
                str_cont = editCont.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s');", "pladaytodo", dd, str_cont, "F");
                db.execSQL(queryadd);
                break;
            case R.id.daytodoe_upd:
                str_cont = editCont.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "pladaytodo", str_cont, dei.getEd_id());
                db.execSQL(queryupd);
                break;
        }
        editCont.setText("");
        makeList();
        edhelper.close();
        db.close();
    }
}
