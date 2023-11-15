# DevopsPGR301Exam

# Oppgave 1

## Oppgave 1 - A

Følgende secrets må være konfigurert for å kjøre workflow:
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)
- BUCKET_NAME (Navn på eksisterende bøtte)

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

Gjort om så SAM kan bruke S3 bøtten som er beskrevet i "template-yml":
```
******
Environment:
    Variables:
        BUCKET_NAME: !Ref ImageS3Bucket
******      
```