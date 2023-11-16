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
            "scan_ppe",
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
            "s3.download.image.size",
            { "stat": "Average", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "Average Image Size"
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
            "s3.list.images.timer",
            { "stat": "Average", "period": 300 }
          ]
        ],
        "region": "eu-west-1",
        "title": "Average Time to List Images"
      }
    }
  ]
}
DASHBOARD
}