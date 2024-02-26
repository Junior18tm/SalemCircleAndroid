package network;

import android.content.Context;
import androidx.annotation.NonNull;
import utils.SecurityUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    private static final String BASE_URL = "https://salemcircle.onrender.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(final Context context) {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            String accessToken = SecurityUtils.getAccessToken(context);
                            Request.Builder builder = chain.request().newBuilder();
                            if (accessToken != null && !accessToken.isEmpty()) {
                                builder.addHeader("Authorization", "Bearer " + accessToken);
                            }
                            return chain.proceed(builder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
