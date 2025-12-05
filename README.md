## README

This is template/example repository for BE project for Sourcery Academy's 2025 Spring session.

It is possible to run linting(Spotless, SpotBugs, PMD) on demand with command ./gradlew check

DB Credentials: username: `db_user`, password: `password` (in application.yml)  
Start required services for the project with: `docker compose -p edvinas-be up -d`  
Run Spring Boot application with: `./gradlew bootRun`

Run Local with: `./gradlew bootRun --args="--spring.profiles.active=local"`  
Run Prod with `./gradlew bootRun --args="--spring.profiles.active=prod"`  

API docs

Local:
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

Deployed:
Swagger UI: https://aurora-chat.api.devbstaging.com/swagger-ui/index.html
OpenAPI JSON: https://aurora-chat.api.devbstaging.com/v3/api-docs


### To test out websockets in POSTMAN:
Use Notepad++:

1. Enable View → Show Symbol → Show All Characters
2. Write the first STOMP frame:

## FIRST STOMP FRAME
CONNECT
accept-version:1.1,1.2
heart-beat:0,0

^@


3. Leave the empty line between the text and ^@ and replace ^@ with a NULL character using Edit → Character Panel → NULL (0)
- ![img.png](docs/images/img.png)
4. Select all → Plugins → MIME Tools → Base64 Encode with Padding and copy the encoded result.
5. Open POSTMAN and inside select the "Websocket" option, connect to ws://localhost:8080/ws-stomp
6. Change the message type from "Text", to "Binary" and "Base64"
7. Paste the text and send, you should see a blue received message such as CONNECTED
8. Repeat steps 2-7 with the SECOND and THIRD STOMP frames below:


## SECOND STOMP FRAME
SUBSCRIBE
id:sub-1
destination:/topic/chat.GLOBAL

^@

![img_1.png](docs/images/img_1.png)


## THIRD STOMP FRAME
SEND
destination:/app/send.message
content-type:application/json

{"senderId":1,"groupId":"GLOBAL","content":"Hello!"}

^@

![img_2.png](docs/images/img_2.png)


##### Testing websockets with POSTMAN links:
https://dev.to/danielsc/testing-stomp-websocket-with-postman-218a
https://stackoverflow.com/questions/71696431/how-to-test-stomp-application-using-postman
https://stomp.github.io/stomp-specification-1.2.html#STOMP_Frames