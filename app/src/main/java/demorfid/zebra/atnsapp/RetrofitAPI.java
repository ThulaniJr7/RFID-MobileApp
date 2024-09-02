package demorfid.zebra.atnsapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitAPI {

    // as we are making a post request to post a data
    // so we are annotating it with post
    // and along with that we are passing a parameter as users
    @POST("IssueNewAsset")

    //on below line we are creating a method to post our data.
    Call<UserAsset> createPost(@Body UserAsset userAsset);

    @POST("AssetCheckIn")

        //on below line we are creating a method to post our data.
    Call<AssetCheck> createPostCheck(@Body AssetCheck assetCheck);

    @Headers("Content-Type: application/json")
    @POST("RetrieveAssetList")

        //on below line we are creating a method to post our data.
    Call<AssetList> createPostAssetList(@Body String string);
}

