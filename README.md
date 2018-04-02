# RxJavaComps
RxJava学习
引入依赖：
```
compile 'io.reactivex:rxjava:1.0.14'
compile 'io.reactivex:rxandroid:1.0.1'
```
RxJava定义
```
一个在 Java VM 上使用可观测的序列来组成异步的、基于事件的程序的库
```
图 ![图1](https://github.com/flyingtercel/RxJavaComps/blob/master/rxjava/src/main/res/drawable/t0.png)
```
如图所示，通过 setOnClickListener() 方法，Button 持有 OnClickListener 的引用（这一过程没有在图上画出）；
当用户点击时，Button 自动调用 OnClickListener 的 onClick() 方法。
另外，如果把这张图中的概念抽象出来（Button -> 被观察者、OnClickListener -> 观察者、setOnClickListener() -> 订阅，onClick() -> 事件），
就由专用的观察者模式（例如只用于监听控件点击）转变成了通用的观察者模式。如下图：
```
![图2](https://github.com/flyingtercel/RxJavaComps/blob/master/rxjava/src/main/res/drawable/t1.png)
```
RxJava 有四个基本概念：Observable (可观察者，即被观察者)、 Observer (观察者)、 subscribe (订阅)、事件。
Observable 和 Observer 通过 subscribe() 方法实现订阅关系，从而 Observable 可以在需要的时候发出事件来通知 Observer。
与传统观察者模式不同， RxJava 的事件回调方法除了普通事件 onNext() （相当于 onClick() / onEvent()）之外，
还定义了两个特殊的事件：onCompleted() 和 onError()。
onCompleted(): 事件队列完结。
RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志。
onError(): 事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
在一个正确运行的事件序列中, onCompleted() 和 onError() 有且只有一个，并且是事件序列中的最后一个。
需要注意的是，onCompleted() 和 onError() 二者也是互斥的，即在队列中调用了其中一个，就不应该再调用另一个。
```
RxJava 的观察者模式大致如下图：
![图3](https://github.com/flyingtercel/RxJavaComps/blob/master/rxjava/src/main/res/drawable/t2.png)

RxJava基本实现
基于以上的概念， RxJava 的基本实现主要有三点：
1)创建 Observer
```
  Observer 即观察者，它决定事件触发的时候将有怎样的行为。 RxJava 中的 Observer 接口的实现方式：
Observer<String>observer = new Observer<String>() {
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
除了 Observer 接口之外，RxJava 还内置了一个实现了 Observer 的抽象类：Subscriber。 Subscriber 对 Observer 接口进行了一些扩展，
但他们的基本使用方式是完全一样的：
Subscriber<String>subscriber = new Subscriber<String>() {
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
```
2) 创建 Observable
```
Observable 即被观察者，它决定什么时候触发事件以及触发怎样的事件。 RxJava 使用 create() 方法来创建一个 Observable ，并为它定义事件触发规则：
 Observable<String>observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("哈哈哈");
                subscriber.onNext("嘿嘿嘿");
                subscriber.onNext("呵呵呵");
                subscriber.onCompleted();
            }
        });
just(T...): 将传入的参数依次发送出来。
Observable observable = Observable.just("Hello", "Hi", "Aloha");

from(T[]) / from(Iterable<? extends T>) : 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来。
String[] words = {"Hello", "Hi", "Aloha"};
Observable observable = Observable.from(words);
上面 just(T...) 的例子和 from(T[]) 的例子，都和之前的 create(OnSubscribe) 的例子是等价的。
```
3) Subscribe (订阅)
```
创建了 Observable 和 Observer 之后，再用 subscribe() 方法将它们联结起来，整条链子就可以工作了。代码形式很简单：
observable.subscribe(observer);
// 或者：
observable.subscribe(subscriber);

除了 subscribe(Observer) 和 subscribe(Subscriber) ，subscribe() 还支持不完整定义的回调，RxJava 会自动根据定义创建出 Subscriber 。形式如下：
Action1<String> onNextAction = new Action1<String>() {
    // onNext()
    @Override
    public void call(String s) {
        Log.d(tag, s);
    }
};
Action1<Throwable> onErrorAction = new Action1<Throwable>() {
    // onError()
    @Override
    public void call(Throwable throwable) {
        // Error handling
    }
};
Action0 onCompletedAction = new Action0() {
    // onCompleted()
    @Override
    public void call() {
        Log.d(tag, "completed");
    }
};
// 自动创建 Subscriber ，并使用 onNextAction 来定义 onNext()
observable.subscribe(onNextAction);
// 自动创建 Subscriber ，并使用 onNextAction 和 onErrorAction 来定义 onNext() 和 onError()
observable.subscribe(onNextAction, onErrorAction);
// 自动创建 Subscriber ，并使用 onNextAction、 onErrorAction 和 onCompletedAction 来定义 onNext()、 onError() 和 onCompleted()
observable.subscribe(onNextAction, onErrorAction, onCompletedAction);
```
3. 线程控制 —— Scheduler (一)
```
在不指定线程的情况下， RxJava 遵循的是线程不变的原则，即：在哪个线程调用 subscribe()，就在哪个线程生产事件；
在哪个线程生产事件，就在哪个线程消费事件。如果需要切换线程，就需要用到 Scheduler （调度器）
在RxJava 中，Scheduler ——调度器，相当于线程控制器，RxJava 通过它来指定每一段代码应该运行在什么样的线程。
RxJava 已经内置了几个 Scheduler ，它们已经适合大多数的使用场景：
Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。
行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率。
不要把计算工作放在 io() 中，可以避免创建不必要的线程。
Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。
这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
另外， Android 还有一个专用的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。
有了这几个 Scheduler ，就可以使用 subscribeOn() 和 observeOn() 两个方法来对线程进行控制了。 
* subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。
或者叫做事件产生的线程。 * observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
Observable.just(1,2,3,4)
                .subscribeOn(Schedulers.io())//订阅者发生在哪一个线程，或者指明了事件产生的线程
                .observeOn(AndroidSchedulers.mainThread())//观察者发生在那个线程，或者指明了事件消费的线程
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                    }
                });
```
4. 变换
```
 a. map()变换：一对一转换
 Observable.just("images/logo.png") // 输入类型 String
     .map(new Func1<String, Bitmap>() {
         @Override
         public Bitmap call(String filePath) { // 参数类型 String
             return getBitmapFromPath(filePath); // 返回类型 Bitmap
         }
     })
     .subscribe(new Action1<Bitmap>() {
         @Override
         public void call(Bitmap bitmap) { // 参数类型 Bitmap
             showBitmap(bitmap);
         }
     });
  b.  flatMap()变换：一对多的转化，学生对象转换成学生选的课程
     Student[] students = ...;
     Subscriber<Course> subscriber = new Subscriber<Course>() {
         @Override
         public void onNext(Course course) {
             Log.d(tag, course.getName());
         }
         ...
     };
     Observable.from(students)
         .flatMap(new Func1<Student, Observable<Course>>() {
             @Override
             public Observable<Course> call(Student student) {
                 return Observable.from(student.getCourses());
             }
         })
         .subscribe(subscriber);
  c. lift()变换:针对事件序列的处理和再发送
  d. ompose: 对 Observable 整体的变换
```
5. 线程控制：Scheduler (二)
```
   线程的多次切换
   Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
       .subscribeOn(Schedulers.io())
       .observeOn(Schedulers.newThread())
       .map(mapOperator) // 新线程，由 observeOn() 指定
       .observeOn(Schedulers.io())
       .map(mapOperator2) // IO 线程，由 observeOn() 指定
       .observeOn(AndroidSchedulers.mainThread)
       .subscribe(subscriber);  // Android 主线程，由 observeOn() 指定

     ![图4](https://github.com/flyingtercel/RxJavaComps/blob/master/rxjava/src/main/res/drawable/t3.png)
     图中共有 5 处含有对事件的操作。由图中可以看出，①和②两处受第一个 subscribeOn() 影响，运行在红色线程；③和④处受第一个 observeOn() 的影响，运行在绿色线程；⑤处受第二个 onserveOn() 影响，运行在紫色线程；而第二个 subscribeOn() ，由于在通知过程中线程就被第一个 subscribeOn() 截断，因此对整个流程并没有任何影响。这里也就回答了前面的问题：当使用了多个 subscribeOn() 的时候，只有第一个 subscribeOn() 起作用。
```
 doOnSubscribe()的使用：
 ```
 在前面讲 Subscriber 的时候，提到过 Subscriber 的 onStart() 可以用作流程开始前的初始化。
 然而 onStart() 由于在 subscribe() 发生时就被调用了，因此不能指定线程，而是只能执行在 subscribe() 被调用时的线程。
 这就导致如果 onStart() 中含有对线程有要求的代码（例如在界面上显示一个 ProgressBar，这必须在主线程执行），
 将会有线程非法的风险，因为有时你无法预测 subscribe() 将会在什么线程执行。
 而与 Subscriber.onStart() 相对应的，有一个方法 Observable.doOnSubscribe() 。
 它和 Subscriber.onStart() 同样是在 subscribe() 调用后而且在事件发送前执行，但区别在于它可以指定线程。
 默认情况下， doOnSubscribe() 执行在 subscribe() 发生的线程；而如果在 doOnSubscribe() 之后有 subscribeOn() 的话，
 它将执行在离它最近的 subscribeOn() 所指定的线程。
 Observable.create(onSubscribe)
     .subscribeOn(Schedulers.io())
     .doOnSubscribe(new Action0() {
         @Override
         public void call() {
             progressBar.setVisibility(View.VISIBLE); // 需要在主线程执行
         }
     })
     .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
     .observeOn(AndroidSchedulers.mainThread())
     .subscribe(subscriber);
```
