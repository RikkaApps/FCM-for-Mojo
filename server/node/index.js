var http = require('http');
var https = require('https');
var fs = require('fs');
var httpProxy = require('http-proxy');
var spawn = require('child_process').spawn;
var auth = require('http-auth');
var conf = require('../config.json');

var perl = null;

function restart() {
    if (perl === null || perl.killed) {
        console.log("[FFM] starting Mojo-Webqq, config file: " + conf.mojo.webqq.config_file + ", openqq plugin port: " + conf.mojo.webqq.openqq + ", ffm plugin port: " + conf.mojo.webqq.ffm);

        var cmd = 'perl';
        var args = ['perl/start.pl', '--openqq-port=' + conf.mojo.webqq.openqq, '--ffm-port=' + conf.mojo.webqq.ffm, '--conf-file=' + conf.mojo.webqq.config_file];

        perl = spawn(cmd, args, {stdio: "inherit"});

        return true;
    } else {
        console.log("[FFM] Mojo-Webqq is already running");
        return false;
    }
}

restart();

process.on('exit', function () {
    // TODO push to client server exited
    console.log("[FFM] exit");
    perl.kill('SIGINT');
});

var proxy = httpProxy.createProxyServer({
    proxyTimeout: 3000
});

proxy.on('error', function(err, req, res) {
    if (err.code === "ECONNREFUSED") {
        res.writeHead(502, {
            'Content-Type': 'text/plain'
        });
        res.end('webqq dead');
    } else {
        res.writeHead(502, {
            'Content-Type': 'text/plain'
        });
        res.end('webqq error');
    }
});

var requestListener = function(req, res) {
    console.log("[FFM] " + req.url);

    if (req.url === '/ffm/restart') {
        if (restart()) {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                code: 0
                //message : 'restarted'
            }));
        } else {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                code: 1
                //message : 'already running'
            }));
        }
    } else if (req.url === '/ffm/stop') {
        if (!perl.killed) {
            perl.kill('SIGINT');

            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                code: 0
                //message : 'send SIGINT'
            }));
        } else {
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                code: 1
                //message : 'not running'
            }));
        }
    } else if (req.url.indexOf('/openqq') === 0) {
        proxy.web(req, res, {
            target: 'http://localhost:' + conf.mojo.webqq.openqq + '/'
        });
    } else if (req.url.indexOf('/ffm') === 0) {
        proxy.web(req, res, {
            target: 'http://localhost:' + conf.mojo.webqq.ffm + '/'
        });
    } else {
        res.writeHead(403, {
            'Content-Type': 'text/plain'
        });
        res.end();
    }
};

var httpsOptions;
if (conf.https !== undefined) {
    if (conf.https.key !== undefined && conf.https.cert !== undefined) {
        httpsOptions = {
            key: fs.readFileSync(conf.https.key),
            cert: fs.readFileSync(conf.https.cert)
        };
        console.log('[FFM] using https, key: ' + conf.https.key + ', cert: ' + conf.https.key);
    } else if (conf.https.pfx !== undefined && conf.https.passphrase !== undefined) {
        httpsOptions = {
            pfx: fs.readFileSync(conf.https.pfx),
            passphrase: conf.https.passphrase
        };
        console.log('[FFM] using https, pfx: ' + conf.https.pfx + ', passphrase: ' + conf.https.passphrase);
    } else {
        console.log('[FFM] https configuration found, but wrong');
    }
} else {
    console.log('[FFM] no https configuration found');
}

var server;
if (conf.basic_auth === undefined) {
    console.log('[FFM] no basic auth configuration found');
    if (httpsOptions !== undefined) {
        server = https.createServer(httpsOptions, requestListener);
    } else {
        server = http.createServer(requestListener);
    }
} else {
    console.log('[FFM] using basic auth, passwd file: ' + conf.basic_auth.file);
    var basic = auth.basic(conf.basic_auth);
    if (httpsOptions !== undefined) {
        server = https.createServer(basic, httpsOptions, requestListener);
    } else {
        server = http.createServer(basic, requestListener);
    }
}

if (conf.hostname === undefined) {
    server.listen(conf.port);
    console.log('[FFM] listening ' + conf.port);
} else {
    server.listen(conf.port, conf.hostname);
    console.log('[FFM] listening ' + conf.port + ", " + conf.hostname);
}