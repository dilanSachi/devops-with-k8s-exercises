## Ex2.07

### How to run
#### Build & run locally
* Execute `./build.sh` inside the `ex207` directory. This will build the pingpong application and log reader java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex207` directory to deploy the pod with the 2 containers.
#### Run using image in DockerHub
* Create a namespace `exercises` by `kubectl create namespace exercises`.
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081 and http://localhost:8081/pingpong.
