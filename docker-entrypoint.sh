#!/bin/sh

set -e

./wait-for-it.sh db:5432 --timeout=60 --strict -- echo "Database is ready!"

exec "$@"