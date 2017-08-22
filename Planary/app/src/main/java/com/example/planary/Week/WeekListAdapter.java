package com.example.planary.Week;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.planary.R;

import java.util.ArrayList;
//week 메인 화면에 보이는 리스트를 관리하기 위한 정보들 관리
class WeekListItem {//필요한 데이터를 저장하는 클래스. id, 내용, 체크박스 체크 여부 관한 데이터를 저장한다.
    private int dId;
    private String content;
    private boolean chcond;

    public void setDId(int dd) {dId = dd;} //dId 저장
    public int getDId() {return dId;} //dId 가져옴
    public void setContent(String con) {
        content = con;
    } //content 저장
    public String getContent() { return content; } //content 가져옴
    public void setChcond(boolean cc) { chcond = cc;} //chcond 저장
    public boolean getChcond() { return chcond;} //chcond 가져옴
}

public class WeekListAdapter extends BaseAdapter { //리스트와 연결 위한 어댑터 생성
    private ArrayList<WeekListItem> myList = new ArrayList<>(); //내용 저장하기 위한 ArrayList myList를 생성. WeekListView 클래스 내용을 저장.

    //어댑터 생성하기 위한 기본 함수들 설정
    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public WeekListItem getItem(int position) {
        return myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Context context = parent.getContext();
        final TextView txtv; final int pos = position;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.week_list, parent, false); //해당 리스트 구성 갖고 있는 week_list layout 과 inflate
        }

        //해당 TextView 를 id 값을 이용해 가져옴
        txtv = (TextView)convertView.findViewById(R.id.weektodo_cont);

        WeekListItem mList = getItem(position); //position 위치의 리스트를 가져옴
        txtv.setText(mList.getContent()); //그 리스트로부터 해당 content 내용을 텍스트뷰에 넣음

        //해당 체크박스를 id 값을 이용해 가져옴
        CheckBox chb = (CheckBox)convertView.findViewById(R.id.weektodo_chb);

        //DB에서 checked가 T이면(chcond는 true) 체크박스를 체크하고 내용에 중앙선 삽입
        if(getItem(position).getChcond() == true) {
            chb.setChecked(true); //체크박스 체크
            txtv.setPaintFlags(txtv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //내용 중앙선 삽입
        }
        else {
            chb.setChecked(false); //체크박스 해제
        }

        //체크박스 체크 상태에 따라 DB에 checked 내용을 update 함
        //체크박스 상태 변경에 따라 실행되는 함수
        chb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true) { //체크박스가 체크되면 DB에 checked를 T로 수정하고 텍스트에 중앙선을 삽입. myList의 체크여부 내용 수정.
                    WeekListItem mList = getItem(pos); //myList에 내용 변경하기 위해 해당 position의 아이템 가져와 mList에 넣음
                    mList.setChcond(true); //mList의 chcond를 true로 설정
                    myList.set(pos, mList); //myList의 pos(positon)위치에 있는 내용을 바뀐 내용을 갖고 있는 mList로 수정
                    WeekDB helper = new WeekDB(context); //DB에 바뀐내용 적용시키기 위해 WeekDB를 가져옴
                    SQLiteDatabase db = helper.getWritableDatabase(); //update문을 사용하기 위해 SQListeDatabase 생성
                    String queryupd = String.format("update %s set checked='%s' where _id = %d;", "plaweektodo", "T", mList.getDId());
                    //변경된 내용이 저정되어있는 mList로부터 id 값을 가져와 그 값을 이용해 checked 값을 T로 수정
                    db.execSQL(queryupd); //update문 실행
                    db.close(); //db 닫음
                    txtv.setPaintFlags(txtv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //텍스트 내용에 중앙선 삽입
                }
                else { //체크박스 체크가 해제되면 DB에 checked를 F로 수정하고 텍스트 중앙선 없앰. myList의 체크여부 내용 수정.
                    WeekListItem mList = getItem(pos); //myList에 내용 변경하기 위해 해당 position의 아이템 가져와 mList에 넣음
                    mList.setChcond(false);//mList의 chcond를 false로 설정
                    myList.set(pos, mList); //myList의 pos(positon)위치에 있는 내용을 바뀐 내용을 갖고 있는 mList로 수정
                    WeekDB helper = new WeekDB(context); //DB에 바뀐내용 적용시키기 위해 WeekDB를 가져옴
                    SQLiteDatabase db = helper.getWritableDatabase(); //update문을 사용하기 위해 SQListeDatabase 생성
                    String queryupd = String.format("update %s set checked='%s' where _id = %d;", "plaweektodo", "F", mList.getDId());
                    //변경된 내용이 저정되어있는 mList로부터 id 값을 가져와 그 값을 이용해 checked 값을 F로 수정
                    db.execSQL(queryupd); //update문 실행
                    db.close(); //db 닫음
                    txtv.setPaintFlags(txtv.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG); //텍스트 내용에 중앙선 해제
                }
                notifyDataSetChanged(); //변경사항을 어댑터에 알림
            }
        });

        return convertView;
    }

    public void addItem(int dd, String cont, String cc) { //myList에 원하는 데이터 가진 리스트를 넣는 함수
        WeekListItem myItem = new WeekListItem();
        boolean cd; //받은 String 값에 따라 true, false 를 저장하기 위한 boolean 변수
        myItem.setContent(cont); //인자값으로 받은 cont 값을 content에 넣음
        myItem.setDId(dd); //인자값으로 받은 dd 값을 did에 넣음
        if(cc.equals("T")) cd = true; //인자값으로 받은 cc 값이 T 이면 cd 를 true로
        else cd = false; //인자값으로 받은 cc 값이 F이면 cd를 false로 설정
        myItem.setChcond(cd); //그 cd 값을 chcond에 넣음
        myList.add(myItem); //원하는 값들이 저장된 데이터를 myList 에 넣음
    }

    public void clearItem() {
        myList.clear();
    } //myList 를 초기화 하는 함수
}