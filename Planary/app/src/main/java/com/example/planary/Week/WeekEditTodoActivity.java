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
    TextView weekap; //주 기간을 보여주기 위한 textview
    Intent intent; //인텐트를 전달받아 저장하기 위한 인텐트
    ImageView blue, green, pink, violet; //체크 모양 보이는 여부를 결정하기 위해 데이터를 저장하는 변수
    WeekDB helper; //WeekDB 사용하기 위한 helper
    WeekTodoAdapter wTodoAdapter; //리스트뷰에 원하는 모습으로 내용이 보이게 하기 위한 어댑터
    WeekTodoItem dei; //클릭한 리스트의 내용을 저장하는 아이템
    EditText eEdit; //todolist 내용 추가하고 수정하기 위한 edittext
    ListView editList; //todolist 를 보여주기 위한 리스트뷰
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_edit_todo); //week_edit_todo 레이아웃과 연결

        intent = getIntent(); //인텐트 받아옴
        WYear = intent.getExtras().getInt("WYear"); //년을 받아와 WYear에 저장
        Mmonth = intent.getExtras().getInt("Mmonth"); //주의 월요일 월을 가져와 Mmonth에 저장
        MDate = intent.getExtras().getInt("Mday"); //주의 월요일 일을 가져와 MDate 에 저장
        Smonth = intent.getExtras().getInt("Smonth"); //주의 일요일 월을 가져와 Smonth에 저장
        SDate = intent.getExtras().getInt("Sday"); //주의 일요일 일을 가져와 SDate에 저장
        col = intent.getExtras().getString("color"); //이 화면으로 들어오기 위해 클릭한 리스트이 배경 색을 가져와 col에 저장. 이 색이 defaul가 됨.
        //id를 가져와 변수에 적용시킴
        weekap = (TextView)findViewById(R.id.week_edate);
        blue = (ImageView)findViewById(R.id.week_elistc_b);
        green = (ImageView)findViewById(R.id.week_elistc_g);
        pink = (ImageView)findViewById(R.id.week_elistc_p);
        violet = (ImageView)findViewById(R.id.week_elistc_v);

        setWeekP(); //주를 구해서 textView 에 적용
        setCheck(col);  //메인 화면에서 클릭한 배경 색을 default 로 설정

        //일정들을 추가한 뒤 체크박스 모양(확인)을 누르면 인텐트 종료. 주 변경이 없으니 add 와는 다르게 값 전달 없이 종료한다.
        findViewById(R.id.weektodoe_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(2, intent); //RESULT_OK값 말고 다른 값을 전달해 WeekActivity로 돌아갔을 때 변경 사항이 없음을 알림
                finish(); //인텐트 종료
            }
        });

        eEdit = (EditText)findViewById(R.id.weektodoe_edit); //edittext id 값으로 가져옴
        editList = (ListView)findViewById(R.id.weektodoe_list); //리스트뷰 id 값으로 가져옴
        helper = new WeekDB(this); //WeekDB helper 생성
        wTodoAdapter = new WeekTodoAdapter(); //일정 수정 할 수 있도록 만들어 놓은 어댑터 생성
        editList.setAdapter(wTodoAdapter); //리스트뷰에 어댑터 연결
        makeList(); //해당 col 값을 가진 리스트를 생성
        //클릭한 리스트 내용을 수정할 수 있도록 해당 내용을 dei 에다가 넣고 eEdit 에 클릭 리스트 내용 넣음
        editList.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트 클릭 시 해당 데이터의 DB 내용을 가져와 dei에 넣음
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dei = wTodoAdapter.getItem(position); //클릭한 리스트의 position 위치에 있는 아이템을 가져와 dei에 넣음
                eEdit.setText(dei.getContent_ed()); //dei로부터 content를 가져와 edittext 에 넣음
            }
        });
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
            case R.id.week_elist_b: //파란색 배경 선택하면
                setCheck("B");break; //"B"를 인자로 주고 setCheck 함수 실행. 파란색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_elist_g: //초록색 배경 선택하면
                setCheck("G");break; //"G"를 인자로 주고 setCheck 함수 실행. 초록색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_elist_p: //분홍색 배경 선택하면
                setCheck("P");break; //"P"를 인자로 주고 setCheck 함수 실행. 분홍색 배경에만 체크표시 생김. col값도 변경됨.
            case R.id.week_elist_v: //보라색 배경 선택하면
                setCheck("V");break; //"V"를 인자로 주고 setCheck 함수 실행. 보라색 배경에만 체크표시 생김. col값도 변경됨.
        }
        makeList(); //선택된 col값에 따라 리스트 생성
    }

    public void makeList() {    //선택 배경 색에 해당하는 데이터들을 가져와 리스트를 설정
        wTodoAdapter.clearItem(); //중복 막기 위해 리스트뷰 초기화
        SQLiteDatabase db = helper.getReadableDatabase(); //select 위해 SQLiteDatabase 생성
        Cursor c = db.rawQuery("select _id, content from plaweektodo where date = '"+ wd +
                "' AND color = '" + col + "'", null);
        //현재 선택된 배경색인 col 색이 color 이고 date 가 wd 인 데이터를 커서를 이용해 추출해 id, content 값 가져옴
        while(c.moveToNext()) {
            wTodoAdapter.addItem(c.getInt(0), c.getString(1)); //id, content 값을 어댑터 아이템에 넣음
        }
        c.close(); //커서 닫음
        db.close(); //db 닫음
        wTodoAdapter.notifyDataSetChanged(); //변경된 사항을 어댑터에 알림
    }

    public void editTodo(View v) { //+, 수정 버튼을 누를때 실행. DB에 내용을 삽입, 수정 하고 리스트 다시 재설정
        SQLiteDatabase db = helper.getWritableDatabase(); //추가,수정 위해 SQLiteDatabase 생성
        String str_cont; //eidttext 에 쓴 내용을 저장하기 위한 변수
        switch(v.getId()) {
            case R.id.weektodoe_add: //내용 추가 시
                str_cont = eEdit.getText().toString(); //eidttext 내용을 str_cont 에 넣음
                if (str_cont.length() == 0) //edittext 에 내용이 없으면 그냥 종료
                    break;
                String queryadd = String.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s');",
                        "plaweektodo", wd, str_cont, "F", col, "O");
                //edittext에 내용이 있으면 date에 주의 월요일인 wd, content에 str_cont 내용, 체크여부인 checked는 F,
                // color는 현재 선택되어 있는 색인 col,
                //새로 추가된 데이터인지 아닌지 확인하는 nn은 현재 페이지가 add 페이지가 아니므로 O로 데이터를 insert 함
                db.execSQL(queryadd); //insert문 실행
                break;
            case R.id.weektodoe_upd: //내용 수정 시
                str_cont = eEdit.getText().toString(); //eidttext 내용을 str_cont 에 넣음
                if (str_cont.length() == 0) //edittext 에 내용이 없으면 그냥 종료
                    break;
                String queryupd = String.format("update %s set content='%s' where _id = %d;", "plaweektodo", str_cont, dei.getEd_id());
                //리스트 클릭시 해당 아이템의 값을 넣은 dei로부터 id 값을 가져와 content를 str_cont로 수정함
                db.execSQL(queryupd); //update 문을 실행
                break;
        }
        eEdit.setText(""); //모든 작업이 끝나면 edittext 에 있는 내용을 없앰
        makeList(); //변경된 내용을 바탕으로 다시 리스트뷰 생성
        helper.close(); //helper 닫음
        db.close(); //db 닫음
    }
}
