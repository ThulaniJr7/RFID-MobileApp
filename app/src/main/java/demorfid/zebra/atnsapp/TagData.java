package demorfid.zebra.atnsapp;

public class TagData {

    private String TagID;
    private String TagNum;
    private String Desc;

    public TagData(String tagID, String tagNum, String desc) {
        TagID = tagID;
        TagNum = tagNum;
        Desc = desc;
    }

    public String getTagNum() {
        return TagNum;
    }

    public void setTagNum(String tagNum) {
        TagNum = tagNum;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getTagID() {
        return TagID;
    }

    public void setTagID(String tagID) {
        TagID = tagID;
    }

}
