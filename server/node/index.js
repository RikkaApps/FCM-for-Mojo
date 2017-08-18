var http = require('http');
var httpProxy = require('http-proxy');
var util = require('util');
var child_process = require('child_process');
var spawn = require('child_process').spawn;
var carrier = require('carrier');

var port = 5005;
var openqq_port = 5003;
var ffm_port = 5004;

process.argv.forEach(function(val, index, array) {
    if (val.indexOf("--port=") === 0) {
        port = val.substring("--port=".length, val.length);
    } else if (val.indexOf("--ffm-port=") === 0) {
        ffm_port = val.substring("--ffm-port=".length, val.length);
    } else if (val.indexOf("--openqq-port=") === 0) {
        openqq_port = val.substring("--openqq-port=".length, val.length);
    }
});

String.prototype.format = function () {
    var args = [].slice.call(arguments);
    return this.replace(/({\d+})/g, function (a){
        return args[+(a.substr(1,a.length-2))||0];
    });
};

var perl = null;

function restart() {
    if (perl === null || perl.killed) {
        console.log("[FFM] starting mojo-webqq");

        var cmd = 'perl';
        var args = ['perl/start.pl', '--openqq-port={0}'.format(openqq_port), '--ffm-port={0}'.format(ffm_port), '--conf-file=perl/conf.json'];

        perl = spawn(cmd, args, {stdio: "inherit"});

        return true;
    } else {
        console.log("[FFM] mojo-webqq is already running");
        return false;
    }
}

restart();

process.on('exit', function () {
    console.log("[FFM] exit");
    perl.kill('SIGINT');
});

var proxy = httpProxy.createProxyServer({});

proxy.on('error', function(err, req, res) {
    res.writeHead(500, {
        'Content-Type': 'text/plain'
    });
    res.end(err.toString());
});

var server = http.createServer(function(req, res) {
    console.log(req.url);

    if (req.url === '/ffm/restart') {
        if (restart()) {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                message : 'restarted'
            }));
        } else {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                message : 'already running'
            }));
        }
    } else if (req.url === '/ffm/stop') {
        if (!perl.killed) {
            perl.kill('SIGINT');

            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                message : 'send SIGINT'
            }));
        } else {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                message : 'not running'
            }));
        }
    } else if (req.url.indexOf('/openqq') === 0) {
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

console.log('[FFM] listening ' + port);
console.log('[FFM] proxy /openqq to ' + openqq_port);
console.log('[FFM] proxy /ffm to ' + ffm_port);