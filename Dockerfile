FROM mhart/alpine-node
ENV USER=rikka PASSWD=rikka
EXPOSE 5005
COPY server /data/server
RUN apk add --update --no-cache perl perl-net-ssleay wget make \
    && wget --no-check-certificate -qO- https://cpanmin.us | perl - App::cpanminus \
    && cpanm Mojo::Webqq \
    && cd /data/server/node/ \
    && npm i \
    && apk del make wget \
    && echo 'var fs = require("fs"); \
             var config = {"mojo":{"webqq":{"openqq": 5003}}, \
                           "local_port": 5004, "port": 5005, \
                           "client_config": "client.json", \
                           "basic_auth":{"file": "auth"}}; \
             module.exports = config;' > ../config.js \
    && echo "{}" > /client.json
CMD cd /data/server && echo "$USER:$PASSWD" > auth && node node/index.js