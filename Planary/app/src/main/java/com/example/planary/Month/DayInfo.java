package com.example.planary.Month;


/**
 * 하루의 날짜 정보를 저장하는 클래스
 */

public class DayInfo {
    private String day;
    private boolean inMonth;
    private int todoCount;
    private boolean diary;

    //날짜를 반환
    public String getDay() { return day; }

    //날짜를 저장
    public void setDay(String day) {
        this.day = day;
    }

    //이번달의 날짜인지 정보를 반환
    public boolean isInMonth() {
        return inMonth;
    }

    //이번달의 날짜인지 정보 저장
    public void setInMonth(boolean inMonth) {
        this.inMonth = inMonth;
    }

    //체크되지 않은 todolist의 개수 반환
    public int getTodoCount() {return todoCount;}

    //체크되지 않은 todolist의 개수 저장
    public void setTodoCount(int count) { this.todoCount = count;}

    //다이어리가 있는지 없는지에 대한 정보 반환
    public boolean isDiary() {return diary;}

    //다이어리가 있는지 없는지에 대한 정보 저장
    public void setDiary(boolean dia) {this.diary = dia;}

}