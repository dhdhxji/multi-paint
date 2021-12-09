#!/bin/bash

set -e

terraform -chdir=tf apply -auto-approve

echo "$(terraform -chdir=tf output -raw kube_config)" > ./tf/azurek8s.yml

export KUBECONFIG=./tf/azurek8s.yml
kubectl get nodes
kubectl apply -f kubernetes
kubectl get svc