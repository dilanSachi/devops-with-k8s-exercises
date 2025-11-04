## Ex1.1

### How to run
#### Build & run locally using k3d
1. Execute `./gradle clean build` inside the `ex101` directory. This will build the java project, create a jar and then will create a docker image with the jar file encapsulated.
2. If you are using k3d, import the docker image into the registry by `k3d image import dilansachi/devopswk8s-ex102:1.0.0`
3. Execute `kubectl create deployment ex104-web-server --image=dilansachi/devopswk8s-ex102:1.0.0` to deploy the application.
#### Run using image in DockerHub
1. Execute `kubectl create deployment ex104-web-server --image=dilansachi/devopswk8s-ex102:1.0.0` to deploy the application.
