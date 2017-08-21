#!/bin/bash

PORT=5005			# port of node http server
OPENQQ_PORT=5003	# port of mojo-webqq plugin openqq
FFM_PORT=5004		# port of mojo-webqq plugin ffm

node node/index.js --port=$PORT --openqq-port=$OPENQQ_PORT --ffm-port=$FFM_PORT