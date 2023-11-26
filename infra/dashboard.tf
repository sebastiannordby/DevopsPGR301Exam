resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.dashboard_name
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.cloudwatch_namespace}",
            "scan_ppe.count",
            { "stat": "Sum", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "PPE Scan Count"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 6,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.cloudwatch_namespace}",
            "s3.download.image.size.avg",
            { "stat": "Average", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "s3.download.image.size.sum",
            { "stat": "Sum", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "s3.download.image.size.max",
            { "stat": "Maximum", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "Image Download Metrics"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 12,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.cloudwatch_namespace}",
            "s3.list.images.timer.avg",
            { "stat": "Average", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "s3.list.images.timer.count",
            { "stat": "Sum", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "s3.list.images.timer.max",
            { "stat": "Maximum", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "Image Listing Metrics"
      }
    }
    {
      "type": "metric",
      "x": 0,
      "y": 12,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.cloudwatch_namespace}",
            "analyze.images.timer",
            { "stat": "Average", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "analyze.images.timer",
            { "stat": "Sum", "period": 300 }
          ],
          [
            "${var.cloudwatch_namespace}",
            "analyze.images.timer",
            { "stat": "Maximum", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "Analyzing Images Metrics"
      }
    }
  ]
}
DASHBOARD
}

module "alarm" {
  source = "./alarm_module"
  alarm_email = var.alert_email
  prefix = "scan-ppe-count"
  metric_name = "scan_ppe.count"
  threshold = 5
}