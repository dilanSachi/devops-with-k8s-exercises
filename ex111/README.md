## Ex1.11

### How to run
For the PersistentVolume to work we need to create the local path in the node we are binding it to. Since this cluster runs via Docker, create a directory at `/tmp/kube` in the container `k3d-k3s-default-agent-0`. This can simply be done via `docker exec k3d-k3s-default-agent-0 mkdir -p /tmp/kube`.
#### Build & run locally
* Execute `./gradle clean build` inside the `ex111/log_reader` directory. This will build the log reader java project, create a jar and then will create a docker image with the jar file encapsulated.
* Execute `./gradle clean build` inside the `ex111/ping_pong_application` directory. This will build the ping pong java project, create a jar and then will create a docker image with the jar file encapsulated.
* * Execute `./gradle clean build` inside the `ex111/log_writer` directory. This will build the log writer java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the 3 docker images into the registry by running `k3d image import dilansachi/devopswk8s-ex111-log-reader:1.0.0`, `k3d image import dilansachi/devopswk8s-ex111-log-writer:1.0.0` and `k3d image import dilansachi/devopswk8s-ex111-ping-pong-application:1.0.0`.
* Execute `kubectl apply -f manifests/` in the `ex111` directory to deploy the pods with the 3 containers.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081 and http://localhost:8081/pingpong.
