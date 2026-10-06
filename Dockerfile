#build
FROM node:18-alpine as build

RUN apk update && apk add --no-cache git

WORKDIR /usr/src/app

COPY package.json yarn.lock ./

RUN yarn install --frozen-lockfile && yarn cache clean

ENV PATH /usr/src/app/node_modules/.bin:${PATH}

COPY . .

RUN export $(cat .env.docker | xargs) && \
    yarn build

RUN \
 echo "{"  > /tmp/buildinfo.json && \
 echo "\"commitId\":\"$(git rev-parse --short HEAD)\""  >> /tmp/buildinfo.json && \
 echo ","  >> /tmp/buildinfo.json && \
 echo "\"buildDate\":\"$(date +%s)\""  >> /tmp/buildinfo.json && \
 echo "}"  >> /tmp/buildinfo.json 


# serve
FROM nginx:1.19.0-alpine

ENV TZ=Europe/Warsaw

#needed for audit scripts
#RUN apk add dmidecode ipmitool nfs-utils procps sysstat samba bash

COPY --from=build /usr/src/app/build/ /tmp/buildinfo.json /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN chgrp -R 0 / 2> /dev/null || true && \
    chmod -R g=u / 2> /dev/null || true


EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
