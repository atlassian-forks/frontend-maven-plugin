# Developing

## Running tests locally

### Run all invoker tests

`mvn verify -PintegrationTests`

### Run single invoker test

`mvn verify -PintegrationTests -Dinvoker.test=<test-pattern>`