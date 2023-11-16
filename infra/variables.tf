variable "dashboard_name" {
  type = string
}

variable "apprunner_service_name" {
  description = "Name of the AppRunner Service"
  type = string
}

variable "ecr_repository_uri" {
  description = "ECR URI"
  type = string
}

variable "iam_policy_name" {
  description = "IAM Policy Name"
  type = string
}

variable "apprunner_policy_name" {
  description = "AppRunner Instance Policy Name"
  type = string
}

variable "apprunner_container_port" {
  description = "AppRunner Instance Port Number"
  type = number
  default = 8080
}

variable "cloudwatch_namespace" {
  description = "CloudWatch namespace for metrics"
  type        = string
}

variable "cloudwatch_batch_size" {
  description = "Batch size for CloudWatch metrics"
  type        = number
  default     = 20
}

variable "cloudwatch_step" {
  description = "Step size for CloudWatch metrics"
  type        = string
  default     = "1m"
}

variable "cloudwatch_enabled" {
  description = "Enable CloudWatch metrics"
  type        = bool
  default     = true
}