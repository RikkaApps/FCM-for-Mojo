var fs = require('fs');

var config = {
    "mojo": {
        "webqq": {
            "openqq": 5003,
            "ffm": 5004,
            "config_file": "perl/conf.json"
        }
    },
    "port": 5005,
    "hostname": "",
    "basic_auth": {
        "file": ""
    },
    "https": {
        "key": fs.readFileSync("privkey.pem"),
        "cert": fs.readFileSync("fullchain.pem")
    }
}

module.exports = config;