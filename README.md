# Overview
Legodroid is a tiny Java library for Android with educational purposes,
aimed at writing programs for LEGO Mindstorm in a simple and
straightforward way.

Among its features:
 * strong-typed API for communicating with EV3 bricks: easy connection,
 robust access to motors and sensors;
 * bluetooth supported, wifi will come;
 * sound patterns for transparent asyncronous programming;
 * layered API allows for easy customization of commands/replies;

## Disclaimer
This library comes *as-is*, without any warranty of future development.
        
## Installation
This project can be found on [JitPack](https://jitpack.io).
In order to use it inside you have to include the Jitpack repository
inside your project's `build.gradle` file with the following code

```
repositories {
    maven { url 'https://jitpack.io' }
}
```

After that, you can start using it inside any `build.gradle` file by
simply writing

```
implementation 'com.github.RiccardoM:legodroid:1.0.1'
```

or, if you are using a Gradle version before Gradle 3.2

```
compile 'com.github.RiccardoM:legodroid:1.0.1'
```


## Documentation
The documentation is not yet available.
Refer to the sample MainActivity code for learning how the library
public API works. Its usage is pretty straightforward and requires no
senior expertise unless you want to delve into its internals.
