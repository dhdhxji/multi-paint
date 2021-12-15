# Multi-paint server. Multiplayer paint server
This project was developed for lab. There is multi user multi-thread server application for simultaneously receiving and processing draw commands from multiple users.

# How to use
## Run with maven
To run the server locally with the Maven exec plugin, simple execute: 
```
mvn exec:java
```

## Fat jar
To run the server, You can build a fat jar and run it with java:
```
mvn package
java -jar target/multi-paint-fat.jar
```

## Run with docker
[This](https://hub.docker.com/r/sashakovalchuk/multi-paint) DockerHub repository contains the latest version of the server Docker image. It can be run like:
```
docker run -p 3113:3113\
    -e REDIS_ADDR=localhost\
    -e CANVAS_WIDTH=700\
    -e CANVAS_HEIGHT=500\
    sashakovalchuk/multi-paint:0.2
```
```
IMPORTANT: In all cases above, Redis should be run on localhost, or its address should be specified with REDIS_ADDR environment variable/
```

## Run with k8s
This app is able to run on the Kubernetes cluster. To setup it, execute:
```
kubectl apply -f kubernetes
```
and get the external IP address of the multi-paint server to access it.

## Run with k8s in Azure
This app is adapted to set up and run in the Azure AKS environment. To run it, put the Azure active directory credentials into tf/terrafor.tfvars, like this:
```
client_id    =  "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
client_secret = "bbbbbbb-bbbbbbbb-bbbbbbbbbbb.bbbbb"
``` 
and run:
```
./deploy.sh
```

# What's next?
To use this application, You need the appropriate [client](https://github.com/dhdhxji/multi-paint-client). 

# Available commands on the server
All the commands has the next structure: it's json string with the next fields: 
```json
{
  "command": {
    "command-name": "[command-respresent string]",
    "command-data": {
      //some data for this particular command
    }
  }
}
```

Input commands:
* Login, received with user connection to the server
  ```json
  {
    "command": {
      "command-name": "login",
      "command-data": {
        "username": "Petia"
      }
    }
  }
  ```
* Set pixel, setting pixel on the canvas:
  ```json
  {
    "command": {
      "command-name": "set",
      "command-data": {
        "x": 42,  //x coordinate of pixel
        "y": 42,  //y coordiante of pixel
        "color": 1234 //24-bit RGB color
      }
    }
  }
  ```
* Add circle ont the canvas:
  ```json
  {
    "command": {
      "command-name": "circle",
      "command-data": {
        "x": 42,  //x coordinate of center
        "y": 42,  //y coordiante of center
        "radius": 42, //circle radius
        "color": 1234 //24-bit RGB color
      }
    }
  }
  ```
* Request rect, requests part of pixmap from the server: 
  > :warning: This feature will not be implemented soon
  ```json
  {
    "command": {
      "command-name": "request-rect",
      "command-data": {
        "a_point": {
          "x": 42,  //-1 request all the pixmap
          "y": 42   //-1 request all the pixmap
        },
        "b_point": {
          "x": 42,  //-1 request all the pixmap
          "y": 42   //-1 request all the pixmap
        }
      }
    }
  }
  ```
Output commands:
* Set pixel, setting pixel on client canvas:
  ```json
  {
    "command": {
      "command-name": "set",
      "command-data": {
        "x": 42,  //x coordinate of pixel
        "y": 42,  //y coordiante of pixel
        "color": 1234 //24-bit RGB color
      }
    }
  }
  ```
* Set strip. sent data is sequence of colors starting from specified point
  ```json
  {
    "command": {
      "commnad-name": "strip",
      "command-data": {
        "start-x": 42,
        "start-y": 42,
        "pixels": [42, 42, 42, 42]
      }
    }
  }
  ```
* Size. Tell size of the pixmap to client 
  ```json
  {
    "command": {
      "commnad-name": "size",
      "command-data": {
        "w": 42,
        "h": 42,
      }
    }
  }
  ```
* Add circle ont the canvas:
  ```json
  {
    "command": {
      "command-name": "circle",
      "command-data": {
        "x": 42,  //x coordinate of center
        "y": 42,  //y coordiante of center
        "radius": 42, //circle radius
        "color": 1234 //24-bit RGB color
      }
    }
  }
  ```