package moe.shizuku.fcmformojo.api;

import java.util.List;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.RegistrationId;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by rikka on 2017/8/15.
 */

public interface FFMService {

    @GET("/ffm/get_registration_ids")
    Single<List<RegistrationId>> getRegistrationIds();

    @POST("/ffm/update_registration_ids")
    Single<FFMResult> updateRegistrationIds(@Body List<RegistrationId> registrationIds);
}
