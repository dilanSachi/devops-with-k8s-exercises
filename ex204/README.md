## Ex2.04

### How to run
For the PersistentVolume to work we need to create the local path in the node we are binding it to. Since this cluster runs via Docker, create a directory at `/tmp/kube` in the container `k3d-k3s-default-agent-0`. This can simply be done via `docker exec k3d-k3s-default-agent-0 mkdir -p /tmp/kube`.
#### Build & run locally
* Execute `./gradle clean build` inside the `ex204/todo_app` directory. This will build the todo app java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the docker image into the registry by running `k3d image import dilansachi/devopswk8s-ex204-todo-app:1.0.0`.
* Create a namespace `project` by `kubectl create namespace project`.
* Execute `./gradle clean build` inside the `ex204/todo_backend` directory. This will build the todo backend java project, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the docker image into the registry by running `k3d image import dilansachi/devopswk8s-ex204-todo-backend:1.0.0`.
* Execute `kubectl apply -f manifests/` in the `ex204` directory to deploy the pod.
#### Run using image in DockerHub
* Create a namespace `project` by `kubectl create namespace project`.
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://localhost:8081.
