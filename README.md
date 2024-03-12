# Antarctica Explorer Api

This application scrapes information related to Antarctica expeditions from various cruise line websites and exposes endpoints for accessing the scraped data.

## Getting Started
To get a local copy up and running, please follow these simple steps.

### Prerequisites

Before you begin, ensure you have the following installed on your local machine:

- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)

You'll likely be better served just installing [Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)

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