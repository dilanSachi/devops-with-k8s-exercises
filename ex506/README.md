## Ex5.06

* Ensure to have Knative Serving component installed in your k3d cluster.

### How to run
#### Build & run locally
* Execute `kubectl apply -f manifests/hello.yaml` in the `ex506` directory to deploy the serverless app.
* Invoke the app by,
```aiignore
curl -H "Host: URL_HERE" http://localhost:8081
```
You can get the `URL_HERE` by,
```
  kubectl get ksvc
```
Outputs,
![Service-Scaling-Up-Down-Screenshot](/ex506/Service-Scaling-Up-Down-Screenshot.png)
---
* Next execute `kubectl apply -f manifests/hello-updated.yaml` to split traffic with the new app.
* Invoke the app multiple times to see outputs from both 2 revisions.

![Traffic-Split-Deployment-Screenshot](/ex506/Traffic-Split-Deployment-Screenshot.png)
![Traffic-Split-Output-Screenshot](/ex506/Traffic-Split-Output-Screenshot.png)
