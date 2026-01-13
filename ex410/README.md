## Ex4.10

### How to run
* Create a namespace `project-staging` by `kubectl create namespace project-staging`.
* Install argo CRDs,
```
kubectl create namespace argo-rollouts
kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
```
* You need to have NATS installed in the cluster before running the application.
```
helm repo add nats https://nats-io.github.io/k8s/helm/charts/
helm repo update
helm install my-nats nats/nats
```
* Create GKE secret in the cluster.
```
kubectl create secret generic ex410-gcloud-secret --from-literal=GKE_SA_KEY='add-gke-secret-here' -n project-staging
```
* Create a server in discord and create a webhook and get the webhook url. Store the url as a secret in the cluster.
```
kubectl create secret generic ex410-discord-webhook-secret --from-literal=DISCORD_WEBHOOK_URL='add-webhook-url-here' -n project-staging
```
#### Build & run locally
* Execute `./build.sh` inside the `ex410` directory. This will build the todo app, broadcaster app and todo backend java projects, 
create jars and then will create docker images with the jar files encapsulated.
* If you are using k3d, it will import the docker images into the registry as well.
* Execute `kubectl apply -f manifests/` in the `ex410` directory to deploy the pods.
#### Run using image in DockerHub
* Execute `kubectl apply -f manifests/` to deploy the application.
#### Deployment pipeline
* Whenever a change is pushed `ex410/` directory, the GitHub actions will catch it and will push the images to docker 
hub and update the image versions in the `kustomization.yaml` files in `https://github.com/dilanSachi/devops-with-k8s-configuration`.
* If you are deploying with ArgoCD, apply the production and staging application configurations to create apps in ArgoCD which will automatically deploy the latest versions taken from the `kustomization.yaml` files.

---

* When you are ready to proceed to production deployment, repeat the above steps for `project-prod` namespace and create a tag.
* Server would be available in http://EXTERNAL-IP.
* `EXTERNAL-IP` can be found from `kubectl get gateway ex410-gateway`
