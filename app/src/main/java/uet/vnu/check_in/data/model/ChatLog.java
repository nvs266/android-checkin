package uet.vnu.check_in.data.model;

public class ChatLog {
    public String text;
    public String fromId;
    public Boolean isTeacher;
    public Long timestamp;
    public String name;

    public ChatLog(){

    }
    public ChatLog(String text, String fromId, Boolean isTeacher, Long timestamp, String name){
        this.text = text;
        this.fromId = fromId;
        this.isTeacher = isTeacher;
        this.timestamp = timestamp;
        this.name = name;
    }
}
