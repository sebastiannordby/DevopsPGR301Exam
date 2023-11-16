# DevopsPGR301Exam

# Oppgave 1

## Oppgave 1 - A 

[![Publish Python AWS SAM](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_sam_python.yml/badge.svg)](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_sam_python.yml)

Følgende secrets må være konfigurert for å kjøre workflow:
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)

Gjort om hardkodet BUCKET_NAME variabel til å hente ifra miljøvariabler:

```
******
# Hent miljøvariabel <BUCKET_NAME>
try:
    BUCKET_NAME = os.environ['BUCKET_NAME']
except KeyError:
    raise ValueError("The environment variable <BUCKET_NAME> must be provided.")
****** 
```

Gjort om så SAM kan bruke S3 bøtten som er beskrevet i "template.yml":
```
******
Environment:
    Variables:
        BUCKET_NAME: !Ref ImageS3Bucket
******      
```

Fikk publisert til Sam, men fikk en "Internal Server Error", gikk så til CloudWatch for å se om jeg fant noen logg, og det gjorde jeg.. Jeg glemte jo å gi tilgang til S3 bøtten med bilder.
Så jeg la til det i policies for funksjonen :
```
******      
Policies:
    - AmazonRekognitionFullAccess 
    - S3ReadPolicy: 
        BucketName: !Ref ImageS3Bucket
******      
```

Fikk fortsatt "Internal Server Error", men prøvde så å teste Gatway'en via AWS Gateway API og fant følgende feilmelding:
"Wed Nov 15 14:05:05 UTC 2023 : Endpoint response body before transformations: {"errorMessage":"2023-11-15T14:05:05.017Z 30400a8b-8eab-47e5-a4f4-22473f201533 Task timed out after 3.01 seconds"}"

Prøver da følgende i "template.yml"
```
HelloWorldFunction:
    Type: AWS::Serverless::Function 
    Properties:
        Timeout: 60 # Timeout for function
```

## Oppgave 1 - A - Resultat:
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/17a20dd2-42dc-4771-b226-3ec510960d91)

## Oppgave 1 - B
Docker-filen jeg skrev later til å fungere og begge kommandoer oppgitt i oppgaven kjører, som vist i resultat.
Slik ser Docker-filen ut:

```
FROM python:3.9-slim

# Sett arbeidskatalogen i containeren
WORKDIR /app

# Kopier filene som kreves for å installere avhengighetene
COPY requirements.txt ./

# Installer eventuelle nødvendige pakker spesifisert i requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Kopier resten av applikasjonens kildekode til arbeidskatalogen i containeren
COPY . .

CMD ["python", "./app.py"]
```

## Oppgave 1 - B - Resultat
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/8352b4d9-ba36-476b-9f10-0fac3805b887)

# Oppgave 2
[![Build and Push to AWS ECR](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_deploy_ecr.yml/badge.svg)](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_deploy_ecr.yml)

## Oppgave 2 - A
Dockerfile'n later til å fungere og begge kommandoer oppgitt i oppgaven kjører, som vist i resultat.
Slik ser Docker-filen ut:
```
# Stage 1: Bygg
# Bruker Maven-bilde for å bygge applikasjonen
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app

# Kopier Maven-konfigurasjonsfiler
COPY pom.xml .

# Last ned alle avhengigheter
RUN mvn dependency:go-offline

# Kopier kildekoden til bildet
COPY src /app/src

# Bygg applikasjonen
RUN mvn package -DskipTests

# Stage 2: Kjøring
FROM openjdk:11-jre-slim
WORKDIR /app

# Kopier den bygde applikasjonen fra Stage 1
COPY --from=build /app/target/*.jar app.jar

# Sett standard kommando for containeren
CMD ["java", "-jar", "app.jar"]
```

##Oppgave 2 - A - Resultat
Kjøring av "docker build -t ppe .":

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/b857fa6e-9e8e-42f4-a105-ca806339d809)

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/13bb2811-3069-4108-9e4c-8540702d765d)

Kjøring av "docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket ppe":

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/24848cef-807f-4c2a-9745-ed74e2df7421)

Kjøring av "curl localhost:8080/scan-ppe?bucketName=kjellsimagebucket":
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/e59893f2-9afa-42c8-b88a-8a0abeda427d)

## Oppgave 2 - B

For bygging i Github Actions trengs følgende secrets:
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)

Det er også forhånds laget et Elastic Container Repository(ECR) med navn:
[kandidat2033ecr](https://eu-west-1.console.aws.amazon.com/ecr/repositories/private/244530008913/kandidat2033ecr?region=eu-west-1)

##Oppgave 2 - B - Resultat
[Commit'en har id d14dcab544268b3192fbc74487474159d8d98691](https://github.com/sebastiannordby/DevopsPGR301Exam/commit/d14dcab544268b3192fbc74487474159d8d98691).

Kjøring av workflow:
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/830d96eb-00b7-4920-9546-4ce26db11da3)

Publisert til ECR og tagget med "latest" og hash for commit:
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/6f5ef3fb-64a1-4f02-9196-aec3ffb77efb)

# Oppgave 3
For bygging i Github Actions trengs følgende secrets:
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)

## Oppgave 3 - A
Endte opp med å gjøre om fire hardkodet felter til variabler:

```
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
```

Redusert CPU til 256 og Minne til 1024:

```
*****
instance_configuration {
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    cpu = 256
    memory = 1024
}
*****
```

## Oppgave 3 - B:

Lagt til en ny jobb i workflow "aws_deploy_ecr.yml":

```
*****
terraform-deploy:
    needs: [build-and-push-ecr] 
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v2
    
      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v1
    
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
            aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
            aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
            aws-region: eu-west-1
    
      - name: Terraform Init
        working-directory: ./infra
        run: terraform init
    
      - name: Terraform Apply
        working-directory: ./infra
        run: terraform apply -auto-approve -input=false
        env:
          TF_LOG: DEBUG
          TF_VAR_iam_policy_name: kandidat2033polly
          TF_VAR_ecr_repository_uri: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/seno005-private
          TF_VAR_apprunner_container_port: 8080
          TF_VAR_apprunner_service_name: kandidat2033apprunr
          TF_VAR_apprunner_policy_name: kandidat2033apprunpolly
*****
```

På siste steg "Terraform Apply" kan diverse variabler endres etter ditt ønske
- TF_VAR_iam_policy_name (Navn på Policy som blir opprettet)
- TF_VAR_apprunner_container_port (Hvem port AppRunner skal kjøre på)
- TF_VAR_apprunner_service_name (Hva AppRunner instansen skal hete)
- TF_VAR_apprunner_policy_name (Hva AppRunner policien skal hete)

```
env:
  TF_LOG: DEBUG
  TF_VAR_iam_policy_name: kandidat2033polly
  TF_VAR_ecr_repository_uri: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/seno005-private
  TF_VAR_apprunner_container_port: 8080
  TF_VAR_apprunner_service_name: kandidat2033apprunr
  TF_VAR_apprunner_policy_name: kandidat2033apprunpolly
```

Litt motstand med å sette opp(fordi kloke meg kopierte uri til feil ECR), men det ordnet seg:
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/076688de-864d-4023-9f7b-16f782edc36c)

Etter deploy dukket instansen opp i AppRunner:
![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/ca6aec84-7ccc-411f-b31f-9bdca65f8984)

Policien blir også opprettet:

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/ba4e2cb8-5a97-43ac-9506-7d1b506cc692)

Endte opp meg å legge til S3 Full Access med Terraform, dette er vel strengt talt ikke nødvendig ettersom 
du(Glenn) har laget en rolle(AppRunnerECRAccessRole) som implisitt allerede gjør dette:

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/3a2cae7c-78f4-4999-aba8-cf4acc958e8a)


## Oppgave 3 - Resultat:
Kjører en "curl" kommando mot AppRunner fra Cloud9-miljøet og resultatet blir(drumroll): 

![image](https://github.com/sebastiannordby/DevopsPGR301Exam/assets/24465003/2d3bbc22-a6f1-4975-8420-8cf2b957c96a)
