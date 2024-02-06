docker run --rm \
  -v ${PWD}:/api openapitools/openapi-generator-cli generate \
  -i ./api/admin-api.yaml \
  -g typescript-axios \
  -o /api/admin-axios-client
  cp -r admin-axios-client ../admin-ui/src/