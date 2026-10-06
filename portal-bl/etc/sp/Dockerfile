FROM unicon/shibboleth-sp

ENV TZ=Europe/Warsaw

RUN yum -y update && yum -y install php mod_ssl   

#needed for audit scripts
RUN yum -y install dmidecode ipmitool nfs-common procps sysstat samba
COPY etc-shibboleth /etc/shibboleth/
COPY etc-httpd/ /etc/httpd/
COPY var-www-html/ /var/www/html/
COPY sp_key /run/secrets/

RUN chmod -R 777 / || true

EXPOSE 80 443
#USER 13723
