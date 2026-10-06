# Virtual Transcription Laboratory Portal - Business Logic

Business Logic of Virtual Transcription Laboratory Portal

## Docs
Project docs can be found [here](./docs/docs.md)

## Running application via Docker
Requirements:

```
docker : 19.03.0
docker-compose : 1.24.0
```

Run:

```
docker login gitlab.pcss.pl:5005
docker-compose down -v
docker-compose build
./docker-compose-dev.sh postgresql iiif-server iiif-tools ocr-tools ocr-training-db ocr-training-service wlt-portal-bl 
```

OpenAPI specification:

```
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/v3/api-docs
```

## Running application to development
Requirements in addition to docker:

```
java 11
maven 3.6.0
IntelliJ / Eclipse with Lombok (https://projectlombok.org/)
```

Run:

```

docker login gitlab.pcss.pl:5005
docker-compose-dev
docker-compose build
(using GitBash on Windows)
./docker-compose-dev.sh postgresql iiif-server iiif-tools ocr-tools ocr-training-db ocr-training-service 
In IntelliJ / Eclipse run pl.psnc.vlt.portal.VirtualTranscriptionLaboratory with: -Dspring.profiles.active=dev
```


 