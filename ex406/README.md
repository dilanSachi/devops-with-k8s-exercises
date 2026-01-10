## Ex4.06

### How to run
Create a namespace `project` by `kubectl create namespace project`.
#### Build & run locally
* Execute `./build.sh` inside the `ex406` directory. This will build the todo app and todo backend java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex406` directory to deploy the pod.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.
#### Deployment pipeline
* Whenever a change is pushed `ex406/` directory, the GitHub actions will catch it and will trigger a new deployment build. This will be deployed in `project` namespace if pushed to the `main` branch. Otherwise, the namespace will have the branch-name.

---

* Server would be available in http://EXTERNAL-IP.
* `EXTERNAL-IP` can be found from `kubectl get gateway ex406-gateway`
