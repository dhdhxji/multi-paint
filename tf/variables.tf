variable "client_id" {}
variable "client_secret" {}

variable "agent_count" {
    default = 2
}

variable "ssh_public_key" {
    default = "~/.ssh/id_rsa.pub"
}

variable "dns_prefix" {
    default = "multi-paint"
}

variable cluster_name {
    default = "multi-paint_k8s"
}

variable resource_group_name {
    default = "multi-paint_rg"
}

variable location {
    default = "westeurope"
}