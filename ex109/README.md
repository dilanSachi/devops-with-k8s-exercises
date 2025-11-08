## Ex1.9

### How to run
#### Build & run locally
* This exercise uses the docker image from ex107 to deploy the log output application (`dilansachi/devopswk8s-ex107:1.0.0`). If it is not present in the registry, either it can be pulled from docker hub. Or else, you need to navigate to `ex107` and rebuild the image and pull into k3d registry.
* Execute `./gradle clean build` inside the `ex109` directory. This will build the ping pong app, create a jar and then will create a docker image with the jar file encapsulated.
* If you are using k3d, import the docker image into the registry by `k3d image import dilansachi/devopswk8s-ex109:1.0.0`.
* Execute `kubectl apply -f manifests/` to deploy the application.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.

---
* The manifests will deploy both ping-pong-app and log-output applications along with the ingress. The ingress would forward the requests `/` to log-output app and requests to `/pingpong` to ping-pong-app.
* Applications would be available in http://localhost:8081 and http://localhost:8081/pingpong.
