# Bayzat Takehome Assignment

## Description

Hello, this is my solution to the coding test. This component exposes a REST api with different endpoints for creating and processing of work items.  This was built using [Spring Boot](https://spring.io/projects/spring-boot/) framework, [rabbitmq](https://www.rabbitmq.com/) for producing and consuming messages, and [Mongo](https://www.mongodb.com/atlas/database) for database storage, [Jasper Reports](https://www.jaspersoft.com/), and [junit](https://junit.org/) for unit and E2E tests.

## Prerequisites

- Install Mongodb: [https://www.mongodb.com/docs/manual/installation/]
- Install Rabbitmq: [https://www.rabbitmq.com/download.html]
- Install Postman: [https://www.postman.com/downloads/]


## How to install

### Using Git (recommended)

```sh
$ git clone https://github.com/bellopromise/1wa-workitems # or clone your own fork
$ git checkout main
```
### Using manual download ZIP

1.  Download repository
2.  Uncompress to your desired directory

### Configuration

Configure the following 
##### src/main/resources/application.properties

-   `spring.rabbitmq.host`= **the host of your rabbitmq client (Default: localhost)** 
-   `spring.rabbitmq.port`= **the port of your rabbitmq (Default: 5672)**
-   `spring.rabbitmq.username`= **the username of your rabbitmq client (Default: guest)** 
-   `spring.rabbitmq.password`= **the password of your rabbitmq (Default: guest)**
-   `spring.rabbitmq.virtual-host`= **the virtual host of your rabbitmq client (Default: /)** 
-   `spring.data.mongodb.host`= **the host of your mongodb (Default: localhost)**
-   `spring.data.mongodb.port`= **the port of your mongodb (Default: 27017)**
-   `spring.data.mongodb.database`= **the name of your database (Default: 1wa)**



### Run the application using cli 
**Note: please ensure JAVA 17 is installed as the target compatibility is JAVA 17**
```bash
$ cd /path/to/solution/directory
$ ./gradlew build
$  ./gradlew bootRun --stacktrace
```

### Test the application using cli

```bash
$ ./gradlew test
$ ./gradlew test jacocoTestReport
```


### View And Download Report

- Visit `http://localhost:8080/work-items/report.html` to view the report.
- It allows you to refresh and download the pdf of the report

## API documentation

#### Postman

To access the Postman documentation, you can import the provided Postman collection into your own instance of Postman. The collection file can be found in the root directory of this repository (Work Item.postman_collection.json).

**To load test 1000 random values, kindly set the iteration on Postman to 1000.**

#### Swagger
Swagger is an interactive documentation tool that allows you to easily explore the API's endpoints, test requests, and view responses. To access the Swagger documentation, simply navigate to the following URL:


`http://localhost:8080/swagger-ui.html`

Once there, you'll be able to see all of the available endpoints, as well as details on the required parameters, expected responses, and more.


## API Endpoint
The API endpoints for this solution will be structured as follows:

#### Person
- GET /work-items
  * Retrieves a list of all work items.
- GET /work-items/{id}
  * Retrieves a work item identified by their ID.

- POST /work-items
  * Creates a new work.
  * Returns the id  of the work item
  * Request body example:

    ```bash
    {
      "value": 5
    }
    ```
  * Response body example:

    ```bash
    {
      "id": "647a5efb406dc9099538d392"
    }
    ```
  * * **Note: The value must be between 1 and 10**
- DELETE /work-items/{id}
  * Deletes a work item identified by their ID.
  * **Only Deletes an unprocessed work item**
- GET /work-items/report
  * Gets the report of work items with each value, total items and the processed items.
- GET /work-items/report-pdf
  * Downloads the report of work items.


## How to use Postman to queue the tool items

- Locate the file in the root directory with the name `Work Item.postman_collection.json`.
- Import the json file to your Postman. (Go to File > Imports...).
-  Click on the "..." (three dots) button next to the Collection name, select "Run Collection," and choose set the number of iterations to 1000.
- Click Run Work Item.
- You can also edit the url, if you use a different one.


**1000 random values between will be created and queued to the api**



## Support
For more questions and clarifications, you can reach out to me here `bellopromise5322@gmail.com`

