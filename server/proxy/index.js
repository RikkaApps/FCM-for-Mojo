var http = require('http');
var httpProxy = require('http-proxy');

var port = 5005;
var openqq_port = 5003;
var ffm_port = 5004;

process.argv.forEach(function(val, index, array) {
    if (val.indexOf("--port=") === 0) {
        port = val.substring("--port=".length, val.length);
    } else if (val.indexOf("--ffm-port=") === 0) {
        ffm_port = val.substring("--ffm-port=".length, val.length);
    } else if (val.indexOf("--openqq-port=") === 0) {
        openqq_port = val.substring("--openqq-port".length, val.length);
    }
});

var proxy = httpProxy.createProxyServer({});

proxy.on('error', function(err, req, res) {
    res.writeHead(500, {
        'Content-Type': 'text/plain'
    });
    res.end(err);
});

var server = http.createServer(function(req, res) {
    console.log(req.url);
    if (req.url.indexOf('/openqq') === 0) {
        proxy.web(req, res, {
            target: 'http://localhost:' + openqq_port + '/'
        });
    } else if (req.url.indexOf('/ffm') === 0) {
        proxy.web(req, res, {
            target: 'http://localhost:' + ffm_port + '/'
        });
    } else {
        res.writeHead(403, {
            'Content-Type': 'text/plain'
        });
        res.end();
    }
});

server.listen(port);

console.log('FFMW: listening ' + port);
console.log('FFMW: proxy /openqq to ' + openqq_port);
console.log('FFMW: proxy /ffm to ' + ffm_port);