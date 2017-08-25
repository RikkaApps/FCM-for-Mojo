var spawn = require('child_process').spawn;

function MojoQQ(port, openqq_port) {
    this.proc = null;
    this.port = port;
    this.openqq_port = openqq_port;

    this.restart = function() {
        if (this.proc === null || this.proc.killed) {
            console.log("[FFM] starting Mojo-Webqq...");

            var cmd = 'perl';
            var args = ['perl/start.pl', '--node-port=' + this.port, '--openqq-port=' + this.openqq_port];

            this.proc = spawn(cmd, args, {stdio: "inherit"});

            return true;
        } else {
            console.log("[FFM] Mojo-Webqq is already running");
            return false;
        }
    };

    this.kill = function(signal) {
        if (this.proc !== null && !this.proc.killed) {
            this.proc.kill(signal);
            console.log("[FFM] killing Mojo-Webqq...");
            return true;
        } else {
            console.log("[FFM] Mojo-Webqq is already dead");
            return true;
        }
    };

    this.restart();
}

module.exports = MojoQQ;