[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
 [ ![Download](https://api.bintray.com/packages/cjww-development/releases/metrics-reporter/images/download.svg) ](https://bintray.com/cjww-development/releases/metrics-reporter/_latestVersion)

metrics-reporter
=================

A module to report captured metrics to graphite

To utilise this library add this to your sbt build file

```
"com.cjww-dev.libs" % "metrics-reporter_2.11" % "0.4.0" 
```

Then add this snippet in your application.conf file.

```hocon
    play.modules.enabled += "com.cjwwdev.metrics.MetricsModule"
```

Then add this snippet your routes file.

```hocon
    GET     /admin/metrics          com.cjwwdev.metrics.MetricsController
```

Display all current, captured metrics for your app on a web page in a Json format.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
