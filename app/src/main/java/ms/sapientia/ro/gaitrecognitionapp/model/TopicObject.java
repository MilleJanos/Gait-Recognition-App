package ms.sapientia.ro.gaitrecognitionapp.model;

public class TopicObject {

    private int id;
    private String question;
    private String answer;

    public TopicObject(int id, String question, String description) {
        this.id = id;
        this.question = question;
        this.answer = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
