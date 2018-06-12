# Ticket Service



## Running It

Building the project: 

```sh
gradle clean shadowJar
```



Running the application: 

```sh
java -jar build/libs/ticket-service-1.0-SNAPSHOT-all.jar \
--rows 20 \
--columns 50 \
--ttlInMillis 10000 \
--numWorkers 10
```



## Running Tests

```sh
gradle clean test
```



## Assumptions

- Its rare to get a 1 person reservation. This means we do not want to leave individual seats empty in a row. We should strive to leave at least two seats together
- All parties in a reservation want to sit together in the same row.
- If there not enough unreserved contiguous seats for a given reservation request, this application will not suggest that seats that are scattered throughout the venue. 
- The venue map has a retangular shape, e.g. `n x m`. 



 

