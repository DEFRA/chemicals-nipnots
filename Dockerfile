FROM defradigital/java:latest-jre

ARG BUILD_VERSION

USER root

RUN mkdir -p /usr/src/reach-nipnots
WORKDIR /usr/src/reach-nipnots

COPY ./target/nipnots-${BUILD_VERSION}.jar /usr/src/reach-nipnots/reach-nipnots.jar
COPY ./target/agent/applicationinsights-agent.jar /usr/src/reach-nipnots/applicationinsights-agent.jar
COPY  ./target/classes/applicationinsights.json /usr/src/reach-nipnots/applicationinsights.json

RUN chown jreuser /usr/src/reach-nipnots
USER jreuser

EXPOSE 8100

CMD java -javaagent:/usr/src/reach-nipnots/applicationinsights-agent.jar \
-Xmx${JAVA_MX:-1024M} -Xms${JAVA_MS:-1024M} -jar reach-nipnots.jar
