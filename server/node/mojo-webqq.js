var spawn = require('child_process').spawn;
var path = require('path');

function MojoQQ(port, openqq_port) {
    this.proc = null;
    this.port = port;
    this.openqq_port = openqq_port;

    this.restart = function() {
        if (!this.running()) {
            console.log("[FFM] starting Mojo-Webqq...");

            var cmd = 'perl';
            var args = [path.resolve(__dirname, '..') + '/perl/start.pl', '--node-port=' + this.port, '--openqq-port=' + this.openqq_port];

            this.proc = spawn(cmd, args, {stdio: "inherit"});

            var mojoQQ = this;
            this.proc.on('exit', function () {
                console.log("[FFM] Mojo-Webqq exit");

                mojoQQ.proc = null;
            });

            return true;
        } else {
            console.log("[FFM] Mojo-Webqq is already running");
            return false;
        }
    };

    this.kill = function(signal) {
        if (this.running()) {
            this.proc.kill(signal);
            console.log("[FFM] killing Mojo-Webqq...");
            return true;
        } else {
            console.log("[FFM] Mojo-Webqq is already dead");
            return true;
        }
    };

    this.running = function() {
        return this.proc !== null;
    };

    this.restart();
}

module.exports = MojoQQ;
