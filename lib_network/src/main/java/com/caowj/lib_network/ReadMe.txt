
包含三种网络框架的封装：

1、Retrofit的封装；
    1.1、参考LegoArch
    1.2、基于Retrofit2的简单使用
2、volley的封装；
3、OKHttp的封装；


1、为什么没有Retrofit2的集成：

Retrofit2本身就是基于OkHttp3封装的，适合API形式，不适合URL形式。需要单独处理。

再者Retrofit提供了很多注解，不需要代码完成。如果再配合RxJava就更加个性了。



2、参考项目：https://github.com/7449/Retrofit_RxJava_MVP

源码参考：https://github.com/7449/Retrofit_RxJava_MVP/tree/master/app/src/main/java/com/example/y/mvp/network


