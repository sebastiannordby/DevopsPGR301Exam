resource "aws_cloudwatch_metric_alarm" "threshold" {
  alarm_name = "${var.name_prefix}-threshold"
  namespace = var.cloudwatch_namespace
  metric_name = var.metric_name
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold = var.threshold
  evaluation_periods  = "1"
  period = "60"
  statistic = "Maximum"
  alarm_description = "This alarm goes of if treshold exceed in a one minute period."
  alarm_actions = [aws_sns_topic.user_updates.arn]
}

resource "aws_sns_topic" "user_updates" {
  name = "${var.name_prefix}-alarm-topic"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  topic_arn = aws_sns_topic.user_updates.arn
  protocol = "email"
  endpoint = var.alarm_email
}


