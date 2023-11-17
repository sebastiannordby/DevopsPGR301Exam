variable "threshold" {
  default = "50"
  type = string
}

variable "alarm_email" {
  type = string
}

variable "name_prefix" {
  type = string
}

variable "metric_name" {
  type = string
}

variable "cloudwatch_namespace" {
  type = string
}
