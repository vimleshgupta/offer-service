# offer-service

offer-service allows to create a new offer, get and cancel the offer. The offer will automatically expire if the end date has passed.


The offer-service exposes following APIs:
1. POST http://localhost:8080/api/v1/offer - Create a new Offer
1. GET http://localhost:8080/api/v1/offer/{id} - Get an existing offer
1. DELETE http://localhost:8080/api/v1/offer/{id} - Cancel the offer

### The project directory structure
It is a Spring boot application and used gradle to build the application.
 
    Technology stacks:
    1. Java
    2. Spring Boot
    3. H2 Database (in-memory database)
    4. Gradle (Build tool)
    5. Groovy and Spock Framework (For unit tests)

It follows the standard directory structure used in most projects:
```Gherkin
src
  + main
    + java                         Main production code
    + resources                    Configuration properties
  + test
    + groovy                       Tests
  + start.sh                       build and run the application
```

## Run the application
You can run either just run `sh start.sh` or build and run with gradle command `./gradlew clean build` from command line.

This will build and start the application on default port `8080` (specified in `start.sh` file), and you can perform following actions from postman:

1. `POST`  `http://localhost:8080/api/v1/offer`

Request Body should be in this format:
```
{
    "name":"UP TO 50% OFF",
    "startDate" : "2022-02-20T04:26:00",
    "endDate" : "2022-08-20T04:26:00"
}
```
and you should get a response in this format:
```
{
    "id": 1,
    "name": "UP TO 50% OFF",
    "startDate": "2022-02-20T04:26:00.000+00:00",
    "endDate": "2022-08-20T04:26:00.000+00:00",
    "status": "ACTIVE"
}
```

2. `GET` `http://localhost:8080/api/v1/offer/{id}` - `id` should be an id of existing offer

and you should get a response in this format:
```
{
    "id": 1,
    "name": "UP TO 50% OFF",
    "startDate": "2022-02-20T04:26:00.000+00:00",
    "endDate": "2022-08-20T04:26:00.000+00:00",
    "status": "ACTIVE"
}
```

3. `DELETE` `http://localhost:8080/api/v1/offer/{id}` - `id` should be an id of existing offer

and you should get a response in this format:
```
{
    "id": 1,
    "name": "UP TO 50% OFF",
    "startDate": "2022-02-20T04:26:00.000+00:00",
    "endDate": "2022-08-20T04:26:00.000+00:00",
    "status": "CANCELLED"
}
```

#### Run the application on different port
Update the `start.sh` with custom port (`-server.port=8083`) and run it. Now the application will run on `8083` port.






