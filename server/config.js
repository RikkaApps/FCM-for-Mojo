var fs = require('fs');

var config = {
    "mojo": {
        "webqq": {
            "openqq": 5003
        }
    },
	"local_port": 5004,
	"client_config": "client.json",
    "port": 5005,
    "hostname": "",
    "basic_auth": {
        "file": ""
    },
    "https": {
        "key": fs.readFileSync(""),
        "cert": fs.readFileSync("")
    }
}

module.exports = config;