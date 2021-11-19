# Reach Nipnots

### Environment variables

The following environment variables are required to run locally (beyond the variables required to run the other reach applications):

```
DB_REACH_NIPNOTS_URL=jdbc:sqlserver://localhost:1433;database=reach-nipnots;
CHEMICAL_REGULATIONS_URL="https://comply-chemical-regulations.service.gov.uk/"
APPLICATION_INSIGHTS_IKEY=A (if you don't already have a value set for this variable)
```

### Running and developing locally

reach-nipnots requires a MSSQL DB to run.

Assuming there is already a docker container named `reach-database` which would have been created as part of the chemicals `build.sh` you can create the
`reach-nipnots` DB using the command

```
docker exec -i reach-database /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P L0calDBPass < database/setupDb.sql
``` 

#### Liquibase

To run the latest Liquibase changesets against the DB, from the `database` directory run

```
mvn process-resources
```
