# What this project has.

1. A configured local H2 Database 
2. Liquibase Setup to create tables in the database 
3. Spring Data JPA- Hibernate with a few entities and repository, 
4. Spring Web - A Rest controller to be able to save objects using the repository.

## How to run
Just pick the main class with the @SpringBootApplication annotation and start it. 

### Useful Links
1. H2 console will be available at http://localhost:8080/h2_console
2. Postman collection is available in the postman folder. Import it into postman and you should be able to
   * POST - create a player and see a player id in the response.  
   * GET - get a player by id. 
