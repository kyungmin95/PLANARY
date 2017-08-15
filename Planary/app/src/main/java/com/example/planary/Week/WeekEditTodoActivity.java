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

public class WeekEditTodoActivity extends Activity {
    int MDate, SDate, Mmonth, Smonth, WYear; //해당 주의 월,일요일에 대한 정보를 저장하고, 년을 저장하는 변수
    String wd; //DB에 쓰이기 위한 날짜를 저장하는 변수
    String col; //체크 색상을 저장하기 위한 변수
    TextView weekap;
    Intent intent;
    ImageView blue, green, pink, violet; //체크 모양 보이는 여부를 결정하기 위해 데이터를 저장하는 변수
    WeekDB helper;
    WeekTodoAdapter wTodoAdapter;
    WeekTodoItem dei;
    EditText eEdit;
    ListView editList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_edit_todo);
        //인텐트로 날짜와 색에 대한 데이터를 받아오고, id를 가져와 변수에 적용시킴
        intent = getIntent();
        WYear = intent.getExtras().getInt("WYear");
        Mmonth = intent.getExtras().getInt("Mmonth");
        MDate = intent.getExtras().getInt("Mday");
        Smonth = intent.getExtras().getInt("Smonth");
        SDate = intent.getExtras().getInt("Sday");
        col = intent.getExtras().getString("color");
        weekap = (TextView)findViewById(R.id.week_edate);
        blue = (ImageView)findViewById(R.id.week_elistc_b);
        green = (ImageView)findViewById(R.id.week_elistc_g);
        pink = (ImageView)findViewById(R.id.week_elistc_p);
        violet = (ImageView)findViewById(R.id.week_elistc_v);

        setWeekP();
        setCheck(col);  //메인 화면에서 클릭한 배경 색을 default 로 설정

        //일정들을 추가한 뒤 체크박스 모양(확인)을 누르면 인텐트 종료. 주 변경이 없으니 add 와는 다르게 값 전달 없이 종료한다.
        findViewById(R.id.weektodoe_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(2, intent);
                finish();
            }
        });

        //일정을 수정할 수 있도록 기본 내용들 설정
        eEdit = (EditText)findViewById(R.id.weektodoe_edit);
        editList = (ListView)findViewById(R.id.weektodoe_list);
        helper = new WeekDB(this);
        wTodoAdapter = new WeekTodoAdapter();
        editList.setAdapter(wTodoAdapter);
        makeList();
        //클릭한 리스트 내용을 수정할 수 있도록 해당 내용을 dei 에다가 넣고 eEdit 에 클릭 리스트 내용 넣음
        editList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = wTodoAdapter.getItem(position);
                eEdit.setText(dei.getContent_ed());
            }
        });
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
            case R.id.week_elist_b:
                setCheck("B");break;
            case R.id.week_elist_g:
                setCheck("G");break;
            case R.id.week_elist_p:
                setCheck("P");break;
            case R.id.week_elist_v:
                setCheck("V");break;
        }
        makeList();
    }

    public void makeList() {    //선택 배경 색에 해당하는 데이터들을 가져와 리스트를 설정
        wTodoAdapter.clearItem();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select _id, content from plaweektodo where date = '"+ wd +
                "' AND color = '" + col + "'", null);
        while(c.moveToNext()) {
            wTodoAdapter.addItem(c.getInt(0), c.getString(1));
        }
        c.close();
        db.close();
        wTodoAdapter.notifyDataSetChanged();
    }

    public void editTodo(View v) { //+, 수정 버튼을 누를때 실행. DB에 내용을 삽입, 수정 하고 리스트 다시 재설정
        SQLiteDatabase db = helper.getWritableDatabase();
        String str_cont;
        switch(v.getId()) {
            case R.id.weektodoe_add: //내용 추가 시
                str_cont = eEdit.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s');",
                        "plaweektodo", wd, str_cont, "F", col, "O");
                db.execSQL(queryadd);
                break;
            case R.id.weektodoe_upd: //내용 수정 시
                str_cont = eEdit.getText().toString();
                if (str_cont.length() == 0)
                    break;
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "plaweektodo", str_cont, dei.getEd_id());
                db.execSQL(queryupd);
                break;
        }
        eEdit.setText("");
        makeList();
        helper.close();
        db.close();
    }
}
