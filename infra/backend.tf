terraform {
  backend "s3" {
    bucket = "kandidat2033b"
    key = "terraform/state"
    region = "eu-west-1"
  }
}
