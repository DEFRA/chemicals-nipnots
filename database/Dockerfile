FROM sndeuxfesacr001.azurecr.io/reach-liquibase-base:1.0

ADD src/main/resources /liquibase/changelog
RUN echo "changeLogFile: db/changelog/db.changelog-master.yaml" >> /liquibase/liquibase.docker.properties
