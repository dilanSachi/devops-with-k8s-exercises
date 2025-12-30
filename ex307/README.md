## Ex3.07

### How to run
Create a namespace `project` by `kubectl create namespace project`.
#### Build & run locally
* Execute `./build.sh` inside the `ex307` directory. This will build the todo app and todo backend java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex307` directory to deploy the pod.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---

* Server would be available in http://EXTERNAL-IP.
* `EXTERNAL-IP` can be found from `kubectl get gateway ex307-gateway`
