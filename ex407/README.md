## Ex4.07

### How to run
#### Build & run locally
* Execute `./build.sh` inside the `ex407` directory. This will build the pingpong application, log writer and log reader 
java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* It will also push the images to docker hub.
* Create a namespace `exercises` by `kubectl create namespace exercises`.
* If you are using ArgoCD, you need to create the app with directory as `ex407` in order catch the changes.
* Otherwise, execute `kubectl apply -k .` in the `ex407` directory to deploy the pod with the 2 containers.
#### Run using image in DockerHub
* Create a namespace `exercises` by `kubectl create namespace exercises`.
* Execute `kubectl apply -k .` to deploy the application.

---

* Server would be available in http://EXTERNAL-IP and http://EXTERNAL-IP/pingpong.
* `EXTERNAL-IP` can be found from `kubectl get gateway ex407-gateway`
