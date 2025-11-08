## Ex1.6

### How to run
#### Build & run locally
* Execute `./gradle clean build` inside the `ex106` directory. This will build the java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the docker image into the registry by `k3d image import dilansachi/devopswk8s-ex106:1.0.0`.
* Execute `kubectl apply -f manifests/deployment.yaml` to deploy the application.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/deployment.yaml` to deploy the application.

---

* Execute `kubectl apply -f manifests/service.yaml` to apply the service.
* Server would be available in http://localhost:8082.
