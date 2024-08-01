package demorfid.zebra.atnsapp;

public class AssetCheck {

    private String rfidTagNum;
    private String rfidATNSId;
    private String rfidAssetCheck;


    public AssetCheck(String rfidTagNum, String rfidATNSId, String rfidAssetCheck) {
        this.rfidTagNum = rfidTagNum;
        this.rfidATNSId = rfidATNSId;
        this.rfidAssetCheck = rfidAssetCheck;
    }

    public String getRfidTagNum() {
        return rfidTagNum;
    }

    public void setRfidTagNum(String rfidTagNum) {
        this.rfidTagNum = rfidTagNum;
    }

    public String getRfidATNSId() {
        return rfidATNSId;
    }

    public void setRfidATNSId(String rfidATNSId) {
        this.rfidATNSId = rfidATNSId;
    }

    public String getRfidAssetCheck() {
        return rfidAssetCheck;
    }

    public void setRfidAssetCheck(String rfidAssetCheck) {
        this.rfidAssetCheck = rfidAssetCheck;
    }

}

