# MovieAdvisor

A movie and event recommendation platform for individuals and groups of users. Available for Android 5.1+ devices.

## Demo

https://www.youtube.com/watch?v=L51uZSH96l4

## Structure

This repository contains the source code for the MovieAdvisor system, which is made up of the following:

### 1. Backend Web API (Python)
- This folder contains the source code for the web REST API (Flask), as well as the implementation of the recommendation systems.
- This folder can be zipped and directly deployed onto Elastic Beanstalk (AWS).

### 2. Frontend Android Application (Java)
- This folder contains the source code for the Android application.
- The application was created using Java and Android Studio. It can be compiled and installed on devices running Android 5.1+. 

### 3. MySQL Database (SQL)
- The 'public.sql' file contains the SQL statements that create a database needed by the backend API.
- These statements insert all of the required data — including the training data set used by the recommendation systems.
