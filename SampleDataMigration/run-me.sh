# create a sample database and dump CSV data into a table

echo
echo "Initializing OpenHDS Database"
mysql --user=root -pmotech4 openhds < init-openhds-db.sql
mysql --user=root -pmotech4 openhds < openhds-schema.sql
mysql --user=root -pmotech4 openhds < openhds-required-data.sql
mysql --user=root -pmotech4 openhds < openhds-baseline-stub.sql

echo
echo "Loading Sample Data From CSV file"
mysql --user=root -pmotech4 < init-sample-data-db.sql
mysql --user=root -pmotech4 --local-infile sample < load-sample-data.sql

echo
echo "Moving Sample Data to OpenHDS Database"
#mysql --user=root -pmotech4 sample < migrate-sample-data.sql

