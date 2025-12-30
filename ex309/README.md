## DBaaS vs DIY

Google Cloud SQL (DBaaS) and self-managed Postgres on GKE with PVCs both work for deploying applications on cloud, 
but they differ significantly in setup effort, costs, and maintenance.

### Initial Setup Effort
Cloud SQL wins easily. We can create a Cloud SQL instance via GCP console or gcloud CLI in few minutes.
We only need to update the database url in the app and deploy. No complex Kubernetes YAMLs are needed for the database setup.

On the other hands, self-managed Postgres requires more work. We need to write Deployment/StatefulSet YAMLs for Postgres, 
configure PVCs for storage, set up ConfigMaps/Secrets for credentials, and create Services/Networks database setup.
This takes lot of time to setup and required knowledge on database from the user.

### Ongoing Costs
When we consider the costs for the two solutions, as per the discussions on internet, setting up our own database 
setup is cheaper. But costs may add up with backups, high availability settings etc.

### Maintenance Burden
Almost zero in Cloud SQL because it is managed by Google on behalf of us. We just monitor via Cloud Monitoring and 
rotate credentials occasionally. But self-managed needs ongoing work. We need to manage Postgres upgrades, 
monitor disk space, handle pod restarts, debug PVC issues, and ensure high availability.

### Backup and Recovery
Cloud SQL is very simple. It has features for automated daily backups and recovery. We can restore easily if any 
issues occur. But self-managed requires more effort for these features. We need to use pg_dump cronjobs to get 
database snapshots manually.

### Scalability and Reliability
Cloud SQL is easily scalable. But self-managed scaling is manual and complex. We have to add Postgres replicas, 
configure replication etc.
