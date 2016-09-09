# Deployment portal Agent web application
## How to start a Jetty server 
### For AWS
    
    mvn jetty:run -P aws -Djetty.port=8021
    
will start agent on locally on [http://localhost:8021](http://localhost:8021)     

### For Verizon Terremark

    mvn jetty:run -P verizon -Djetty.port=8022
    
will start agent on locally on [http://localhost:8022](http://localhost:8022)     
    

## How to build WAR 
### For AWS

    mvn clean package
or

    mvn clean package -P aws
will build `aws-agent-rest-*.war`     

### For Verizon Terremark

    mvn clean package -P verizon
will build `verizon-agent-rest-*.war`     

