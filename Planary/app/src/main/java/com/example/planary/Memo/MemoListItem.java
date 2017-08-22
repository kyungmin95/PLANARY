package com.example.planary.Memo;

public class MemoListItem { //리스트뷰에 내용을 보여주기 위해 필요한 데이터 저장하는 클래스
    private int mId; //id 값 저장하는 mId
    private String title; //title 값 저장하는 title
    private String cont; //content 값 저장하는 cont
    private String date; //date 값 저장하는 date

    public void setmId(int id) {mId = id;} //mId 값 저장
    public int getmId() {return mId;} //mId값 가져옴
    public void setTitle(String t) {title = t;} //title 값 저장
    public String getTitle() {return title;} //title 값 가져옴
    public void setCont(String co) {cont = co;} //cont값 저장
    public String getCont() {return cont;} //cont 값 가져옴
    public void setDate(String dd) {date = dd;} //date 값 저장
    public String getDate() {return date;} //date 값 가져옴
}
