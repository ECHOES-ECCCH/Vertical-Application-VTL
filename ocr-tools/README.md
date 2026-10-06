# Virtual Transcription Laboratory Portal - OCR Tools

Interface for tools accompanied with OCR engines (e.g. tesseract-ocr or Abbyy FineReader)

## Running application via Docker
Requirements:

```
docker : 19.03.0
docker-compose : 1.24.0
```

Run:

```
docker-compose build
docker-compose up
```

OpenAPI specification:

```
http://localhost:5557/v3/api-docs
http://localhost:5557/swagger-ui/index.html?url=http://localhost:5557/v3/api-docs
```
## POD probe
* Liveness probe:
    GET `/.well-known/live`  

* Readiness probe:
    GET`/.well-known/ready`
  
  BODY: 
  ```
  {
    "threads": 18,
    "activeOCRTasks": 0,
    "busy": false
  }
  ```   
    * threads - number of threads
    * activeOCRTasks - number of running ocr tasks
    * busy - is busy, currently defined as `activeOCRTasks > 0`

## Running application to development
Requirements in addition to docker:

```
java 11
maven 3.6.0
tesseract-ocr-all 4.0.0-2 (https://github.com/tesseract-ocr/tesseract)
IntelliJ / Eclipse with Lombok (https://projectlombok.org/)
```

Run:

```
In IntelliJ / Eclipse run pl.psnc.vlt.ocr.VTLOCRTools
```

