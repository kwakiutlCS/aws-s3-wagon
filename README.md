![Maven central](https://maven-badges.herokuapp.com/maven-central/io.github.kwakiutlcs/aws-s3-wagon/badge.png?style=flat)


# aws-s3-wagon
Wagon that allows communicating Maven and AWS S3, to enable artifacts to be published in S3 buckets.

## Usage
### Deploying artifacts in the S3 bucket
In the build section of the pom file

```
<build>
  ...
  <extensions>
    <extension>
      <groupId>io.github.kwakiutlcs</groupId>
      <artifactId>aws-s3-wagon</artifactId>
      <version>0.2.3</version>
   </extension>
  </extensions>
  ...
</build>
```

In the distribution management of the pom file
```
<distributionManagement>
  <repository>
    <id>s3.release</id>
    <url>s3://[S3 Bucket Name]/release</url>
  </repository>
  <snapshotRepository>
    <id>s3.snapshot</id>
    <url>s3://[S3 Bucket Name]/snapshot</url>
  </snapshotRepository>
  <site>
    <id>s3.site</id>
    <url>s3://[S3 Bucket Name]/site</url>
  </site>
</distributionManagement>
````
`mvn deploy` will deploy the artifact in the S3 bucket.
`mvn site-deploy` will deploy the maven site in the S3 bucket.

### Using the artifacts as dependencies in another project
Besides adding the extension to the build section, as in the deploy artifact section, it is necessary to specify the repository
```
<repositories>
  <repository>
    <id>repo-id</id>
    <url>s3://[S3 Bucket Name]/release</url>
  </repository>
</repositories>
```

## Credentials
Credentials can be supplied as described [here](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html#credentials-chain).

