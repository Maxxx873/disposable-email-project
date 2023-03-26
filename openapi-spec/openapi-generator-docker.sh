docker run --rm \
  -v ${PWD}:/api openapitools/openapi-generator-cli generate \
  -i /api/external-api.yaml \
  -g typescript-axios \
  -o ./api/axios-client