## Ex1.8

### How to run
#### Build & run locally
* Execute `./gradle clean build` inside the `ex108` directory. This will build the java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the docker image into the registry by `k3d image import dilansachi/devopswk8s-ex108:1.0.0`.
* Execute `kubectl apply -f manifests/` to deploy the application.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081.
