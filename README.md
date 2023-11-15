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

