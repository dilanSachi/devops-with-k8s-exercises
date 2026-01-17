## Ex5.01

### How to run
* Run `kubectl apply -k .` in the `ex501` directory.
* This will add the CRDs, controller, roles and service accounts.
* Port forward the dummy website to port 9091 by `kubectl port-forward service/dummysite-wikipediasite 9091:80`.
* Visit `http://localhost:9091` to view the dummy site of `https://en.wikipedia.org/wiki/Kubernetes`.
