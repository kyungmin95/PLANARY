package com.example.planary.Memo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.planary.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MemoAddEdit extends Activity { //메모를 추가, 수정하는 화면
    Intent maeIntent; //받아온 인텐트를 저장하는 인텐트
    int con, mid; //add 로부터 받아온 인텐트인지 edit으로부터 받아온 인텐트인지 구분하기 위한 변수 con 과 id 값을 저장하기 위한 변수 mid
    int year, month, day; //오늘 날짜를 저장하기 위한 변수
    String dd, sd; //DB에 사용하기 위한 오늘 날짜와 시간을 저장하는 변수.
    EditText title, cont;
    TextView tab;
    String t, c;
    MemoDB mhelper;
    Calendar cal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_addedit);

        //인텐트를 받아오고, add 인지 edit 인지 상태를 받아와("CON") con에 저장하고, 필요한 기본 값들을 설정
        maeIntent = getIntent();
        con = maeIntent.getExtras().getInt("CON");
        title = (EditText)findViewById(R.id.memoae_title);
        cont = (EditText)findViewById(R.id.memoae_cont);
        tab = (TextView)findViewById(R.id.memoae_tab);
        mhelper = new MemoDB(this);

        //달력을 가져와 년,월,일 을 저장하고 DB에 사용하기 위해 dd 에 String 값으로 날짜 저장
        cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH)+1;
        day = cal.get(Calendar.DAY_OF_MONTH);
        dd = Integer.toString(year) + "." + Integer.toString(month) + "." + Integer.toString(day);

        if(con == 1) { //con 이 1이면 MemoActivity 에서 추가 버튼을 누르고 들어왔다는 이야기이므로 title 과 cont 에 내용이 없음.
            title.setText("");
            cont.setText("");
            tab.setText("메모 추가");
        }
        //나머지 경우는 MemoCont 에서 수정을 하기 위해 이 화면으로 왔다는 이야기이므로,
        //MemoCont 에서 받아온 id 값을 mid에 저장하고 그 값으로 DB에서 해당 메모의 title과 content 를 가져와 title 과 cont 에 내용을 넣음.
        else {
            mid = maeIntent.getExtras().getInt("id");
            SQLiteDatabase db = mhelper.getReadableDatabase();
            Cursor c = db.rawQuery("select title, content from plamemo where _id = '"+ mid + "';", null);
            while(c.moveToNext()) {
                title.setText(c.getString(0));
                cont.setText(c.getString(1));
            }
            tab.setText("메모 수정");
            c.close();
            db.close();
        }

        //체크버튼(ok버튼)을 누르는 경우 상황에 따라 DB에 데이터를 삭제,수정,삽입하고 intent를 종료함.
        findViewById(R.id.memoae_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //메모를 최신순으로 나열하기 위해 DB에 해당 메모 저장 시간을 넣기 때문에 체크버튼(ok버튼)을 누르는 그 순간의 시간을 구해
                //DB저장에 사용하기 위해 그 시간을 String 타입으로 sd에 저장
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                Date d = new Date();
                sd = df.format(d);
                SQLiteDatabase db = mhelper.getWritableDatabase();
                //추가, 수정 과정을 거친 뒤의 제목과 내용을 가져와 t와 c에 저장.
                t = title.getText().toString();
                c = cont.getText().toString();
                if(t.length() == 0 && c.length() == 0) { //제목도, 내용도 아무것도 안써져 있다면 해당 메모를 delete 함.
                    //add와 edit 중 edit를 위해 들어온 경우에만 DB에 기존 데이터가 존재할 것이므로 con 이 2인 경우(edit인 경우)에만 DB에 데이터를 delete 함.
                    if(con == 2) {
                        db.delete("plamemo", "_id=" + mid, null); //인텐트에서 받아온 id값을 저장한 mid를 토대로 DB에서 내용 delete
                        maeIntent.putExtra("Del", 1);
                        //인텐트가 종료되고 MemoCont로 돌아갔을 때 내용이 삭제 되었으면 내용이 없으므로 바로 MemoCont에서 인텐트를 종료하기 위해 "Del"값을 1로 인텐트로 보냄
                    }
                    else ;

                }
                else { //제목이나 내용 어딘가에 글이 써있는 경우
                    if(con == 1) { //con 이 1이면 add를 하기 위해 들어온 경우이므로 DB에서 해당 내용(제목, 내용, 날짜, 시간)을 insert 함.
                        String queryadd = String.format("insert into %s values(null, '%s', '%s','%s', '%s');", "plamemo", dd, sd, t, c);
                        db.execSQL(queryadd);
                    }
                    else { //con 이 1이 아닌 경우는 edit을 위해 들어온 경우이므로 mid를 써서 DB에 해당 변경 내용(제목,내용,날짜,시간)을 update 함
                        String queryupd = String.format("update %s set title='%s', content = '%s', date = '%s', time = '%s'" +
                                "where _id = %d;", "plamemo", t, c, dd, sd, mid);
                        db.execSQL(queryupd);
                        maeIntent.putExtra("Del", 0);
                        //인텐트가 종료되고 MemoCont로 돌아갔을 때 내용이 삭제된것이 아닌것을 알리기 위해 "Del"값을 0으로 인텐트로 보냄
                    }
                }
                setResult(RESULT_OK, maeIntent); //인텐트를 종료했을 때 결과값이 MemoActivity와 MemoCont에 반영되게 하기 위해 값을 전달
                db.close();
                finish(); //모든 작업이 끝나면 인텐트 종료하고 이전 화면으로 돌아감
            }
        });
    }
}
