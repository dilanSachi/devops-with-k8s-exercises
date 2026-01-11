## Ex4.09

### How to ru
* Create a namespace `project` by `kubectl create namespace project`.
* You need to have NATS installed in the cluster before running the application.
```
helm repo add nats https://nats-io.github.io/k8s/helm/charts/
helm repo update
helm install my-nats nats/nats
```
* Create GKE secret in the cluster.
```
kubectl create secret generic ex409-gcloud-secret --from-literal=GKE_SA_KEY='add-gke-secret-here' -n project
```
* Create a server in discord and get the webhook url. Store the url as a secret in the cluster.
```
kubectl create secret generic ex409-discord-webhook-secret --from-literal=DISCORD_WEBHOOK_URL='add-webhook-url-here' -n project
```
#### Build & run locally
* Execute `./build.sh` inside the `ex409` directory. This will build the todo app, broadcaster app and todo backend java projects, 
create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex409` directory to deploy the pods.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.
#### Deployment pipeline
* Whenever a change is pushed `ex409/` directory, the GitHub actions will catch it and will push the images to docker hub.
* If you are using ArgoCD, you need to create the app with directory as `ex409` in order catch the changes.

---

* Server would be available in http://EXTERNAL-IP.
* `EXTERNAL-IP` can be found from `kubectl get gateway ex409-gateway`
