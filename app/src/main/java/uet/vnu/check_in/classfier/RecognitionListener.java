package uet.vnu.check_in.classfier;

public interface RecognitionListener {
    void onDone(boolean isSuccess, float[] embeddings);
}
