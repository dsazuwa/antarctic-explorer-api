# Antarctica Explorer Api

This application scrapes information related to Antarctica expeditions from
various cruise line websites and exposes endpoints for accessing the scraped
data.
___

## Getting Started

To get a local copy up and running, please follow these simple steps.

### Prerequisites

Before you begin, ensure you have the following installed on your local machine:

- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)

You'll likely be better served just
installing [Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)

### Running the application locally

1. Clone the repo
    ```
    git clone https://github.com/dsazuwa/antarctic-explorer-api
    ```
2. Go to the project folder
    ```
    cd antarctic-explorer-api
    ```
3. Set up your .env file
    - Duplicate the `.env.sample` to `.env`
    - Fill in the missing variables
    - Be sure to replace `<DB_NAME>` in `jdbc:postgresql://db:5432/<DB_NAME>`
      <br/><br/>
4. Build the application
   ```
    mvn clean package
    ```
5. Run the application
   ```
    docker-compose up -d
    ```

___

## Dumping Data

Seed data is provided to allow for a quick demo. On any schema change, be sure
to re-dump data from your PostgreSQL database using the `pg_dump` command-line
utility.

- One your local terminal, run the following command with the appropriate
  options.

   ```bash
   docker exec antarctica-explorer-db-1 pg_dump -d <DB_NAME> -U <DB_USER> -a --encoding utf8 -f postgresql/seed.sql

___

## Upcoming Features
- [ ] Use Spring [Task Execution](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.task-execution-and-scheduling) and [Messaging](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka) to handle executing long running scrapping tasks
- [ ] Implement PostgreSQL full text search and expose endpoint for searching expeditions
- [ ] Scrape
  - [ ] Quark Expeditions
  - [ ] Ponant
  - [ ] Viking Expeditions

___

## Endpoints

Contains general input logic and validation: incomes/expenses items, savings and
account settings.

| Method	 | Path	                                                 | Description	                         |
|---------|:------------------------------------------------------|:-------------------------------------|
| GET	    | /api/cruise-lines	                                    | Get all cruise line objects          |
| GET	    | /api/cruise-lines/names	                              | Get all cruise line names            |
| GET	    | /api/cruise-lines/{id}	                               | Get specified cruise line            |
| GET	    | /api/cruise-lines/{id}/expeditions/{name}	            | Get specified expedition             |
| GET	    | /api/cruise-lines/{id}/expeditions/{name}/departures	 | Get specified expedition's departure |
| GET	    | /api/expeditions	                                     | Get all expeditions                  |
| GET	    | /api/vessels	                                         | Get all vessels                      |
| GET	    | /api/vessels/{id}	                                    | Get specified vessel                 |

___

### GET /api/expeditions

Get expeditions with pagination and optional filtering

**Parameters**

|           Name |    Default    |   Type   | Description                                                                                                                       |
|---------------:|:-------------:|:--------:|-----------------------------------------------------------------------------------------------------------------------------------|
|         `page` |       0       | Integer  | The page index for pagination, where indexing starts from 0. Must not be negative.                                                |
|         `size` |       6       | Integer  | The size of the page. Must be greater than 0.                                                                                     |
|         `sort` | `nearestDate` |  String  | The field upon which to sort expeditions by. <br/><br/> Supported values: `name`, `cruiseLine`, `startingPrice` or `nearestDate`. |
|          `dir` |     `asc`     |  String  | The sort direction. <br/><br/> Supported values: `asc` or `desc`. Any value other than  `desc` will coalesce to `asc`.            |
|    `startDate` |       x       |   Date   | The earliest date an expedition can depart. <br/><br/> The string must be in the `yyyy-MM-dd` format.                             |
|      `endDate` |       x       |   Date   | The latest date an expedition can depart. <br/><br/> The string must be in the `yyyy-MM-dd` format.                               |
|  `cruiseLines` |       x       | String[] | An array of cruise lines to filter expeditions by.                                                                                |
| `duration.min` |       x       | Integer  | The minimum number of days an expedition can last.                                                                                |
| `duration.max` |       x       | Integer  | The maximum number of days an expedition can last.                                                                                |
| `capacity.min` |       x       | Integer  | The minimum vessel capacity an expedition can have.                                                                               |
| `capacity.max` |       x       | Integer  | The maximum vessel capacity an expedition can have.                                                                               |

<details>
  <summary>Response</summary>

  ```js
      {
         cruiseLines: string[];
         expeditions: {
            data: {
                cruiseLine: {
                    id: number;
                    name: string;
                    logo: string;
                };
                logo: string;
                name: string;
                duration: string;
                startingPrice: number | null;
                nearestDate: Date | null;
                photoUrl: string;
            }[];
            itemsPerPage: number;
            totalItems: number;
            totalPages: number;
            currentPage: number;
         }
      }
  ```

</details>

___

### GET /api/cruise-lines/{id}/expeditions/{name}

Get data for the expedition with the provided id.

#### Parameters

|           Name |    Default    |   Type   | Description                                                                                                                       |
|---------------:|:-------------:|:--------:|-----------------------------------------------------------------------------------------------------------------------------------|
|         `page` |       0       | Integer  | The page index for pagination, where indexing starts from 0. Must not be negative.                                                |
|         `size` |       6       | Integer  | The size of the page. Must be greater than 0.                                                                                     |
|         `sort` | `nearestDate` |  String  | The field upon which to sort expeditions by. <br/><br/> Supported values: `name`, `cruiseLine`, `startingPrice` or `nearestDate`. |
|          `dir` |     `asc`     |  String  | The sort direction. <br/><br/> Supported values: `asc` or `desc`. Any value other than  `desc` will coalesce to `asc`.            |
|    `startDate` |       x       |   Date   | The earliest date an expedition can depart. <br/><br/> The string must be in the `yyyy-MM-dd` format.                             |
|      `endDate` |       x       |   Date   | The latest date an expedition can depart. <br/><br/> The string must be in the `yyyy-MM-dd` format.                               |
|  `cruiseLines` |       x       | String[] | An array of cruise lines to filter expeditions by.                                                                                |
| `duration.min` |       x       | Integer  | The minimum number of days an expedition can last.                                                                                |
| `duration.max` |       x       | Integer  | The maximum number of days an expedition can last.                                                                                |
| `capacity.min` |       x       | Integer  | The minimum vessel capacity an expedition can have.                                                                               |
| `capacity.max` |       x       | Integer  | The maximum vessel capacity an expedition can have.                                                                               |

<details>
  <summary>Response</summary>

  ```js
      {
         id: number;
         name: string;
         description: string[];
         highlights: string[];
         duration: string;
         startingPrice: number;
         website: string;
         photoUrl: string;
         cruiseLine: {
            id: number;
            name: string;
            logo: string;
         };
         gallery: {
            alt: string | null;
            url: string;
         }[];
         vessels: {
            name: string;
            description: string[];
            cabins: number;
            capacity: number;
            photoUrl: string;
            website: string;
         }[];
         itineraries: {
            id: number;
            name: string;
            startPort: string;
            endPort: string;
            duration: number;
            mapUrl: string;
            schedule: {
               day: string;
               header: string;
               content: string[];
            }[];
         }[];
         departures: {
            startDate: Date;
            endDate: Date;
         }[];
         extensions: {
            name: string;
            startingPrice: number;
            duration: number;
            website: string;
            photoUrl: string;
         }[];
         otherExpeditions: {
            id: number;
            name: string;
            duration: string;
            nearestDate: Date;
            startingPrice: number;
            photoUrl: string;
         }[];
      }
  ```

</details>

<details>
  <summary>Response Details</summary>

| Field            | Type     | Description                                     |
|------------------|----------|-------------------------------------------------|
| id               | number   |                                                 |
| name             | string   |                                                 |
| description      | string[] |                                                 |
| highlights       | string[] | A list of expedition highlights.                |
| duration         | string   | Expedition duration in days.                    |
| startingPrice    | number   | COALESCE(discounted_price, starting_price)      |
| website          | string   | External expedition website URL.                |
| photoUrl         | string   |                                                 |
| cruiseLine       | object   | Basic cruise line details.                      |
| gallery          | object[] | Images for the highlight carousel.              |
| vessels          | object[] | Vessels used in this expedition.                |
| itineraries      | object[] | Details of all itineraries for this expedition. |
| departures       | object[] | Future departures.                              |
| extensions       | object[] | Optional trip extensions.                       |
| otherExpeditions | object[] | Information on 3 other similar expeditions.     |

</details>

___

### GET /api/cruise-lines/{id}/expeditions/{name}/departures

Get departures for the expedition with the provided id

**Parameters**

|   Name |   Default   |  Type   | Description                                                                                                            |
|-------:|:-----------:|:-------:|------------------------------------------------------------------------------------------------------------------------|
| `page` |      0      | Integer | The page index for pagination, where indexing starts from 0. Must not be negative.                                     |
| `size` |      5      | Integer | The size of the page. Must be greater than 0.                                                                          |
| `sort` | `startDate` | String  | The field upon which to sort expeditions by. <br/><br/> Supported values: `startDate` or `price`.                      |
|  `dir` |    `asc`    | String  | The sort direction. <br/><br/> Supported values: `asc` or `desc`. Any value other than  `desc` will coalesce to `asc`. |

<details>
  <summary>Response</summary>

 ```js
{
   data: {
      id: number;
      name: string;
      itinerary: string;
      vessel: string | null;
      departingFrom: string | null;
      arrivingAt: string | null;
      duration: string;
      startDate: Date;
      endDate: Date;
      startingPrice: number;
      discountedPrice: number | null;
      website: string;
   }[];
   itemsPerPage: number;
   totalItems: number;
   totalPages: number;
   currentPage: number;
}
```

</details>

___