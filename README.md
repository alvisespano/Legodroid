# Overview

Legodroid is a tiny Java library for Android with educational purposes, aimed at writing programs for LEGO Mindstorms in a simple and straightforward way.

Among its features:
    * strong-typed API for communicating with EV3 bricks: easy connection, robust access to motors and sensors;
    * bluetooth supported, wifi will come;
    * sound patterns for transparent asyncronous programming;
    * layered API allows for easy customization of commands/replies;

## Disclaimer

This library is in alpha stage of development: minor bugs and quirks might occur. Be patient please, we will fix them soon :)
    
## Credits

Written by Alvise Spanò, additional coding by Giulio Zausa.
Developed for the course of Software Engeneering, degree in Computer Science, chair professor Agostino Cortesi.
(C) 2018 Università Ca' Foscari, Venezia, Italy
        
## Installation

Open the project on Android Studio and 2 modules will appear: the 'app' module contains a sample MainActivity showing how to use the library; the 'lib' module contains the library code. Add a dependency to the 'lib' module if you need to create your own standalone app and module.

## Installation as pure library in your own project

1) First of all, clone or download into a folder this repository, let's call it Legodroid, once you have done this.
2) Create a new project on Android Studio, be careful to select the minimum Sdk as 21.
3) After all the normal operation and gradle sync, right click into app module and select open module settings.
4) On the new window that appears, click on the upper left '+' and select import gradle project, browse for the Legodroid folder cloned before and select the "Lib" subfolder.
5) After all the gradle operation, repeat point 3) and proceed as follow, click on app module and select the tab "dependencies", click on the bottom '+' and select module dependency, when asked, select Lib module already imported.
6) Let gradle sync another time, and you will be set-up and ready to code.

NB. If any problem raise about sdk, probably you did not select 21 as minSdk, to correct this you should edit your gradle build under app module folder.

Guide by Diego.

## Documentation

The documentation is not yet available.
Refer to the sample MainActivity code for learning how the library public API works - its usage is pretty straightforward and requires no senior expertise unless you want to delve into its internals.
