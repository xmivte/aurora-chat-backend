## README

This is template/example repository for BE project for Sourcery Academy's 2025 Spring session.

It is possible to run linting(Spotless, SpotBugs, PMD) on demand with command ./gradlew check

DB Credentials: username: `db_user`, password: `password` (in application.yml)  
Start required services for the project with: `docker compose -p edvinas-be up -d`  
Run Spring Boot application with: `./gradlew bootRun`  


API docs

Local:
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

Deployed:
Swagger UI: https://aurora-chat.api.devbstaging.com/swagger-ui/index.html
OpenAPI JSON: https://aurora-chat.api.devbstaging.com/v3/api-docs
