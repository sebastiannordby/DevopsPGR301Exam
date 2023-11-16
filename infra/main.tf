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

        runtime_environment_variables = {
          "MANAGEMENT_METRICS_EXPORT_CLOUDWATCH_NAMESPACE" = var.cloudwatch_namespace
          "MANAGEMENT_METRICS_EXPORT_CLOUDWATCH_BATCHSIZE" = tostring(var.cloudwatch_batch_size)
          "MANAGEMENT_METRICS_EXPORT_CLOUDWATCH_STEP" = var.cloudwatch_step
          "MANAGEMENT_METRICS_EXPORT_CLOUDWATCH_ENABLED" = tostring(var.cloudwatch_enabled)
        }
      }

      image_identifier = var.ecr_repository_uri
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

  statement {
    effect = "Allow"
    actions = [
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability"
    ]
    resources = ["arn:aws:ecr:eu-west-1:244530008913:repository/seno005-private"]
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
