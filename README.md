# Couchbase Importer #

Example Akka HTTP service for uploading [Protein XML](http://aiweb.cs.washington.edu/research/projects/xmltk/xmldata/data/pir/psd7003.xml).
Supports Jenkins Blue Ocean for building with Jenkinsfile.

Example cURL command:
```
curl -i -X POST http://localhost:8080/import/xml -H "Content-Type: text/xml" --data-binary "@/Users/cosmic/tmp/psd7003.xml"
```
 
Tech Stack:
* [SBT](http://www.scala-sbt.org/) build
* [Scala 2.12](https://www.scala-lang.org/)
* [Akka](http://akka.io/) with [Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html)
 and [Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/index.html)
* [Swagger](https://swagger.io/)


## Jenkinsfile
See: [Using a Jenkinsfile](https://jenkins.io/doc/book/pipeline/jenkinsfile/)
Documentation also available in a running Jenkins instance:
[http://localhost:8080/pipeline-syntax/](http://localhost:8080/pipeline-syntax/).


## SBT Development Environment ##

    sbt run


## SBT Release Command ##

    sbt clean assembly
