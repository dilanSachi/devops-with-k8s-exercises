## Ex5.03

### How to run
* Note that you need to have istio service mesh installed in your cluster before running. Activate ambient mode for the namespace as well.
```
kubectl label namespace exercises istio.io/dataplane-mode=ambient
```
* Ensure to enforce Layer 7 authorization policy.
```istioctl waypoint apply --enroll-namespace --wait```

#### Build & run locally
* Execute `./build.sh` inside the `ex503` directory. This will build the pingpong application, log writer, greeter app and log reader 
java projects, create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* It will also push the images to docker hub.
* Create a namespace `exercises` by `kubectl create namespace exercises`.
* Execute `kubectl apply -k .` in the `ex503` directory to deploy the pod with the 2 containers.
#### Run using image in DockerHub
* Create a namespace `exercises` by `kubectl create namespace exercises`.
* If you are using ArgoCD, you need to create the app with directory as `ex503` in order catch the changes.
* Otherwise, execute `kubectl apply -k .` to deploy the application.

---

* Port forward the gateway to view locally.
```
kubectl port-forward svc/ex503-gateway-istio 8081:80
```
* Visit `http://localhost:8081` and `http://localhost:8081/pingpong`

![Kiali](/ex503/Kiali.png)
