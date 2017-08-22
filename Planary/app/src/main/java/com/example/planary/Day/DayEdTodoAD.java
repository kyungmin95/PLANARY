package com.example.planary.Day;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.planary.R;

import java.util.ArrayList;
//day 의 todolist 수정 화면에서 뜨는 리스트뷰에 대한 클래스와 어댑터 생성
class DayEdTodoItem {  //필요한 데이터 저장하는 클래스 생성. 리스트 내용 저장하는 content_ed와 해당 아이디값을 저장하는 ed_id 로 구성
    private String content_ed;
    private int ed_id;

    public void setContent_ed(String cont) {content_ed = cont;} //content_ed 내용 저장
    public String getContent_ed() {return content_ed;} //content_ed 내용 가져옴
    public void setEd_id(int id) {ed_id = id;} //ed_id 내용 저장
    public int getEd_id() {return ed_id;} //ed_id 내용 가져옴
}

public class DayEdTodoAD extends BaseAdapter { //리스트뷰 연결 위한 어댑터 생성
    private ArrayList<DayEdTodoItem> edList = new ArrayList<>(); //필요한 내용들 저장하는 ArrayList edList 생성. DayTodoItem 클래스 내용을 저장.
    DayEdTodoItem medList; //각각 position 위치에 있는 리스트 아이템 저장하는 medList

    //어댑터 생성하기 위한 기본 함수들 설정
    @Override
    public int getCount() {
        return edList.size();
    }

    @Override
    public DayEdTodoItem getItem(int position) {
        return edList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        //eidt의 list 를 inflate 하여서 convertView 구함
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.day_todoed_list, parent, false); //해당 리스트 구성 갖고 있는 day_todoed_list 레이아웃과 inflate
        }

        //해당 TextView 를 id 를 이용해 가져옴
        TextView txtv = (TextView)convertView.findViewById(R.id.daytodoe_cont);
        medList = getItem(pos); //해당 pos의 위치에 있는 아이템을 가져와 medList 에 넣음
        txtv.setText(medList.getContent_ed()); //medList에 있는 content_ed 내용을 가져와 텍스트뷰에 넣음

        //- 버튼을 누르면 삭제 여부를 물어보고 삭제하면 DB에서 삭제, list 내용도 갱신
        ImageButton ibt = (ImageButton)convertView.findViewById(R.id.daytodoe_del); //버튼을 id 써서 가져옴
        ibt.setOnClickListener(new View.OnClickListener() {   //- 버튼을 누르면 확인 뒤 해당 데이터를 DB에서 삭제하고 list 갱신.
            @Override
            public void onClick(View v) { //버튼을 누르면
                new AlertDialog.Builder(context).setTitle("삭제").setMessage("삭제 하시겠습니까?"). //삭제 여부 물어보는 AlertDialog 실행
                        setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int but) { //확인을 누르는 경우
                                medList = getItem(pos); //해당 위치의 아이템을 가져와 medList에 저장
                                DayDB helper = new DayDB(context); //DB에 내용 변경을 위해 DayDB를 가저와 helper 생성
                                SQLiteDatabase db = helper.getWritableDatabase(); //delete 위해 SQLiteDatabase 생성
                                db.delete("pladaytodo", "_id=" + medList.getEd_id(), null);
                                //medList로부터 삭제할 위치의 아이디 가져와 DB에서 해당 데이터 삭제
                                db.close(); //db 닫음
                                edList.remove(medList); //DB에서 내용이 삭제되었으므로 edList에서도 해당 리스트 삭제
                                notifyDataSetChanged();//변경된 내용 어댑터에 알림
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() { //취소를 누르는 경우
                    public void onClick(DialogInterface dialog, int but) { //작동되는 내용 없이 그냥 종료
                    }}).show(); //AlertDialog 실행
            }
        });

        return convertView;
    }

    public void addItem(int id, String cont) { //edList에 원하는 데이터 가진 리스트를 넣는 함수
        DayEdTodoItem myItem = new DayEdTodoItem();

        myItem.setContent_ed(cont); //인자로 받은 cont 값을 content_ed에 넣음
        myItem.setEd_id(id); //인자로 받은 id 값을 ed_id에 넣음

        edList.add(myItem);//원하는 데이터를 갖고 있는 리스트를 edList에 넣음
    }

    public void clearItem() {
        edList.clear();
    } //리스트 내용을 전부 삭제
}
