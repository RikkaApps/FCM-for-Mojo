var fs = require('fs');

function FFMConfig(file) {
    var self = this;

    this.file = file;
    this.data = require('../' + file);

    console.log('[FFM] client config file: ' + file);

    this.makeIds = function () {
        self.ids = [];

        var registration_ids = this.data.registration_ids;
        if (registration_ids !== undefined) {
            for (var i = 0; i < registration_ids.length; i++) {
                self.ids.push(registration_ids[i]['id']);
            }
        }

        //console.log('[FFM] new ids: ' + self.ids);
    };

    this.save = function () {
        self.makeIds();

        var json = JSON.stringify(self.data);
        fs.writeFile(self.file, json, 'utf-8', function (err) {
            if (err) console.warn("[FFM] can't save plugin config file, " + err + " " + process.cwd());
            console.log('[FFM] plugin config file saved');
        });
    };

    this.makeIds();

    this.data.notifications = this.data.notifications || {
        friend: true,
        group: false
    };

    this.data.registration_ids = this.data.registration_ids || [];

    //console.log('[FFM] ids: ' + this.ids);
}

module.exports = FFMConfig;