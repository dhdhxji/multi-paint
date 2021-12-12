#!/bin/bash

set -e

az login &> /dev/null

terraform -chdir=tf apply -auto-approve
echo "$(terraform -chdir=tf output -raw kube_config)" > ./tf/azurek8s.yml

export KUBECONFIG=./tf/azurek8s.yml

kubectl apply -f kubernetes

external_ip=""
while [ -z $external_ip ]; do
    sleep 10
    external_ip=$(kubectl get svc multi-paint --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}")
done

echo "Multi-paint address: $external_ip"