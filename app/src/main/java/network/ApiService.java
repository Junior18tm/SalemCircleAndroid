package network;

import models.EventModel;
import models.UserModel;
import models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {
    @GET("event/all")
    Call<List<EventModel>> getEvents();

    // USERS
    @POST("user/signup")
    Call<UserModel> signUp(@Body UserModel User);
    @POST("user/login")
    Call<LoginResponse> login(@Body UserModel user);
}
