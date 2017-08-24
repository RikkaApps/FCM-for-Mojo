var http = require('http');
var https = require('https');
var httpProxy = require('http-proxy');
var spawn = require('child_process').spawn;
var auth = require('http-auth');
var request = require('request');
var querystring = require('querystring');
var fs = require('fs');
var config = require('../config');

var perl = null;
var perlConfig = '../' + config.mojo.webqq.config_file;

process.on('exit', function () {
    // TODO push to client server exited
    console.log("[FFM] exit");
    perl.kill('SIGINT');
});

function restart() {
    if (perl === null || perl.killed) {
        console.log("[FFM] starting Mojo-Webqq, config file: " + config.mojo.webqq.config_file + ", openqq plugin port: " + config.mojo.webqq.openqq + ", ffm plugin port: " + config.mojo.webqq.ffm);

        var cmd = 'perl';
        var args = ['perl/start.pl', '--openqq-port=' + config.mojo.webqq.openqq, '--ffm-port=' + config.mojo.webqq.ffm, '--conf-file=' + config.mojo.webqq.config_file];

        perl = spawn(cmd, args, {stdio: "inherit"});

        return true;
    } else {
        console.log("[FFM] Mojo-Webqq is already running");
        return false;
    }
}

function saveConfig(ffm) {
    var json = JSON.stringify(ffm);
    json = json.replace(/[\u007F-\uFFFF]/g, ""); // or Mojo will say not utf-8..
    fs.writeFile(config.mojo.webqq.config_file, json, 'utf-8', function (err) {
        if (err) console.warn("[FFM] can't save 55plugin config file, " + err + " " + process.cwd());
        console.log('[FFM] plugin config file saved');
    });

    request
        .get('http://localhost:' + config.mojo.webqq.ffm + '/ffm/reload_config')
        .on('error', function (err) {
            if (err) console.warn("[FFM] failed to request plugin to reload config, " + err);
        })
        .on('response', function (response) {
            console.log('[FFM] plugin config updated');
        });
}

restart();

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

function handle(req, res) {
    var ffm = require(perlConfig);
    switch (req.url) {
        case '/ffm/get_registration_ids':
            var ids = ffm.registration_ids;
            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify(ids));
            break;
        case '/ffm/update_registration_ids':
            handlePost(req, res);
            break;
        case '/ffm/restart':
            if (restart()) {
                res.writeHead(200, {"Content-Type": "application/json"});
                res.end(JSON.stringify({
                    code: 0
                }));
            } else {
                res.writeHead(200, {"Content-Type": "application/json"});
                res.end(JSON.stringify({
                    code: 1
                }));
            }
            break;
        case '/ffm/stop':
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
            break;
        default:
            if (req.url.indexOf('/openqq') === 0) {
                proxy.web(req, res, {
                    target: 'http://localhost:' + config.mojo.webqq.openqq + '/'
                });
            } else {
                res.writeHead(403, {
                    'Content-Type': 'text/plain'
                });
                res.end();
            }
    }
}

function handlePost(req, res) {
    if (req.method === 'POST') {
        var body = '';
    } else {
        res.writeHead(403, {
            'Content-Type': 'text/plain'
        });
        res.end();
        return;
    }

    req.on('data', function (data) {
        body += data;
    });

    req.on('end', function () {
        if (req.headers['content-type'] === "application/json") {
            body = JSON.parse(body);
        }

        onPostEnd(req, res, body);
    });
}

function onPostEnd(req, res, body) {
    var ffm = require(perlConfig);
    switch (req.url) {
        case '/ffm/update_registration_ids':
            ffm.registration_ids = body;

            saveConfig(ffm);

            res.writeHead(200, {"Content-Type": "application/json"});
            res.end(JSON.stringify({
                code: 0
            }));
            break;
        default:
            res.writeHead(403, {
                'Content-Type': 'text/plain'
            });
            break;
    }
}

var requestListener = function(req, res) {
    console.log("[FFM] " + req.url);

    handle(req, res);
};

var httpsOptions;
if (config.https !== undefined) {
    httpsOptions = config.https;
    console.log('[FFM] https configuration found');
} else {
    console.log('[FFM] no https configuration found');
}

var server;
if (config.basic_auth === undefined) {
    console.log('[FFM] no basic auth configuration found');
    if (httpsOptions !== undefined) {
        server = https.createServer(httpsOptions, requestListener);
    } else {
        server = http.createServer(requestListener);
    }
} else {
    console.log('[FFM] using basic auth, passwd file: ' + config.basic_auth.file);
    var basic = auth.basic(config.basic_auth);
    if (httpsOptions !== undefined) {
        server = https.createServer(basic, httpsOptions, requestListener);
    } else {
        server = http.createServer(basic, requestListener);
    }
}

if (config.hostname === undefined) {
    server.listen(config.port);
    console.log('[FFM] listening ' + config.port);
} else {
    server.listen(config.port, config.hostname);
    console.log('[FFM] listening ' + config.port + ", " + config.hostname);
}