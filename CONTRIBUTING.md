# Developing

## Running tests locally

### Run all invoker tests

`mvn verify -PintegrationTests`

### Run single invoker test

`mvn verify -PintegrationTests -Dinvoker.test=<test-pattern>`

## Docker images with node version managers

### Build docker image

`docker build -f docker/<node-version-manager>/Dockerfile -t fmp-local/base-with-<node-version-manager> ./docker`

### Run project with docker image

e.g. `docker run --rm -it -v $(pwd):/source -v ~/.m2:/root/.m2 fmp-local/base-with-<node-version-manager>` will mount project into /source directory