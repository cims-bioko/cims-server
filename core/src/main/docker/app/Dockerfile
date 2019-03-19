FROM cimsbioko/oracle-java8-awsrds-ssl
LABEL maintainer="brent.atkinson@gmail.com"

EXPOSE 8080

COPY app/ /app
COPY application.properties /

VOLUME "/app-storage/data"
VOLUME "/app-storage/forms"
VOLUME "/app-storage/search"
VOLUME "/app-storage/submissions"

CMD java -jar app/${jarFilename}