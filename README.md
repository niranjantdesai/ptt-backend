# Pomodoro Time Tracker Backend
The Pomodoro technique is a time management method that uses a timer to break down work into intervals, typically 25 minutes in length, separated by short breaks of generally 5 minutes. You can learn more about the Pomodoro technique [here](https://en.wikipedia.org/wiki/Pomodoro_Technique).

Pomodoro Time Tracker was developed in the course CS 6301: Advanced Software Engineering taught by Prof. Alex Orso in Spring 2019 at Georgia Tech. The class was divided into multiple teams of four and each team was responsible for one of the following: backend, frontend web, frontend mobile and DevOps.

Our backend was developed with Node.js, TypeScript and MongoDB. Instructions for running the backend server and unit tests are given below.

## Instructions to run the backend server:
* Clone repo
  ```
  git clone https://github.com/niranjantdesai/ptt-backend
  ```
* Go to the the code directory
  ```
  cd PTTBackend2/src/
  ```
* Install node version 8.10.0. Follow the instructions [here](https://github.com/nodesource/distributions/blob/master/README.md).
* Install the latest stable version of MongoDB from https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/
* Start MongoDB service
  ```
  sudo service mongod start
  ```
* Install all packages for the server
  ```
  npm install
  ```
* Start the server
  ```
  npm start
  ```
* If everything went correctly, then you should be able to see the following message:
    ```
    Connected to MongoDB database
    Backend server listening on 8080
    ```

## Instructions to run the test cases:
* Set the environment variable (PTT_URL). For example:
  ```
  export PTT_URL=http://localhost:8080/ptt
  ```
* From the root directory of the project, run the test cases for a Backend team (1,2,3). For example:
  ```
  gradle BackendTestsBackend1
  ```
* From the root directory of the project, run the test cases for a Web team (1,2,3,4). For example:
  ```
  gradle BackendTestsWeb1
  ```
* From the root directory of the project, run the test cases for a Mobile team (1,2,3,4). For example:
  ```
  gradle BackendTestsMobile1
  ```
* From the root directory of the project, run the test cases for a DevOps team (12,34). For example:
  ```
  gradle BackendTestsDevOps12
  ```