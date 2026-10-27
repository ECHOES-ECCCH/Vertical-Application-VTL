# Virtual Transcription Laboratory Portal - IIIF Tools

Interface for tools accompanied with IIPImage Server (e.g. vipslib) working on IIIF Server

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
## POD probe
* Liveness probe:
    GET `/.well-known/live`  

* Readiness probe:
    GET`/.well-known/ready`
  
  BODY: 
  ```
  {
    "threads": 18,
    "taskCounter": 0,
    "busy": false
  }
  ```   
    * threads - number of threads
    * taskCounter - number of running tasks
    * busy - is busy, currently defined as `taskCounter > 0`

## Running application to development
Requirements in addition to docker:

```
java 11
maven 3.6.0
libvips 8.7.4-1 (https://libvips.github.io/libvips/)
IntelliJ / Eclipse with Lombok (https://projectlombok.org/)
```

Run:

```
In IntelliJ / Eclipse run pl.psnc.vlt.iiif.VTLIIIFTools
```

