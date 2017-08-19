package com.example.planary.Memo;

public class MemoListItem { //리스트뷰에 내용을 보여주기 위해 필요한 데이터 저장하는 클래스
    private int mId;
    private String title;
    private String cont;
    private String date;

    public void setmId(int id) {mId = id;}
    public int getmId() {return mId;}
    public void setTitle(String t) {title = t;}
    public String getTitle() {return title;}
    public void setCont(String co) {cont = co;}
    public String getCont() {return cont;}
    public void setDate(String dd) {date = dd;}
    public String getDate() {return date;}
}
