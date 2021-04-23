# Lib_Demo
Android开发常用工具类、控件等封装

## 分支：
- master：正在维护的分支，开源库发布到Maven Central；
- dev_jcenter：包含发布开源库到JCenter的配置；代码不再同步。

## 模块介绍：

- lib_logs：logcat日志工具；
- lib_network：网络框架封装；
- lib_utils：常用的工具类；
- lib_widgets：常用的自定义组件；

## 发布注意事项：
- 1、修改project.properties文件，确认发布哪个library；
- 2、修改对应library的versionName，升级版本号；
- 3、去 https://bintray.com/caowj/LibRepo  查看结果；