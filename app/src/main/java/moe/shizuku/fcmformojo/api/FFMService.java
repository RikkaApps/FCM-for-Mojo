package moe.shizuku.fcmformojo.api;

import java.util.List;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.NotificationToggle;
import moe.shizuku.fcmformojo.model.RegistrationId;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by rikka on 2017/8/15.
 */

public interface FFMService {

    @GET("/ffm/get_registration_ids")
    Single<List<RegistrationId>> getRegistrationIds();

    @Headers("Content-Type: application/json")
    @POST("/ffm/update_registration_ids")
    Single<FFMResult> updateRegistrationIds(@Body List<RegistrationId> registrationIds);

    @GET("/ffm/restart")
    Single<FFMResult> restart();

    @GET("/ffm/stop")
    Single<FFMResult> stop();

    @GET("/ffm/get_registration_ids")
    Single<Response<List<RegistrationId>>> getRegistrationIdsResponse();

    @GET("/ffm/get_notifications_toggle")
    Single<NotificationToggle> getNotificationsToggle();

    @Headers("Content-Type: application/json")
    @POST("/ffm/update_notifications_toggle")
    Single<FFMResult> updateNotificationsToggle(@Body NotificationToggle notificationToggle);
}
