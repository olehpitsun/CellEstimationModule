package sample.model;

/**
 * Created by Oleh7 on 3/19/2016.
 */
public class ResearchParam {

    public static Integer research_id;
    public static String research_name;
    public static String img_name;
    public static Integer img_id;
    public static String resName;

    public static void setResearch_id(Integer res_id){
        research_id = res_id;
    }

    public static Integer getResearch_id(){
        return research_id;
    }

    public static void setResearch_name(String res_name){
        research_name =res_name;
    }
    public static String getResearch_name(){
        return research_name;
    }

    public static void setImg_name(String path){
        img_name = path;
    }

    public static String getImg_name(){
        return img_name;
    }

    public static void setImg_id(Integer id){
        img_id = id;
    }

    public static Integer getImg_id(){
        return img_id;
    }

    public static void setResName(String rn){
        resName = rn;
    }
    public static String getResName(){
        return resName;
    }
}
