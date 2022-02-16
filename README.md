# AnyText

## What's this

This application provides features to modify any TextView in any other applications.

## How does it work

This application depends on the [Xposed](https://github.com/rovo89/Xposed) framework. For more information about this framework, just visit their [website](https://api.xposed.info/).

Here's the hook process:

1. Hook the `onCreate` method of `Application` .
2. After application hook, register an `ActivityLifecycleCallback`.
3. In `ActivityLifecycleCallback`, hook the `OnClickListener` or directly set it if there's no listener set inside the `TextView`, set it to my own customized one.

Quite simple right?

## Caution

This application is completely free and 100% open-source. It's just for learning usage. I don't take any responsibilities for the result you make while using this application.

## Finally

Screenshots CANNOT be used as evidences!
