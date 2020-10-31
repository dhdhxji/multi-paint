# Multi-paint server. Multiplayer paint server
This project was developed for lab. There is multi user multi-thread server application for simultaneously receiving and processing draw commands from multiple users.

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
    "commnad": "strip",
    "command-data": {
      "start-x": 42,
      "start-y": 42,
      "pixels": [42, 42, 42, 42]
    }
  }
  ```