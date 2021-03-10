#!/usr/bin/env bash
set -e

# NB:
#
# Dette er init-scriptet til docker-containeren som kjøres opp for testing
# og lokal kjøring. Ingenting av det som er her kjøres ute i miljøene (DEV/PROD)
# kun under bygg/test/ og lokal kjøring av applikasjonen.
#

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER grensekomp WITH PASSWORD 'grensekomp';
    CREATE DATABASE grensekomp_db;
    CREATE SCHEMA grensekomp;
    GRANT ALL PRIVILEGES ON DATABASE grensekomp_db TO grensekomp;
EOSQL

psql -v ON_ERROR_STOP=1 --username "grensekomp" --dbname "grensekomp_db" <<-EOSQL

EOSQL

