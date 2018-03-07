package us.mifeng.rxjava;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import us.mifeng.rxjava.bean.Course;
import us.mifeng.rxjava.bean.Student;
import us.mifeng.rxjava.retrofit.RetrofitServer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    private Subscriber<String> subscriber;
    private Observer<String> observer;
    private ImageView imageView;
    private int id = R.mipmap.ic_launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
    }
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn01:
                //创建Observer对象
                createObserver();
                break;
            case R.id.btn02:
                //创建Subscriber对象
                createSubscriber();
                break;
            case R.id.btn03:
                //创建Observable对象
                createObdervable();
                break;
            case R.id.btn04:
                //使用Action对象
                observableAndAction();
                break;
            case R.id.btn05:
                //根据id转换成Drawable
                ObservableGetImg();
                break;
            case R.id.btn06:
                //map集合变换 转换案例 一对一转换,将id转换成Drawable
                observableChange();
                break;
            case R.id.btn07:
                //根据学生信息，加载出每个学生选课的所有信息
                observableChangeMore();
                break;
            case R.id.btn08:
                //设置事件产生在哪一个线程，事件消费产生在那一个线程
                scheduler01();
                break;
            case R.id.btn09:
                //使用Scheduler控制线程加载一张图pain
                scheduler02();
                break;
            case R.id.btn10:
                //线程反复切换学习
                changeOperator();
                break;
            case R.id.btn11:
                //使用lift切换，将一个整形转化成String.
                studyLift();
                break;
            case R.id.btn12:
                rxBindRetrofit();
                break;
        }
    }

    private void rxBindRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("url")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        RetrofitServer rs = retrofit.create(RetrofitServer.class);
        Observable<Student> stus = rs.getStus("");
        stus.observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Student>() {
                    @Override
                    public void call(Student student) {

                    }
                });
    }

    private void studyLift() {
        Observable.just(11)

                .lift(new Observable.Operator<String, Integer>() {
                    @Override
                    public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {

                        return new Subscriber<Integer>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Integer integer) {
                                subscriber.onNext(""+integer);
                            }
                        };
                    }
                }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, "call: ==sssss==="+s);
            }
        });
    }

    private void changeOperator() {
        //线程的反复切换
        Observable.just(32)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return ""+integer;
                    }
                }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.newThread())
                .just("")
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                    }
                });
    }

    private void scheduler02() {
        Observable.just(id)
                .map(new Func1<Integer, Drawable>() {
                    @Override
                    public Drawable call(Integer integer) {
                        Drawable drawable = ContextCompat.getDrawable(MainActivity.this,integer);
                        return drawable;
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Drawable>() {
                    @Override
                    public void call(Drawable drawable) {
                        imageView.setImageDrawable(drawable);
                    }
                });
    }

    private void scheduler01() {
        Observable.just(1,2,3,4)
                .subscribeOn(Schedulers.io())//订阅者发生在哪一个线程，或者指明了事件产生的线程
                .observeOn(AndroidSchedulers.mainThread())//观察者发生在那个线程，或者指明了事件消费的线程
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                    }
                });
    }


    private void observableChangeMore() {
        Student[]stus = new Student[5];
        for(int i=0;i<stus.length;i++){
            String name = "张三"+i;
            Course[]cus = new Course[5];
            for(int j=0;j<cus.length;j++){
                Course cu = new Course("语文"+i);
                cus[j] = cu;
            }
            stus[i] = new Student(name,cus);
        }
        Observable.from(stus)
                .flatMap(new Func1<Student, Observable<Course>>() {
                    @Override
                    public Observable<Course> call(Student student) {

                        return Observable.from(student.getCourse());
                    }
                }).subscribe(new Action1<Course>() {
            @Override
            public void call(Course course) {
                Log.i(TAG, "call: ======="+course.getName());
            }
        });

    }

    private void observableChange() {
        Observable.just(R.mipmap.ic_launcher)
                .map(new Func1<Integer, Drawable>() {
                    @Override
                    public Drawable call(Integer integer) {
                        Drawable drawable = ContextCompat.getDrawable(MainActivity.this,integer);
                        return drawable;
                    }
                }).subscribe(new Action1<Drawable>() {
            @Override
            public void call(Drawable drawable) {

            }
        });
       /* Observable.just("")
                .map(new Func1<String, Bitmap>() {
                    @Override
                    public Bitmap call(String s) {
                        return null;
                    }
                }).subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {

            }
        });*/
    }

    private void ObservableGetImg() {
        /*Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.mipmap.ic_launcher);
                subscriber.onNext(drawable);
            }
        }).subscribe(new Action1<Drawable>() {
            @Override
            public void call(Drawable drawable) {
                imageView.setImageDrawable(drawable);
            }
        });*/
        Observable<Drawable>observable = Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.mipmap.ic_launcher);
                subscriber.onNext(drawable);
                subscriber.onCompleted();
            }
        });
        observable.subscribe(new Action1<Drawable>() {
            @Override
            public void call(Drawable drawable) {
                imageView.setImageDrawable(drawable);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.i(TAG, "call: ====>"+throwable.getMessage());
            }
        });
    }

    private void observableAndAction() {
        Observable<String>observable = Observable.just("哈哈哈","嘿嘿嘿","呵呵呵呵呵");
        Action0 onComplete = new Action0() {
            @Override
            public void call() {
                //因为Call方法无返回值也无参数，所以和onCompleted方法类似。所以可以包装成OnComplete方法。
                Log.i(TAG, "call: ==s0==");
            }
        };
        Action1<String>onNext = new Action1<String>() {
            @Override
            public void call(String s) {
                //因为call方法无返回值，但有一个参数，和onError和onNext方法类似，所以可以包装成这两个方法中一个            }
                Log.i(TAG, "call: ====>"+s);
            }
        };
        Action1<Throwable>onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.i(TAG, "call: =throwable===");
            }
        };

        observable.subscribe(onNext,onError,onComplete);
    }

    private void createObdervable() {
        Observable<String>observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("哈哈哈");
                subscriber.onNext("嘿嘿嘿");
                subscriber.onNext("呵呵呵");
                subscriber.onCompleted();
            }
        });
        //事件处理
        observable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: =====>"+"完成");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: =====>"+s);
            }
        });
    }

    private void createSubscriber() {
        subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onNext(String s) {
            };
        };
    }

    private void createObserver() {
        //完成时调用
//错误时调用
//事件发出时调用
        observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                //完成时调用
            }
            @Override
            public void onError(Throwable e) {
                //错误时调用
            }
            @Override
            public void onNext(String s) {
                //事件发出时调用
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriber.unsubscribe();
    }


}
