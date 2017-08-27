# FCM for Mojo
Push QQ Messages to Android devices according Firebase Cloud Message (FCM) with [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq).
Design for Android 7.0+ specially, full use Android notification feature
(Reply in notification and bundled notifications, etc.)

[简体中文](/README_zh.md)

# Docker
It's very easy to install FFM by Docker. With [kotomei's guilde](https://github.com/kotomei/FCM-for-Mojo), you could finish it in a few minutes.

# Step by Step
## Mojo-Webqq
FFM is depending on Mojo-Webqq, you need install [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) at first.

## Get Server
You need [install Node.js with npm](https://nodejs.org/en/download/package-manager) and Git at first.
And get server files from [releases](https://github.com/RikkaW/FCM-for-Mojo/releases). Then, install dependent modules runing node:

```Shell
mkdir ffm && cd ffm
# Copy server.zip download link to here:
wget <server.zip>
unzip server.zip && cd node
npm install && cd ..
node node/index.js
```

Congratulation, HTTP FFM server running now at basic mode now!
But that's not finished yet, we need something to protect your messages.

### HTTP Basic Authorization
Generate a password with openssl:

```Shell
$ openssl passwd
Password:
Verifying - Password:
<MD5>
```

Copy MD5 and create a file with:

```
<username>:<MD5>
```

Edit ```config.json```, finding the line with ```basic_auth```
and del annotation (```/*``` and ```*/```) near that's line:

```js
	"basic_auth": {
		"file": "/path/to/passwd"
	},
```

### HTTPS
Attention, you need **SSL certificates** to set up HTTPS.

Edit ```config.js```, finding the line with "```https```"
and del annotation (```/*``` and ```*/```) near that's line:

```js
	"https": {
			"key": fs.readFileSync("/path/to/privkey.pem"),
			/* Add ca-cert here if you have
			"ca": fs.readFileSync("/path/to/ca-cert.pem"), */
			"cert": fs.readFileSync("/path/to/fullchain-or-server-cert.pem")
		}
```

Run ```node node/index.js```. [download cilent](https://github.com/RikkaW/FCM-for-Mojo/releases) to  finish last configuration!

PS: You can find out more usage about the ```config.conf``` in [wiki](https://github.com/RikkaW/FCM-for-Mojo/wiki/usage-of-config).

# [License](/LICENSE)
```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
