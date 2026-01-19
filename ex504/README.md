## Ex5.04

### How to run
#### Build & run locally
* Execute `kubectl apply -f wiki-serve.yaml` in the `ex504` directory to deploy the pod with the 3 containers.
---

* Port forward the service to view locally.
```
kubectl port-forward svc/ex504-wiki-server-service 8081:80
```
* Visit `http://localhost:8081` and `http://localhost:8081/random-wiki.html` to visit the wikis. The random wiki may 
not be available at the beginning.
