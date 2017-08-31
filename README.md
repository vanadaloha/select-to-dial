What is this?
=============

*** right now this is Work-In-Progress. The program is a mock-up, instead of dialing it will only display a message box with the phone number it would dial ***

This single source file is all the code you will need to create a small Java desktop application to dial any phone number shown on your screen using Aloha.

Build
=====

- Install a Java SDK >= 1.7
- Type ***javac SelectToDial.java***
( For maximum backward compatibility you can use ***javac -source 1.7 -target 1.7 SelectToDial.java*** with a newer JDK installed; eventually with minor code changes you could even go back to 1.4 ).
- The above will generate some ***SelectToDial__.class*** files.

Run
===

- Get a recent Java runtime
- Type ***java SelectToDial*** in a directory where SelectToDial.class exists.
- You will get a "VD" (Vanad Dial) icon in the notification space.
  
