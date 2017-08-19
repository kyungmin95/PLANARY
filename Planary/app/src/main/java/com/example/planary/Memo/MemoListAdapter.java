package com.example.planary.Memo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.planary.R;

import java.util.ArrayList;

//리스트뷰에 보이는 내용을 설정 하기 위해 커스텀 어댑터 생성
public class MemoListAdapter extends BaseAdapter { //리스트뷰 연결 위한 어댑터 생성
    private ArrayList<MemoListItem> edList = new ArrayList<>();
    MemoListItem medList;

    @Override
    public int getCount() {
        return edList.size();
    }

    @Override
    public MemoListItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.memo_list, parent, false);
        }

        //해당 pos 위치에 있는 아이템값을 가져와 medList 에 넣은 뒤, 그 데이터를 기반으로 title, cont, day 에 제목, 내용, 날짜를 넣음
        medList = getItem(pos);
        TextView title = (TextView)convertView.findViewById(R.id.memol_title);
        title.setText(medList.getTitle());
        TextView cont = (TextView)convertView.findViewById(R.id.memol_cont);
        cont.setText(medList.getCont());
        TextView day = (TextView)convertView.findViewById(R.id.memol_date);
        day.setText(medList.getDate());

        return convertView;
    }

    public void addItem(int id, String date, String tit, String cont) { //edList에 원하는 데이터 가진 리스트를 넣는 함수
        MemoListItem myItem = new MemoListItem();

        myItem.setmId(id);
        myItem.setTitle(tit);
        myItem.setCont(cont);
        myItem.setDate(date);

        edList.add(myItem);
    }

    public void clearItem() {
        edList.clear();
    } //리스트 내용을 전부 삭제
}
