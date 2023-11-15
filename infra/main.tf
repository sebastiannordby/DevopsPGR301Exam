variable "apprunner_service_name" {
  description = "Name of the AppRunner service"
  type = string
}

variable "ecr_repository" {
  description = "URI to ECR repository"
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
  description = "Container port number"
  type = number
  default = 8080
}

resource "aws_apprunner_service" "service" {
  service_name = var.apprunner_service_name

  instance_configuration {
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    cpu = 256
    memory = 1024
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
    }

    image_repository {
      image_configuration {
        port = var.apprunner_container_port
      }

      image_identifier = "${var.ecr_repository}:latest"
      image_repository_type = "ECR"
    }

    auto_deployments_enabled = true
  }
}

resource "aws_iam_role" "role_for_apprunner_service" {
  name = var.apprunner_policy_name
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}


data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "policy" {
  statement {
    effect = "Allow"
    actions = ["rekognition:*"]
    resources = ["*"]
  }
  
  statement  {
    effect = "Allow"
    actions = ["s3:*"]
    resources = ["*"]
  }

  statement  {
    effect = "Allow"
    actions = ["cloudwatch:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name = var.iam_policy_name
  description = "Policy for AppRunner Instance"
  policy = data.aws_iam_policy_document.policy.json
}

resource "aws_iam_role_policy_attachment" "attachment" {
  role = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}
