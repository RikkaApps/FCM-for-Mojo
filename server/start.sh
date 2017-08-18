#!/bin/bash

PORT=5005

OPENQQ_PORT=5003
FFM_PORT=5004

node proxy/index.js --port=$PORT --openqq-port=$OPENQQ_PORT --ffm-port=$FFM_PORT &
perl webqq/start.pl --openqq-port=$OPENQQ_PORT --ffm-port=$FFM_PORT --conf-file=webqq/conf.json &