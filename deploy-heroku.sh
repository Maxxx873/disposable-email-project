#!/bin/bash

# Ensure, that docker-compose stopped
docker-compose stop

# Build new images
docker build ./rest-app-openapi -t getting-started

# Start new deployment
cd rest-app-openapi
heroku container:login
heroku container:push web --app dsp-mail-rest-app-openapi
heroku container:release web --app dsp-mail-rest-app-openapi