-- V1__alter_date_to_datetime.sql

ALTER TABLE deposits MODIFY COLUMN date DATETIME;
UPDATE deposits SET date = CONCAT(date, ' 00:00:00') WHERE date IS NOT NULL;