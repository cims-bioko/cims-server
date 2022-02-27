FROM cimsbioko/openjdk-8-awsrds-ssl:CA-2019
LABEL maintainer="brent.atkinson@gmail.com"

EXPOSE 8080

COPY app/ /app
COPY application.properties /

VOLUME "/shared-storage"
VOLUME "/app-storage"

CMD java $JAVA_OPTS -jar app/${jarFilename}