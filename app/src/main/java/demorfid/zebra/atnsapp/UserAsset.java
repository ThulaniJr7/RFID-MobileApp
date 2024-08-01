package demorfid.zebra.atnsapp;

public class UserAsset {

    private String rfidTagId;
    private String rfidTagNum;
    private String rfidDesc;
    private String rfidResPerson;
    private String rfidATNSId;
    private String rfidFarNum;


    public UserAsset(String rfidTagId, String rfidTagNum, String rfidDesc, String rfidResPerson, String rfidATNSId, String rfidFarNum) {
        this.rfidTagId = rfidTagId;
        this.rfidTagNum = rfidTagNum;
        this.rfidDesc = rfidDesc;
        this.rfidResPerson = rfidResPerson;
        this.rfidATNSId = rfidATNSId;
        this.rfidFarNum = rfidFarNum;
    }

    public String getRfidTagId() {
        return rfidTagId;
    }

    public void setRfidTagId(String rfidTagId) {
        this.rfidTagId = rfidTagId;
    }

    public String getRfidTagNum() {
        return rfidTagNum;
    }

    public void setRfidTagNum(String rfidTagNum) {
        this.rfidTagNum = rfidTagNum;
    }

    public String getRfidDesc() {
        return rfidDesc;
    }

    public void setRfidDesc(String rfidDesc) {
        this.rfidDesc = rfidDesc;
    }

    public String getRfidResPerson() {
        return rfidResPerson;
    }

    public void setRfidResPerson(String rfidResPerson) {
        this.rfidResPerson = rfidResPerson;
    }

    public String getRfidATNSId() {
        return rfidATNSId;
    }

    public void setRfidATNSId(String rfidATNSId) {
        this.rfidATNSId = rfidATNSId;
    }

    public String getRfidFarNum() {
        return rfidFarNum;
    }

    public void setRfidFarNum(String rfidFarNum) {
        this.rfidFarNum = rfidFarNum;
    }

}

