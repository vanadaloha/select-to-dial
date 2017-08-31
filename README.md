What is this?
=============

This single source file is all the code you will need to create a small Java desktop application to dial any phone number shown on your screen using Aloha.

Build
=====

- Install a Java SDK >= 1.7
- Clone this project.
- Type **javac SelectToDial.java** in the project directory.
( For maximum backward compatibility you can use **javac -source 1.7 -target 1.7 SelectToDial.java** with a newer JDK installed; eventually with minor code changes you could even go back to 1.4 ).
- The above will generate some **SelectToDial__.class** files.

Run
===

- Get a recent Java runtime. Note that older runtimes might have problems making a HTTPS connection.
- Execute **java SelectToDial <URL> <APIKEY> [select]** in a directory where SelectToDial.class exists. Obviously you would make a .BAT file or desktop shortcut for this. The URL must be something like **https://api.eu2.vanadcimplicity.net/v2/telephony/clicktodial/**. The APIKEY you obtain for your user from the Aloha administrator in the tab "API key". If you add the single word "select" as the third parameter, it if sufficient to only select a phone number. By default you need to copy the number to the clipboard.
- You will get a "VD" (Vanad Dial) icon in the notification space.
- Log in your agent and select or copy any Dutch phone number on your screen. You should get a message dialog asking if you want to dial the number.  
