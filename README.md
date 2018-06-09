# Ticket Service





## Design Notes





## Assumptions

### Business Context

- Its rare to get a 1 person reservation. This means we do not want to leave individual seats empty in a row. We should strive to leave at least two seats together
- All parties in a reservation want to sit together in the same row.
- If there not enough unreserved contiguous seats for a given reservation request, this application will not suggest that seats that are scattered throughout the venue. 

#### Meaning of "best available"

What constitutes the "best available" seating for the venue? This application offers a couple different implementation strategies for this.  They are:

- ==update me== 

### Technical

- The venue map has a retangular shape, e.g. `n x m`. 

- 

   

