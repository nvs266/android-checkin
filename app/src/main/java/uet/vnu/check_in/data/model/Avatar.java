package uet.vnu.check_in.data.model;

import java.util.ArrayList;

public class Avatar {
    public String url;
    public ArrayList<Float> vector;

    public  Avatar(String url, ArrayList<Float> vector){
        this.url = url;
        this.vector = vector;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public Avatar(){

    }
}
