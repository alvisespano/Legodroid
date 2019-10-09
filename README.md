# Overview

Legodroid is a tiny Java library for Android with educational purposes, aimed at writing programs for LEGO Mindstorms in a simple and straightforward way.

Among its features:
 - strong-typed API for communicating with EV3 bricks: easy connection, robust access to motors and sensors;
 - bluetooth supported, wifi will come;
 - sound patterns for transparent asyncronous programming;
 - layered API allows for easy customization of commands/replies;

## Disclaimer

This library is in alpha stage of development: minor bugs and quirks might occur. Be patient please, we will fix them soon :)

## Credits

Written by Alvise Spanò, additional coding by Giulio Zausa.
Developed for the course of Software Engineering, degree in Computer Science, chair professor Agostino Cortesi.
Documentation written by Alvise Spanò.
(C) 2018 Università Ca' Foscari, Venezia, Italy

## Installation

Open the project on Android Studio and 2 modules will appear: the 'app' module contains a sample MainActivity showing how to use the library; the 'lib' module contains the library code. Add a dependency to the 'lib' module if you need to create your own standalone app and module.

## Documentation

The documentation is available as javadoc. Refer to the doc directory for pre-generated html.
Also, the project includes a sample MainActivity aimed at showing how the library public API works - its usage is pretty straightforward and requires no senior expertise unless you want to delve into its internals.
