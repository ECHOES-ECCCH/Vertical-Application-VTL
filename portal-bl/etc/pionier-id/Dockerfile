FROM unicon/shibboleth-sp

ENV TZ=Europe/Warsaw

RUN yum -y update && yum -y install php mod_ssl   

#needed for audit scripts
RUN yum -y install dmidecode ipmitool nfs-common procps sysstat samba
COPY etc-shibboleth /etc/shibboleth/
COPY etc-httpd/ /etc/httpd/
COPY var-www-html/ /var/www/html/
COPY sp_key /run/secrets/

RUN chgrp -R 0 /etc/httpd && \
    chmod -R g=u /etc/httpd && \
    chgrp -R 0 /etc/shibboleth && \
    chmod -R g=u /etc/shibboleth && \
    chgrp -R 0 /var/www/html && \
    chmod -R g=u /var/www/html && \
    chgrp -R 0 /run/secrets && \
    chmod -R g=u /run/secrets

EXPOSE 80 443
