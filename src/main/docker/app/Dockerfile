FROM cimsbioko/oracle-java8-awsrds-ssl
LABEL maintainer="brent.atkinson@gmail.com"

EXPOSE 8080

VOLUME "/app-storage/data"
VOLUME "/app-storage/forms"
VOLUME "/app-storage/submissions"
VOLUME "/app-storage/logs"

COPY app/ /app
COPY application.properties /

CMD java -jar app/${jarFilename}