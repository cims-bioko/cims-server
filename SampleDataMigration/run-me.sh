# create a sample database and dump CSV data into a table

USER="--user=root"
#PASS="-pmotech4"
PASS=""

echo
echo "Initializing OpenHDS Database"
mysql $USER $PASS openhds < init-openhds-db.sql
mysql $USER $PASS openhds < openhds-schema.sql
mysql $USER $PASS openhds < openhds-required-data.sql
mysql $USER $PASS openhds < openhds-baseline-stub.sql

echo
echo "Loading Sample Data From CSV file"
mysql $USER $PASS < init-sample-data-db.sql
mysql $USER $PASS --local-infile sample < load-sample-data.sql

echo
echo "Moving Sample Data to OpenHDS Database"
mysql $USER $PASS sample < migrate-sample-data.sql

