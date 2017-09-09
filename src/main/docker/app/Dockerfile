FROM cimsbioko/oracle-java8-awsrds-ssl
LABEL maintainer="brent.atkinson@gmail.com"

EXPOSE 8080
VOLUME "/app-storage"

COPY app/ /app
COPY application.properties /

CMD java -jar app/${jarFilename}