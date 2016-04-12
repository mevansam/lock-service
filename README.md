# Lock Service [![Build Status](https://travis-ci.org/mevansam/terraform-provider-bosh.svg?branch=master)](https://travis-ci.org/appbricks/lock-service)

Implements a shared lock capability that a distributed collection of micro-services can 
use to obtain exclusive access to a shared remote resource that does not natively support 
atomic operations against its API.

## Publishing to Sonatype OSS

In order to publish this project to the Sonatype open source [maven site](https://oss.sonatype.org/), you will need to 
create a key to [sign the published artifacts](http://central.sonatype.org/pages/requirements.html#sign-files-with-gpgpgp).
 
>> You need to do this only if you are publishing your own fork of this library.
   
1. Generate key and note your public key password.
   ```
   $ gpg --gen-key
   $ gpg --export-secret-key > secring.gpg
   ```
 
2. Determine you public key id.
   ```
   $ gpg --list-keys | awk '/pub /{print substr($2,length($2)-7)}'
   ```

3. Create a ```gradle.properties``` file with your credentials to the Sonatype OSS site.
   ```
   signing.keyId=<your-public-key-id>
   signing.password=<your-public-key-password>
   signing.secretKeyRingFile=secring.gpg
   ossrhUsername=<your-jira-id>
   ossrhPassword=<your-jira-password>
   ```
   
   >> Check the following link for the guide on using the signing plugin
   >> https://docs.gradle.org/current/userguide/signing_plugin.html.

4. Tar the properties and secrets files for upload to travis.
   ```
   tar cvf environment.tar gradle.properties secring.gpg
   ```

5. Install Travis CLI and add the environment archive to travis
   ```
   $ gem install travis --no-document
   $ travis encrypt-file environment.tar --add
   ```

6. Update the ```.travis.yml``` file with the correct encryption variable names when invoking openssl to decrypt the 
environment file.