package moe.shizuku.fcmformojo.api;

import java.util.List;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.model.openqq.Discuss;
import moe.shizuku.fcmformojo.model.openqq.User;
import moe.shizuku.fcmformojo.model.openqq.Group;
import moe.shizuku.fcmformojo.model.openqq.SendResult;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Rikka on 2017/4/21.
 */

public interface OpenQQService {

    /**
     * 获取好友数据
     */
    @GET("openqq/get_friend_info")
    Single<List<User>> getFriendsInfo();

    /**
     * 查询群信息
     */
    @GET("openqq/get_group_info")
    Single<List<Group>> getGroupsInfo();

    /**
     * 查询群信息（不包含群成员）
     */
    @GET("openqq/get_group_basic_info")
    Single<List<Group>> getGroupsBasicInfo();

    /**
     * 查询群信息（不包含群成员）
     */
    @GET("openqq/get_discuss_info")
    Single<List<Discuss>> getDiscussesInfo();

    /**
     * 发送好友消息
     *
     * @param id 好友的id（每次扫描登录可能会变化）
     * @param content 发送的消息
     */
    @FormUrlEncoded
    @POST("openqq/send_friend_message")
    Single<SendResult> sendFriendMessage(@Field("id") long id, @Field("content") String content);

    /**
     * 发送好友消息
     *
     * @param uid 好友的QQ号
     * @param content 发送的消息
     */
    @FormUrlEncoded
    @POST("openqq/send_friend_message")
    Single<SendResult> sendFriendMessageByUid(@Field("uid") int uid, @Field("content") String content);

    /**
     * 发送群组消息
     *
     * @param id 群组的id（每次扫描登录可能会变化）
     * @param content 发送的消息
     */
    @FormUrlEncoded
    @POST("openqq/send_group_message")
    Single<SendResult> sendGroupMessage(@Field("id") long id, @Field("content") String content);

    /**
     * 发送群组消息
     *
     * @param uid 群号码
     * @param content 发送的消息
     */
    @FormUrlEncoded
    @POST("openqq/send_group_message")
    Single<SendResult> sendGroupMessageByUid(@Field("uid") int uid, @Field("content") String content);

    /**
     * 发送讨论组消息
     *
     * @param id 讨论组的id（每次扫描登录可能会变化）
     * @param content 发送的消息
     */
    @FormUrlEncoded
    @POST("openqq/send_discuss_message")
    Single<SendResult> sendDiscussMessage(@Field("id") long id, @Field("content") String content);
}
