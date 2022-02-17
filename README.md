# AnyText

## What's this

This application provides features to modify any TextView in any other applications.

本应用提供了修改原生Android应用的文本功能。

## How does it work

This application depends on the [Xposed](https://github.com/rovo89/Xposed) framework. For more information about this framework, just visit their [website](https://api.xposed.info/).

Here's the hook process:

1. Hook the `onCreate` method of `Application` .
2. After application hook, register an `ActivityLifecycleCallback`.
3. In `ActivityLifecycleCallback`, hook the `OnClickListener` or directly set it if there's no listener set inside the `TextView`, set it to my own customized one.

Quite simple right?

本应用依赖于[Xposed框架](https://github.com/rovo89/Xposed)，如果想要获取更多关于本框架的信息，请访问他们的[官网](https://api.xposed.info/)。

应用Hook流程：

1. Hook掉`Application`类的`onCreate`方法。
2. 在执行`onCreate`之后，注册`ActivityLifecycleCallback`回调。
3. 在`ActivityLifecycleCallback`中，替换掉`TextView`中的`OnClickListener`，把它设置成自己的封装实现。

是不是挺简单的，你也可以自己写一个。

## Caution

This application is completely free and 100% open-source. It's just for learning usage. I don't take any responsibilities for the result you make while using this application.

本应用完全开源免费，且仅供学习交流使用。使用者在使用本应用的过程产生的任何后果都与开发者无关。

## Finally

Screenshots CANNOT be used as evidences!

都说了截图能当个锤子的证据.jpg