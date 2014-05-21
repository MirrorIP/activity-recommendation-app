# Activitiy Recommendation App
The Activity Recommendation App is a tool to create Recommendations and Capture Experiences. It is a web application using the XMPP protocol to perform all operations.

## Build
To build the application an build script for [Apache Ant][1] is provided.

The Application is written with [GWT][2]. It is therefore necessary that you change the 'gwt.sdk' to your GWT installation.

## Configure
The application is configured over the file `properties.js` in the public directory. The following parameters are available:

* **domain**
The XMPP domain to use, e.g. `mirror-demo.eu`.
* **httpbind**
HTTP binding (BOSH) interface to use. You can either bind on the BOSH port of the Openfire server directly using [CORS][3] (since Openfire 3.8) or on a reverse proxy on the web server the application is deployed on. Both absolute and relative URLs are allowed, e.g. `http://mydomain.com/http-bind/` or `/http-bind/`.
*NOTE:* For compatibiliy reasons (browser support, firewall settings) it is recommended to use a reverse proxy as binding interfave.
* **enablechat**
The application provides a feature to chat with other people. By setting this variable to false, it can be disabled.

Furthermore the application uses the mirror-fileservice to append files as evidences. This can be configured in the web.xml. If you don't want to use this feature remove all init-parameter.

## Install
The `war` file can be renamed and deployed on any web server which can deliver static files. We recommend to use a web server capable of reverse proxying, e.g., [Apache HTTPD][4] or [nginx][5].

## Usage
Upon the URL in the web browser. The Activity Recommendation App allows the management of recommendations and experiences: Any user registered at the XMPP network may log in using his/her XMPP user creadential, i.e. user id and password.

## License
The MIRROR Space Manager is provided under the Apache [License 2.0][6].
License information for third party libraries is provided with the JS files.

  [1]: http://ant.apache.org/
  [2]: http://www.gwtproject.org/
  [3]: http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing
  [4]: http://httpd.apache.org/
  [5]: http://nginx.org/
  [6]: http://www.apache.org/licenses/LICENSE-2.0.html