## Ex2.08

### How to run
For the PersistentVolume to work we need to create the local path in the node we are binding it to. Since this cluster runs via Docker, create a directory at `/tmp/kube` in the container `k3d-k3s-default-agent-0`. This can simply be done via `docker exec k3d-k3s-default-agent-0 mkdir -p /tmp/kube`.\
Create a namespace `project` by `kubectl create namespace project`.
#### Build & run locally
* Execute `./build.sh` inside the `ex208` directory. This will build the todo app and todo backend java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex208` directory to deploy the pod.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081.
