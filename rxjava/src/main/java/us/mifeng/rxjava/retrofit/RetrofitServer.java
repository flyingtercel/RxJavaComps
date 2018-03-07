package us.mifeng.rxjava.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import us.mifeng.rxjava.bean.Student;

/**
 * Created by 黑夜之火 on 2018/3/7.
 */

public interface RetrofitServer {
    @GET("/user")
    public Observable<Student>getStus(@Query("id")String id);
}
