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
    private ArrayList<MemoListItem> edList = new ArrayList<>(); //내용 저장하기 위한 ArrayList edList를 생성. MemoListItem 클래스 내용을 저장.
    MemoListItem medList; //하나의 아이템을 저장하기 위한 medList

    //어댑터 생성하기 위한 기본 함수들 설정
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
            convertView = inflater.inflate(R.layout.memo_list, parent, false); //해당 리스트 구성 갖고 있는 memo_list layout 과 inflate
        }

        //해당 pos 위치에 있는 아이템값을 가져와 medList 에 넣은 뒤, 그 데이터를 기반으로 title, cont, day 에 제목, 내용, 날짜를 넣음
        medList = getItem(pos); //해당 pos 위치에 있는 아이템값을 가져와 medList 에 넣음
        TextView title = (TextView)convertView.findViewById(R.id.memol_title);
        title.setText(medList.getTitle()); //medList에 넣은 title 값을 제목 textview에 넣음
        TextView cont = (TextView)convertView.findViewById(R.id.memol_cont);
        cont.setText(medList.getCont()); //medList에 넣은 cont 값을 내용 textview에 넣음
        TextView day = (TextView)convertView.findViewById(R.id.memol_date);
        day.setText(medList.getDate()); //medList에 넣은 date 값을 날짜 textview에 넣음

        return convertView;
    }

    public void addItem(int id, String date, String tit, String cont) { //edList에 원하는 데이터 가진 리스트를 넣는 함수
        MemoListItem myItem = new MemoListItem();

        myItem.setmId(id); //인자로 받은 id값을 mId에 넣음
        myItem.setTitle(tit); //인자로 받은 tit 값을 title에 넣음
        myItem.setCont(cont); //인자로 받은 cont 값을 cont에 넣음
        myItem.setDate(date); //인자로 받은 date 값을 date에 넣음

        edList.add(myItem); //원하는 데이터가 들어간 아이템을 edList에 넣음
    }

    public void clearItem() {
        edList.clear();
    } //리스트 내용을 전부 삭제
}
