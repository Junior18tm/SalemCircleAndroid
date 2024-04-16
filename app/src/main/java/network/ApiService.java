package network;

import java.util.List;

import models.CommentModel;
import models.CommentPostRequest;
import models.EventModel;
import models.FavoriteCountResponse;
import models.FavoriteModel;
import models.FavoriteRemoveRequest;
import models.FavoriteRequest;
import models.LoginResponse;
import models.ParticipationRequest;
import models.ParticipationResponse;
import models.UserModel;
import models.UserRoleResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

     //Events
    @GET("event/all")
    Call<List<EventModel>> getEvents();
    @POST("event/create")
    Call<EventModel> createEvent(@Body EventModel event);
    @GET("event/details/{eventId}")
    Call<EventModel> getEventDetails(@Path("eventId") String eventId);
    @DELETE("event/delete/{eventId}")
    Call<Void> deleteEvent(@Path("eventId") String eventId);
    @PUT("event/edit/{eventId}")
    Call<Void> updateEvent(@Path("eventId") String eventId, @Body EventModel event);
    @GET("event/search")
    Call<List<EventModel>> searchEvents(
            @Query("q") String query,
            @Query("filter") String filter,
            @Query("date") String date
    );


    // USERS
    @POST("user/signup")
    Call<UserModel> signUp(@Body UserModel User);
    @POST("user/login")
    Call<LoginResponse> login(@Body UserModel user);
    @POST("api/user/participate")
    Call<ParticipationResponse> participateInEvent(@Body ParticipationRequest request);
    @POST("api/user/leave")
    Call<ParticipationResponse> leaveEvent(@Body ParticipationRequest request);
    @PUT("user/editUser")
    Call<UserModel> updateUserInfo (@Body UserModel user);
    @GET("user/getUserById/{userId}")
    Call<UserModel> getUserById(@Path("userId") String userId);
    @GET("user/getRole")
    Call<UserRoleResponse> getUserRole();
    @Multipart
    @POST("user/profile-picture")
    Call<ResponseBody> uploadProfilePicture(@Part MultipartBody.Part file);

    // COMMENTS
    @GET("comments/event/{eventId}")
    Call<List<CommentModel>> getCommentsByEventId(@Path("eventId") String _id);
    @POST("comment/comments")
    Call<CommentModel> postComment(@Body CommentPostRequest commentPostRequest);
    @DELETE("comment/delete/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId);

    // Favorites
    @POST("favorites/add")
    Call<Response<Void>> addFavorite(@Body FavoriteRequest favoriteRequest);

    @HTTP(method = "DELETE", path = "favorites/remove", hasBody = true)
    Call<Response<Void>> removeFavorite(@Body FavoriteRemoveRequest favoriteRemoveRequest);

    @GET("favorites/user/{userId}")
    Call<List<FavoriteModel>> getFavoritesByUserId(@Path("userId") String userId);

    @GET("favorites/count/{eventId}")
    Call<FavoriteCountResponse> getFavoriteCount(@Path("eventId") String eventId);


}
