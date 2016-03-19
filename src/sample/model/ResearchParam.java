package sample.model;

/**
 * Created by Oleh7 on 3/19/2016.
 */
public class ResearchParam {

    public static Integer research_id;
    public static String img_name;

    public static void setResearch_id(Integer res_id){
        research_id = res_id;
    }

    public static Integer getResearch_id(){

        return research_id;
    }

    public static void setImg_name(String path){
        img_name = path;
    }

    public static String getImg_name(){
        return img_name;
    }
}
