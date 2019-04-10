## Instructions to run the backend server:
* ##### Clone repo in any directory
  +  ###### git clone https://github.gatech.edu/gt-se-cs6301-spring2019/6301Spring19Backend2.git
* ##### Go to the the code directory
  +  ###### cd 6301Spring19Backend2/PTTBackend2/src/
* ##### Install node version 8.10.0
  + ###### Follow the instructions at https://github.com/nodesource/distributions/blob/master/README.md
* ##### Install the latest stable version of mongodb
  + ###### https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/
* ##### Start mongodb service
  +  ###### sudo service mongod start
* ##### Install all packages for my server
  +  ###### npm install
* ##### Start my server
  +  ###### npm start
* ##### If everything went correctly, then you should be able to see the following message after starting the server:
    ###### Connected to MongoDB database
    ###### Backend server listening on 8080

## Instructions to run the test cases:
* ##### Set the environment variable (PTT_URL). For example:
  +  ###### export PTT_URL=http://localhost:8080/ptt
* ##### From the root directory of the project, run the test cases for a Backend team (1,2,3). For example:
  +  ###### gradle BackendTestsBackend1
* ##### From the root directory of the project, run the test cases for a Web team (1,2,3,4). For example:
  +  ###### gradle BackendTestsWeb1
* ##### From the root directory of the project, run the test cases for a Mobile team (1,2,3,4). For example:
  +  ###### gradle BackendTestsMobile1
* ##### From the root directory of the project, run the test cases for a DevOps team (12,34). For example:
  +  ###### gradle BackendTestsDevOps12