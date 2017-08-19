package com.example.planary.Memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.planary.R;

public class MemoCont extends Activity { //해당 메모의 내용을 보여주기 위한 화면
    Intent conIntent; //받아온 인텐트를 저장하는 인텐트
    TextView tit, cont;
    int mid; //받아온 인텐트로부터 넘어온 id 값을 저장하는 변수
    MemoDB mhelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_content);

        //인텐트를 받아오고 그로부터 id값을 받아와 mid 에 저장하고 필요한 값들을 설정
        conIntent = getIntent();
        tit = (TextView)findViewById(R.id.memo_title);
        cont = (TextView)findViewById(R.id.memo_cont);
        mid = conIntent.getExtras().getInt("id");
        mhelper = new MemoDB(this);

        setData(); //해당 내용을 가져와 tit 와 cont 에 넣음

        //휴지통 버튼을 누르면 현재 해당 메모를 삭제할지 물어보고, '예'를 누르면 삭제하고 인텐트 종료
        findViewById(R.id.memo_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MemoCont.this).setTitle("메모 삭제").setMessage("삭제 하시겠습니까?").
                        setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int but) { //삭제하냐는 질문에 '예'를 누른 경우
                                SQLiteDatabase db = mhelper.getWritableDatabase();
                                db.delete("plamemo", "_id=" + mid, null); //mid 값을 갖고 있는 데이터를 DB로부터 삭제
                                db.close();
                                setResult(RESULT_OK, conIntent); //변경된 값을 반영하기 위해 인텐트에 RESULT_OK 값을 전달
                                finish(); //값을 다 삭제하면 인텐트 종료
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int but) {
                    }}).show();
            }
        });
    }

    //제목을 누르거나 내용을 누르면 해당 메모를 수정하는 페이지로 이동
    public void editMemo(View v){
        Intent editIntent = new Intent(MemoCont.this, MemoAddEdit.class); //MemoAddEdit로 가는 인텐트 생성
        editIntent.putExtra("CON", 2); //edit로 가는 것이기 때문에 CON 의 값을 2로 해서 인텐트에 보냄
        editIntent.putExtra("id", mid); //수정하기 위해서는 id 값도 필요하기 때문에 받아온 id 값을 다시 인텐트에 보냄
        startActivityForResult(editIntent, 1); //인텐트 실행
    }

    @Override   //intent가 finish 된 뒤 실행. 받아온 Del 값에 따라 실행되는 내용이 다르다.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) { //받아온 Del 값이 1이면 해당 메모가 삭제되었다는 이야기이므로 바로 인텐트 종료하고 MemoActivity 로 돌아감.
            if(data.getExtras().getInt("Del") == 1) {
                setResult(RESULT_OK, conIntent);
                finish();
            }
            else setData(); //Del 값이 1이 아니면 해당 메모가 존재한다는 이야기이므로 바뀐 내용을 화면에 적용시킴.
        }
    }

    //취소(뒤로가기) 버튼을 누르는 경우 실행. 인텐트가 종료 되었을 때 MemoActivity 에서 리스트 변환을 시키기 위해 result 값을 주고 인텐트를 종료시킨다.
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, conIntent);
        finish();
        super.onBackPressed();
    }

    public void setData() { //가져온 id 값으로 DB에서 메모의 제목과 내용을 가져와 tit 와 cont 에 넣어 보여주는 함수
        SQLiteDatabase db = mhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select title, content from plamemo where _id = '"+ mid + "';", null);
        while(c.moveToNext()) {
            tit.setText(c.getString(0));
            cont.setText(c.getString(1));
        }
        c.close();
        db.close();
    }
}