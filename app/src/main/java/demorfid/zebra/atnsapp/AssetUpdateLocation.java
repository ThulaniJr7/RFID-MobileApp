package demorfid.zebra.atnsapp;

public class AssetUpdateLocation {

    private String rfidTagNum;
    private String rfidATNSId;
    private String rfidAssetLocation;


    public AssetUpdateLocation(String rfidTagNum, String rfidATNSId, String rfidAssetLocation) {
        this.rfidTagNum = rfidTagNum;
        this.rfidATNSId = rfidATNSId;
        this.rfidAssetLocation = rfidAssetLocation;
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

    public String getRfidAssetLocation() {
        return rfidAssetLocation;
    }

    public void setRfidAssetLocation(String rfidAssetLocation) {
        this.rfidAssetLocation = rfidAssetLocation;
    }

}

