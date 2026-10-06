#How to generate Docker image wlt-portal-ui

##Ansible

You should run ansible playbook with this command:

```
ansible-playbook -i inventory.[ENV YOU ARE BUILDING THE IMAGE FOR].yaml generate-ui-image.yml -e tag=[TAG NAME] --ask-vault-pass
```
vault password has to be provided 

Optionally (not recommended) skip ```--ask-vault-pass``` and run with 
```-e registryUser=[LDAP USER NAME] -e registryPassword=[LDAP USER PASSWORD]```
instead
##Jenkins 
Run https://maven.man.poznan.pl/jenkins/view/wlt/job/wlt-portal-ui/job/develop/ build.

**WARNING**: It will only generate proper image for "test" environment (hosted in Openstack), **only from develop branch!**
