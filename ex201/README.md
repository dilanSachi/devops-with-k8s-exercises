## Ex2.01

### How to run
#### Build & run locally
* Execute `./gradle clean build` inside the `ex201/log_reader` directory. This will build the log reader java project, create a jar and then will create a docker image with the jar file encapsulated.
* Execute `./gradle clean build` inside the `ex201/ping_pong_application` directory. This will build the ping pong application java project, create a jar and then will create a docker image with the jar file encapsulated.
* Execute `./gradle clean build` inside the `ex111/log_writer` directory. This will build the log writer java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the 2 docker images into the registry by running `k3d image import dilansachi/devopswk8s-ex201-log-reader:1.0.0`, `k3d image import dilansachi/devopswk8s-ex201-log-writer:1.0.0` and `k3d image import dilansachi/devopswk8s-ex201-ping-pong-application:1.0.0`.
* Execute `kubectl apply -f manifests/` in the `ex201` directory to deploy the pods with the 3 containers.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081 and http://localhost:8081/pingpong.
