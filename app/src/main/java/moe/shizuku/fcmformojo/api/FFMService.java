package moe.shizuku.fcmformojo.api;

import java.util.Set;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.model.Account;
import moe.shizuku.fcmformojo.model.DiscussWhitelistState;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.FFMStatus;
import moe.shizuku.fcmformojo.model.GroupWhitelistState;
import moe.shizuku.fcmformojo.model.NotificationToggle;
import moe.shizuku.fcmformojo.model.RegistrationId;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by rikka on 2017/8/15.
 */

public interface FFMService {

    @GET("ffm/get_registration_ids")
    Single<Set<RegistrationId>> getRegistrationIds();

    @Headers("Content-Type: application/json")
    @POST("ffm/update_registration_ids")
    Single<FFMResult> updateRegistrationIds(@Body Set<RegistrationId> registrationIds);

    @GET("ffm/restart")
    Single<FFMResult> restart();

    @GET("ffm/stop")
    Single<FFMResult> stop();

    @GET("ffm/get_status")
    Single<FFMStatus> getStatus();

    @GET("ffm/get_notifications_toggle")
    Single<NotificationToggle> getNotificationsToggle();

    @Headers("Content-Type: application/json")
    @POST("ffm/update_notifications_toggle")
    Single<FFMResult> updateNotificationsToggle(@Body NotificationToggle notificationToggle);

    @GET("ffm/get_group_whitelist")
    Single<GroupWhitelistState> getGroupWhitelist();

    @Headers("Content-Type: application/json")
    @POST("ffm/update_group_whitelist")
    Single<FFMResult> updateGroupWhitelist(@Body GroupWhitelistState state);

    @GET("ffm/get_discuss_whitelist")
    Single<DiscussWhitelistState> getDiscussWhitelist();

    @Headers("Content-Type: application/json")
    @POST("ffm/update_discuss_whitelist")
    Single<FFMResult> updateDiscussWhitelist(@Body DiscussWhitelistState state);

    @GET("ffm/get_account")
    Single<Account> getAccountInfo();

    @Headers("Content-Type: application/json")
    @POST("ffm/update_account")
    Single<FFMResult> updateAccountInfo(@Body Account account);
}
