package network;

import models.EventModel;
import models.UserModel;
import models.LoginResponse;
import models.UserRoleResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.lang.invoke.CallSite;
import java.util.List;

public interface ApiService {
  //events
    @GET("event/all")
    Call<List<EventModel>> getEvents();

    // USERS
    @POST("user/signup")
    Call<UserModel> signUp(@Body UserModel User);
    @POST("user/login")
    Call<LoginResponse> login(@Body UserModel user);
    @PUT("user/editUser")
    Call<UserModel> updateUserInfo (@Body UserModel user);
    @GET("user/getUserById/{userId}")
    Call<UserModel> getUserById(@Path("userId") String userId);

  @GET("user/getRole")
    Call<UserRoleResponse> getUserRole();




}
