## Ex1.10

### How to run
#### Build & run locally
* Execute `./gradle clean build` inside the `ex110/log_reader` directory. This will build the log reader java project, create a jar and then will create a docker image with the jar file encapsulated.
* Execute `./gradle clean build` inside the `ex110/log_writer` directory. This will build the log writer java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the 2 docker images into the registry by running `k3d image import dilansachi/devopswk8s-ex110-log-reader:1.0.0` and `k3d image import dilansachi/devopswk8s-ex110-log-writer:1.0.0`.
* Execute `kubectl apply -f manifests/` in the `ex110` directory to deploy the pod with the 2 containers.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081.
