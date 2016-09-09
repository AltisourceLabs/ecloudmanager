# Deployment portal web application

### Requirements
- Java 8
- a running [MongoDB server](https://www.mongodb.com/download-center#community)


### Starting the server from Maven

MongoDB server should run on `localhost` port `27017` (default MongoDB port)
After [the whole project](https://github.com/AltisourceLabs/ecloudmanager) build completed run the following command from this sub-project :

    mvn wildfly:run 
    
You can connect to the server via [http://localhost:8080/ecloud-manager-web](http://localhost:8080/ecloud-manager-web).
Default user name/password are `admin`/`sectret`