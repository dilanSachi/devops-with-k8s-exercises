## Ex1.5

### How to run
#### Build & run locally
1. Execute `./gradle clean build` inside the `ex105` directory. This will build the java project, create a jar and then will create a docker image with the jar file encapsulated.
2. If you are using k3d, import the docker image into the registry by `k3d image import dilansachi/devopswk8s-ex105:1.0.0`
3. Execute `kubectl apply -f manifests/deployment.yaml` to deploy the application.
4. Use port forward and confirm the web server functionality. `kubectl port-forward ex105-web-server-7878595d4b-bskmm 3002:3001` 
#### Run using image in DockerHub
1. Execute `kubectl apply -f manifests/deployment.yaml` to deploy the application.
2. Use port forward and confirm the web server functionality. `kubectl port-forward ex105-web-server-7878595d4b-bskmm 3002:3001`

** Note that, the manifest has specified the PORT environment to be 3001. If needed, change it to any required value. **
