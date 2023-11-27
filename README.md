# DevopsPGR301Exam - Kandidat 2033

[![Publish Python AWS SAM](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_sam_python.yml/badge.svg)](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_sam_python.yml)
[![Build and Push to AWS ECR](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_deploy_ecr.yml/badge.svg)](https://github.com/sebastiannordby/DevopsPGR301Exam/actions/workflows/aws_deploy_ecr.yml)

## Konfigurering av Github
**For alle oppgavene i dette prosjektet må følgende secrets legges inn i Github:**
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)

## Konfigurering av Java applikasjonen
**Resten av konfigureringen i forhold til applikasjonen for Java applikasjonen kan gjøres i "aws_deploy_ecr.yml",
under steget "Terraform Apply":**
```
******
TF_VAR_iam_policy_name: kandidat2033polly
TF_VAR_ecr_repository_uri: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/kandidat2033ecr:latest
TF_VAR_apprunner_container_port: 8080
TF_VAR_apprunner_service_name: kandidat2033apprunr
TF_VAR_apprunner_policy_name: kandidat2033apprunpolly
TF_VAR_dashboard_name: kandidat2033dashboard
TF_VAR_cloudwatch_namespace: Kandidat2033Metrics
TF_VAR_cloudwatch_batchSize: 20
TF_VAR_cloudwatch_step: 5s
TF_VAR_cloudwatch_enabled: true
TF_VAR_alert_email: sebastianbjornstad@hotmail.com
******
```


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
![image](img/curl_resultat_sam.png)

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
![image](img/docker_run_resultat.png)

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

![image](img/oppgave_2_a_1.png)

![image](img/oppgave_2_a_2.png)

Kjøring av "docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket ppe":

![image](img/oppgave_2_a_3.png)

Kjøring av "curl localhost:8080/scan-ppe?bucketName=kjellsimagebucket":
![image](img/oppgave_2_a_4.png)

## Oppgave 2 - B

For bygging i Github Actions trengs følgende secrets:
- AWS_ACCESS_KEY_ID (Lages i IAM)
- AWS_SECRET_ACCESS_KEY (Lages i IAM)

Det er også forhånds laget et Elastic Container Repository(ECR) med navn:
[kandidat2033ecr](https://eu-west-1.console.aws.amazon.com/ecr/repositories/private/244530008913/kandidat2033ecr?region=eu-west-1)

## Oppgave 2 - B - Resultat
[Commit'en har id d14dcab544268b3192fbc74487474159d8d98691](https://github.com/sebastiannordby/DevopsPGR301Exam/commit/d14dcab544268b3192fbc74487474159d8d98691).

Kjøring av workflow:
![image](img/oppgave_2_b_1.png)

Publisert til ECR og tagget med "latest" og hash for commit:
![image](img/oppgave_2_b_1.png)

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

Etter deploy dukket instansen opp i AppRunner:
![image](img/oppgave_3_b_2.png)

Policien blir også opprettet:

![image](img/oppgave_3_b_3.png)

Endte opp meg å legge til S3 Full Access med Terraform, dette er vel strengt talt ikke nødvendig ettersom 
du(Glenn) har laget en rolle(AppRunnerECRAccessRole) som implisitt allerede gjør dette:

![image](img/oppgave_3_b_4.png)


## Oppgave 3 - Resultat:
Kjører en "curl" kommando mot AppRunner fra Cloud9-miljøet og resultatet blir(drumroll): 

![image](img/oppgave_3_resultat.png)

# Oppgave 4 A
For å gjøre oppsett av applikasjonen enklere, flyttet jeg Terraform variablene til egen fil ["variables.tf"](https://github.com/sebastiannordby/DevopsPGR301Exam/blob/main/infra/variables.tf).
Jeg ville også ha mulighet til å konfigurere metrikkene via Github Actions som så sendte videre til Terraform.

Ved en rask titt på byggene og commit historikken så ble det fort trøbbel med dette eksperimentet, men jeg ville ikke gi meg. 
Jeg skulle få til dette med miljøvariabler.

Fant jo til slutt ut av koden har fungert flere ganger, men Terraform kan ikke oppdatere miljøvariabler etter opprettelse.
Dermed måtte jeg legge inn en lifecycle på applikasjonen som gjør at denne slettes hver gang og opprettes på nytt:
```
*****
lifecycle {
    create_before_destroy = true
}
*****
```

De nye variablene er følgende:
```
*****
variable "cloudwatch_namespace" {
  description = "CloudWatch namespace for metrics"
  type = string
}

variable "cloudwatch_batch_size" {
  description = "Batch size for CloudWatch metrics"
  type = string
  default = "20"
}

variable "cloudwatch_step" {
  description = "Step size for CloudWatch metrics"
  type = string
  default = "1m"
}

variable "cloudwatch_enabled" {
  description = "Enable CloudWatch metrics"
  type = string
  default = "true"
}
```

Alle variablene kan og må da settes i steget "Terraform Apply":
```
*****
- name: Terraform Apply
    working-directory: ./infra
    run: terraform apply -auto-approve -input=false
    env:
      TF_VAR_iam_policy_name: kandidat2033polly
      TF_VAR_ecr_repository_uri: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/kandidat2033ecr:latest
      TF_VAR_apprunner_container_port: 8080
      TF_VAR_apprunner_service_name: kandidat2033apprunr
      TF_VAR_apprunner_policy_name: kandidat2033apprunpolly
      TF_VAR_dashboard_name: kandidat2033dashboard
      TF_VAR_cloudwatch_namespace: Kandidat2033Metrics
      TF_VAR_cloudwatch_batchSize: 20
      TF_VAR_cloudwatch_step: 5s
      TF_VAR_cloudwatch_enabled: true
*****
```

# Oppgave 4 - A - Resultat

Introdusert tre nye endepunkter:

listImages: Dette endepunktet tar inn navnet på en S3-bøtte som parameter og returnerer en liste over innholdet i den angitte bøtten. Den bruker Amazon S3-tjenesten til å liste opp objekter i bøtten og returnerer en liste med nøklene til objektene. Her valgte jeg Timer for samme grunn som "Endepunkt for å analysere bilder med kjennetegn"-

![image](img/oppgave_4_a_1.png)

downloadImage: Dette endepunktet tar inn navnet på en S3-bøtte og navnet på en bildefil i bøtten som parametere. Det bruker Amazon S3-tjenesten til å laste ned bildet fra bøtten og returnerer bildedataen som en HTTP-respons med riktig MIME-type. Her valgte jeg DistributionSummary som metrikk. Jeg mener dette er en god metrikk for det å laste ned filer fordi man kan få 
diverse statistikker som gjennomsnitt, maksimum, minimum av filstørrelsene. Ved å se på disse verdiene vet man da om man burde effektivisere koden, 
kanskje streame over http i steden for å lese fra S3 og rett til minne, men også oppdage flaskehalser i forhold til filstørrelsene i forhold til ytelse på applikasjonen.

![image](img/oppgave_4_a_2.png)

analyzeImagesFromBucket: Dette endepunktet tar inn navnet på en S3-bøtte som parameter og analyserer bildene i bøtten ved å bruke Amazon Rekognition-tjenesten til å oppdage og returnere etiketter som beskriver objekter i hvert bilde. Her valgte jeg Timer som metrikk rett og slett for å overvåke ytelse. Her kan man da sette opp varslinger hvis endepunktet skulle bruke 
for lang tid på å eksekvere. En til fordel er at man kan se når hastighetsforskjeller i forhold til mengden brukere(hvis du har dette som metrikk),
men også generelt for å ha statestikk på ytelse av tredjeparts tjenester.

![image](img/res_endepunkt_reko_2.png)

Lagt til metrikk på eksisterende endepunkt:

Endepunkt for å gjøre PPE Scan:
Her valgte jeg Counter som metrikk. Dette for å få statistikk på hvor mange ganger denne funksjonen kjører. Statistikken kan brukes til å vurdere optimaliseringer.
Kanskje endepunktet blir kjørt mange ganger og man skulle ha cashet resultatene innenfor en viss periode, eller lignende.

Et fungerende dashboard med navn "kandidat2033dashboard":

![image](img/oppgave_4_a_resultat.png)


# Oppave 4 - B
Definert en alarm under infra/alarm_module. 
Alarmen skal gå av hvis scan-ppe blir kalt mer enn 5 ganger innen en tidsperiode på 1 minutt.

main.tf:

```
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
```

Med følgende variabler "variables.tf":
```
variable "threshold" {
  default = "50"
  type = string
}

variable "alarm_email" {
  type = string
}

variable "name_prefix" {
  type = string
  default = "2033alarm"
}

variable "metric_name" {
  type = string
}

variable "cloudwatch_namespace" {
  type = string
}
```
Henter inn modulen i dashboardet "dashboard.tf":
```
module "alarm" {
  source = "./alarm_module"
  alarm_email = var.alert_email
  name_prefix = "scan-ppe-count-2033"
  metric_name = "scan_ppe.count"
  threshold = 5
  cloudwatch_namespace = var.cloudwatch_namespace
}
```

## Oppgave 4 - B - Resultat
Alarmen kommer opp i CloudWatch:
![image](img/oppgave_4_resultat_b_1.png)

Mottar e-post for abonnering på alarm:
![image](img/oppgave_4_resultat_b_2.png)

# Oppgave 5

## Oppgave 5 - A

**En definisjon av kontinuerlig integrasjon.**

Kontinuerlig integrasjon, eller CI, er en slags praksis for utvikling der oss utviklere lager og vedlikeholder i et versjonshåndteringssystem.
Versjonhåndteringsystemet blir for det meste kalt for et repository, og standarden er vel per i dag GIT, selvom det er andre systemer som kan brukes. 
Ettersom jeg har drevet med utvikling en liten stund, brukte jeg faktisk TFVC(team foundation version control) i en del år. Uansett, når koden kommer inn til versjonshåntering systemet
blir det triggret et automatisk bygg og eventuel intergasjonstesting.

**Fordelene med å bruke CI i et utviklingsprosjekt - hvordan CI kan forbedre kodekvaliteten og effektivisere utviklingsprosessen.**

*Fordeler med bygg*
Fordelene her er da at man får versifisert at koden bygger på en annen maskin en selve utvikleren sin,
og unngår den klassiske "it works on my machine". Dette kan klassifiseres som "tidlig feilopptagelse".

*Forbedret kodekvalitet og raskere feedback*
Man kan oppnå forbedret kode kvalitet via å kjøre automatiserte tester, men også diverse analysering av koden. 
Dette kan igjen brukes i feedback-loop for å sikre bedre kvalitet på software som lages.

**Hvordan jobber man med CI i Github rent i praksis?**
Først og fremst har man et repository. Dette er da stedet der kildekoden lagres.

Deretter setter man som regel en del standarder for at ting skal gå smidig:

*Kodestandard*
Ofte settes det i kodestandard som legges inn i en form for dokumentasjonsverktøy. Denne standarden kan inneholde alt fra filnavngivning til plassering på kodeblokker.

*Dokumentasjon*
Ikke alle skriver dokumentasjon, dette grunnet at ofte blir dokumentasjonen ikke vedlikeholdt i ettertid, noe som gjør den helt ubrukelig.
Men i enkelte tilfeller kreves det at prosjektet har en dokumentasjon som er kontinuerlig oppdatert.

*Flow for innsjekking*
Det er diverse flows for hvordan man kan sjekke inn kode. Dette går på om man skal branche ut eller ikke, hvordan man skal branche ut, eller om man bare rett og slett skal pushe rett til main.
Denne standarden er viktig å sette ettersom det kan føre til en "bomba" kodebase hvis ikke. 

*Automatisering av bygg og tester*
I Github bruker man da Github Actions for å automatisere bygg og eventuelle tester. Prosessen her har da en eller flere triggere, som regel basert på hva slags
flow teamet velger.

## Oppgave 5 - B

**Scrum/Smidig Metodikk**

Scrum er veldig populært for programvareutvikling, men det er opprinnelig et rammeverk for produktutvikling, der programvareutvikling faller under dette. Scrum er basert på korte iterasjoner/sykluser som er kalt for sprints. En sprint varer som regel i 1-4 uker. Formålet med en sprint er å levere et fungerende produkt, ved å sørge for å fullføre de delene som er planlagt for denne sprinten.

I forhold til programvareutvikling er scrum basert på en håndfull prinsipper:
- Fokus på kunden: Produktets behov og prioritering av funksjoner som skal utvikles i forhold til dette
- Iterativ utvikling: Det skal gå fremover i små inkrementelle trinn(sprints)
- Fokus og engasjement: Teamet er selv ansvarlig for planlegging, gjennomføring og evaluering av arbeidet

For en student kan det jo virke som at dette med scrum og smidig er en standard alle bruker, men i virkeligheten er ikke dette sant. Mange henger faktisk etter grunnet at man er litt "grodd fast" i hvordan ting "alltid har blitt gjort". Andre utfordringer kan f.eks. være:

- Endring i kultur og organisasjon: Det kan være utfordrene for organisasjoner som er vant til å jobbe på en bestemt måte å endre til en tilnærming av scrum/smidig
- Godt samarbeid og kommunikasjon: Ettersom teamet selv er ansvarlig for planlegging, gjennomføring og evaluering krever dette et godt samarbeid og en god kommunikasjon mellom medlemmene av teamet
- Komplekse prosjekter: I komplekse prosjekter kan det være utfordrende å holde seg til de grunnleggende prinsippene for scrum

Selvom det er noen humper i veien er det også en del styrker, f.eks.:
- Redusere riskio og suksess: Sprints gjør det mulig å levere funksjoner til kunden tidlig og ofte, dette fører til at man får raske tilbakemeldinger som kan brukes til å forbedre produktet
- Tilfredshet: Raskere og oftere levering av funksjoner til kunden kan føre til at de kommer raskere i gang med å bruke produktet som kan føre til økt produktivitet hos kunden
- Effektivitet og produktivitet: Ettersom teamet selv er ansvarlig for planlegging, gjennomføring og evaluering kan dette bidra til økt produktivitet, som også som regel kommer med økt effektivitet

**DevOps Metodikk**
De grunnleggende prinsippene i DevOps er: *flyt, feedback og kontinuerlig forbedring*.

Flyt er et viktig prinsipp i DevOps. Flyt innebærer å levere programvare raskt og effektivt, uten unødvendige hindringer. DevOps-teamene bruker en rekke teknikker for å forbedre flyten, f.eks.:

- Iterativ/Inkrementell utvikling: DevOps-teamene utvikler programvare i små, inkrementelle trinn(sprints). Dette gjør det mulig å levere funksjoner til kunden tidlig og ofte, og å få tilbakemeldinger som kan brukes til å forbedre programvaren
- Automatisering: DevOps-teamene bruker automatisering for å redusere manuelle oppgaver og forbedre nøyaktigheten. Automatisering kan bidra til å forbedre flyten ved å gjøre det mulig å bygge, teste og distribuere programvare raskere og mer effektivt

Feedback er et annet viktig prinsipp i DevOps. Feedback innebærer å samle inn tilbakemelding fra brukere og andre interessenter for å forbedre programvaren. DevOps-teamene bruker en rekke teknikker for å samle inn feedback, f.eks.:

- Brukertesting: DevOps-teamene tester programvaren med brukere for å få tilbakemelding om hvordan den fungerer i praksis
- Kundeundersøkelser: DevOps-teamene gjennomfører spørreundersøkelser med kunder for å få tilbakemelding om deres behov og ønsker
- Statistikk: DevOps-teamene analyserer statistikk for å identifisere problemer og muligheter for forbedring

Kontinuerlig forbedring er et tredje viktig prinsipp i DevOps. Kontinuerlig forbedring innebærer å kontinuerlig identifisere og løse problemer for å forbedre programvaren. DevOps-teamene bruker en rekke teknikker for å drive kontinuerlig forbedring, f.eks.:

- Scrum: Scrum er et rammeverk for smidig utvikling som fokuserer på kontinuerlig forbedring
- Kaizen: Kaizen er en japansk filosofi for kontinuerlig forbedring
- Lean: Lean er et rammeverk for å forbedre effektiviteten og kvaliteten


Selvom DevOps metodikk kommer med sine fordeler, kan det også ha sine ulemper og utfordringer. 

*Fordeler*:
- Raskere levering av programvare: Ved å automatisere utviklings- og distribusjonsprosesser kan teamene levere nye funksjoner og oppdateringer raskere

- Forbedret samarbeid: Metodikken fremmer samarbeid mellom utviklingsteamene og driftsteamene. Dette hjelper til med å forbedre kommunikasjonen mellom teamene, noe som igjen fører til bedre forståelse og samarbeid

- Kvalitetssikring: Automatisert testing og kontinuerlig integrasjon bidrar til å oppdage feil tidlig i utviklingsprosessen. En effekt av dette er bedre kvalitet på programvaren og reduserer kostnadene og kompleksiteten knyttet til feilretting senere

- Skalerbarhet og fleksibilitet: DevOps gir en plattform for å automatisere skalering og oppsett. Dette gjør det enklere å håndtere varierende arbeidsbelastninger og skalere ressursene etter behov

- Kontinuerlig overvåking og forbedring: DevOps legger vekt på kontinuerlig overvåking av programvare. Dette gjør det mulig å oppdage ytelsesproblemer og feil i sanntid og raskt gjøre forbedringer

*Ulemper/Utfordringer*:
Kulturelle utfordringer: Å implementere DevOps krever ofte en kulturell endring i organisasjonen. Som med Scrum kan det være motstand blant ansatte som er vandt med å jobbe i samme rutiner over mange år

Kompleksitet: Verktøy og infrastrutur kan være komplekse å sette opp og administrere. Å finne riktig kombinasjon av verktøy og integrere dem effektivt kan være utfordrende

Sikkerhet og overholdelse: Automatisering og rask utvikling kan føre til at sikkerhet og overholdelse blir ignorert eller glemt. Det er viktig å integrere sikkerhet i hele "DevOps-løpet" for å unngå sårbarheter

Kostnader: Implementering av DevOps kan medføre investeringer i infrastruktur og opplæring. Mens det på lang sikt kan føre til kostnadsbesparelser, kan de initielle kostnadene være betydelige for enkelte organisasjoner

Feilsøking og feilretting: I en kompleks DevOps-miljø kan feilsøking og feilretting være utfordrende. Å finne kilden til problemer kan være tidkrevende og kreve spesialisert kompetanse. Man må da sørge for at flere besitter denne kompetansen hvis feil skulle oppstå.

**Sammenligning og Kontrast**
*Scrum/Smidig*:

- Programvarekvalitet: Scrum/Smidig fremmer iterativ utvikling og tett samarbeid med kunder og andre parter av interesse. Dette kan føre til bedre forståelse av krav og rask tilbakemelding, som bidrar til å forbedre programvarekvaliteten over tid. Automatisert testing og kontinuerlig integrasjon (CI) er også vanlige i Smidig-utvikling, noe som styrker kvalitetskontrollen på programvaren

- Leveransetempo: Smidig tilbyr hyppige utgivelser av funksjoner, noe som kan øke tempo av leveranse. Imidlertid kan tidsrammene variere avhengig av iterasjonens lengde og kompleksiteten til funksjonene/programvaren

*DevOps*:

- Programvarekvalitet: DevOps automatiserer hele programvareleveranseprosessen, inkludert testing og distribusjon. Dette fører til mer pålitelig programvarekvalitet da menneskelige feil og variasjoner i prosessen reduseres. Kontinuerlig overvåking og rask feilretting bidrar også til å opprettholde høy kvalitet

- Leveransetempo: DevOps er sterkt fokusert på rask og hyppig programvarelevering. Automatisering av prosesser, inkludert infrastrukturprovisjonering og implementering, gjør det mulig å akselerere leveringstiden betydelig. Dette gjør at DevOps ofte gir enda høyere leveransetempo sammenlignet med Smidig

Utviklingssituasjoner:

Valget av metodikk avhenger av prosjektets mål, kompleksitet og krav til programvareutvikling og levering. Har oppsummert grunner for Smidig og Devops under, men også lagt til et punkt der man kombinerer disse.

- Smidig: Smidig er ofte mer egnet for situasjoner der kravene er usikre eller endres hyppig. Det er spesielt verdifullt for prosjekter der kundens tilbakemelding og behov er viktig for programvareutviklingen. Smidig gir muligheten til å tilpasse seg endringer raskt

- DevOps: DevOps er ofte mest ideell for situasjoner der høy hastighet og automatisering er nødvendig. Det er spesielt gunstig for organisasjoner som ønsker å oppnå kontinuerlig levering og forbedre driftsstabilitet. DevOps er mer egnet for mer modne og stabile produkter

Kombinasjon: I mange tilfeller kan en kombinasjon av Smidig og DevOps være mest effektiv, der Smidig brukes for programvareutvikling og kravhåndtering, mens DevOps benyttes for å automatisere leveransesiden for å oppnå raskere og mer pålitelige utgivelser


# Oppsummering
Alt i alt har det vært kjempegøy. Hadde litt problematikk med de miljøvariablene, men ellers syntes jeg det har gått rimelig smertefritt. Kudos til Glenn for et utrolig gøy fag og morsomme forelesninger!
