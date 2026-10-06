#How to generate Docker image for Service Provider

##Ansible

You should run ansible playbook with this command:

```
ansible-playbook -i inventory.[ENV YOU ARE BUILDING THE IMAGE FOR].ini generate-sp-image.yml -e spName=[TARGET SP NAME] -e adfsName=[ADFS URL] -e tag=[TAG FOR THE DOCKER IMAGE]
```

spName, adfsName - are optional, if skipped, defaults from inventory will be used

tag - optional, if skipped "latest" will be created

**IMPORTANT: this playbook uses relative paths. Please be sure to run it from the directory generate-sp-image.yml is in.**

You will be prompted to provide your registry.apps.man.poznan.pl credentials. If you are already logged in you can comment out vars_prompt and roles: -docker-login sections.

Playbook will be run on your local host. 

Correct variables will be used and files on local disk will be changed. Please check/set proper keys and idp-metadata in files/ENV directory.
 
Docker image will be built and pushed to repository. 

##SAML Certificate
For each environment we have a pair of private/public certificate generated. 

**BUT for uwr-test and prod there are stored in keepass:** https://git.man.poznan.pl/stash/projects/LEO/repos/keepass/browse/leopasswords.kdbx

**If you want to generate the image once again, you have to copy their content to private.pem and public.pem files.**

To generate a new pair generate proper SAML Certificates. 

Run:
```
LIFETIME=3650 #validity period in days
SUBJECT_NAME='/CN=YOUR-SP-NAME' #it has to be equal to whatever do you plan to use as spName parameter while runing ansible playbook
sudo /usr/bin/openssl req -new -x509 -nodes  -newkey rsa:2048 -keyout private.pem  -days $LIFETIME -subj $SUBJECT_NAME -out public.pem
```

Full description here: https://wiki.shibboleth.net/confluence/display/CONCEPT/SAMLKeysAndCertificates to do so.

Place generated files in `files/[ENV] directory`. Preserving the file names is essential!

**Remember**

When generating the certificate set `CN name (SUBJECT_NAME)` has to be equal to `TARGET SP NAME` of your choice (`spName` variable from playbook)