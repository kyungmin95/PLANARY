package com.example.planary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.planary.Memo.MemoAddEdit;
import com.example.planary.Memo.MemoCont;
import com.example.planary.Memo.MemoDB;
import com.example.planary.Memo.MemoListAdapter;
import com.example.planary.Memo.MemoListItem;

public class MemoActivity extends Activity { //가장 메인 화면
    Intent mIntent;
    ListView memoList;
    MemoDB helper;
    MemoListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_main);

        //오른쪽 아래 + 버튼을 누르면 메모 추가 화면(MemoAddEdit)으로 이동.
        //메모 추가와 수정 화면이 같으므로 추가 화면인지 수정 화면인지 구분 위해 "CON" 값을 1로 intent에 보냄.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.memo_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(MemoActivity.this, MemoAddEdit.class);
                addIntent.putExtra("CON", 1);
                startActivityForResult(addIntent, 1);
            }
        });

        //어댑터를 새로 생성하고, 리스트뷰와 연결
        mAdapter = new MemoListAdapter();
        memoList = (ListView)findViewById(R.id.memolist);
        helper = new MemoDB(this);
        makeList(); //리스트 생성
        memoList.setAdapter(mAdapter);

        //해당 메모 리스트를 누르면 해당 메모의 내용들을 보여주는 화면(MemoCont)로 이동. 수정, 삭제 등을 위해 id값을 intent에 보냄.
        memoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MemoListItem mli = mAdapter.getItem(i); //해당 리스트의 정보들을 mli에 저장
                mIntent = new Intent(MemoActivity.this, MemoCont.class);
                mIntent.putExtra("id", mli.getmId()); //mli 로부터 id 값을 가져온 다음에 그 아이디값을 intent 에 보냄
                startActivityForResult(mIntent, 1);
            }
        });

        //해당 메모 리스트를 길게 누르면 그 메모를 삭제할지 안할지 물어보고, '예'를 누르면 DB에서 해당 메모를 삭제하고 리스트를 다시 생성.
        memoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final MemoListItem mli = mAdapter.getItem(i); //해당 리스트의 정보들을 mli에 저장. 값을 AlertDialog에 사용하기 위해 final 값으로 줌.
                new AlertDialog.Builder(MemoActivity.this).setTitle("메모 삭제").setMessage("삭제 하시겠습니까?").
                        setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int but) { //'예'를 누른 경우
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.delete("plamemo", "_id=" + mli.getmId(), null); //mli 로부터 id 값을 가져와 해당 메모를 DB로부터 delete.
                                db.close();
                                makeList(); //삭제된 내용 반영하기 위해 리스트 재생성.
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int but) {
                    }}).show();
                return true;
            }
        });
    }

    public void makeList() { //저장되어 있는 메모를 보여주기 위한 리스트를 생성하는 함수
        mAdapter.clearItem(); //중복되어 나타나는 값이 없도록 어댑터 안의 내용을 모두 지움
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select _id, date, title, content, time from plamemo order by time desc;", null);
        //가장 최근에 작성한 메모가 제일 위에 위치하게 하기 위해 order by time desc 를 사용해 select 결과를 얻어오고, 그 결과를 어댑터에 넣음
        while(c.moveToNext()) {
            mAdapter.addItem(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();
        db.close();
        mAdapter.notifyDataSetChanged(); //어댑터에 변경된 결과를 알려줌
    }

    @Override   //intent가 finish 된 뒤 실행. 변경된 내용들을 적용하기 위해 makeList() 함수를 불러줌.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            makeList();
        }
    }
}
