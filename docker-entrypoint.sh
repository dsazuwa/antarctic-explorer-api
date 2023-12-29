#!/bin/sh

set -e

./wait-for-it.sh db:5432 --timeout=60 --strict -- echo "Database is ready!"
./wait-for-it.sh elasticsearch:9200 --timeout=180 --strict -- echo "Elasticsearch is ready!"

exec "$@"