package com.example.planary.Week;

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
//week의 todolist 내용 추가와 수정에 대한 리스트 관리하기 위한 내용 생성
class WeekTodoItem {  //필요한 데이터 저장하는 클래스 생성. 내용과 id 값 저장.
    private String content_ed;
    private int ed_id;

    public void setContent_ed(String cont) {content_ed = cont;}
    public String getContent_ed() {return content_ed;}
    public void setEd_id(int id) {ed_id = id;}
    public int getEd_id() {return ed_id;}
}

public class WeekTodoAdapter extends BaseAdapter { //리스트뷰 연결 위한 어댑터 생성
    private ArrayList<WeekTodoItem> edList = new ArrayList<>();
    WeekTodoItem medList;

    @Override
    public int getCount() {
        return edList.size();
    }

    @Override
    public WeekTodoItem getItem(int position) {
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

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.week_todo_list, parent, false);
        }

        //해당 TextView 를 가져와서 데이터를 넣음
        TextView txtv = (TextView)convertView.findViewById(R.id.weektodoa_cont);
        medList = getItem(pos);
        txtv.setText(medList.getContent_ed());

        //- 버튼을 누르면 삭제 여부를 물어보고 삭제하면 DB에서 삭제, list 내용도 갱신
        ImageButton ibt = (ImageButton)convertView.findViewById(R.id.weektodoa_del);
        ibt.setOnClickListener(new View.OnClickListener() {   //- 버튼을 누르면 확인 뒤 해당 데이터를 DB에서 삭제하고 list 갱신.
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("삭제").setMessage("삭제 하시겠습니까?").
                        setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int but) {
                                medList = getItem(pos);
                                WeekDB helper = new WeekDB(context);
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.delete("plaweektodo", "_id=" + medList.getEd_id(), null);
                                db.close();
                                edList.remove(medList);
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int but) {
                    }}).show();
            }
        });

        return convertView;
    }

    public void addItem(int id, String cont) { //edList에 원하는 데이터 가진 리스트를 넣는 함수
        WeekTodoItem myItem = new WeekTodoItem();

        myItem.setContent_ed(cont);
        myItem.setEd_id(id);

        edList.add(myItem);
    }

    public void clearItem() {
        edList.clear();
    } //리스트 내용을 전부 삭제
}
