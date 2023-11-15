terraform {
  backend "s3" {
    bucket = "seno005-private"
    key = "terraform/state"
    region = "eu-west-1"
  }
}
