# Log4jDetector

[![Build Status](https://app.travis-ci.com/theque5t/Log4jDetector.svg?branch=main)](https://app.travis-ci.com/github/theque5t/Log4jDetector)

Runnable jar that that detects if [Log4j](https://www.google.com/search?q=log4j) is in use within existing JVMs

![Demo](./docs/assets/demo.gif)

## Disclaimer

Please read the [license](./LICENSE). This is a work in progress. Best effort was made to test the software, but it is not guaranteed to work perfectly in all contexts. 

```
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## How does it work?
1. Searches for targeted JVMs using target pattern (regex)
2. Scan each JVM matching the target pattern by:
    a. Attaching to the JVM
    b. Loading an agent into the JVM that scans for Log4j classes in use
    c. Logging what it's doing and what it's finding
    d. Detach from the JVM
3. Wait until the next scan interval
4. Repeat steps 1-3 until exited 

## Build

```sh
gradle clean ShadowJar
```

## Requirements

Log4jDetector requires the following to run:

- JDK 8+

### Operating System

- Linux: All releases are currently for use on Linux.
- Windows: Work In Progress. Currently not for use on Windows.

## Installation

1. Download the runnable [jar](https://github.com/theque5t/Log4jDetector/releases) to system that needs to be scanned.
```sh
wget "https://github.com/theque5t/Log4jDetector/releases/download/v1.0.0-alpha.1/Log4jDetector-v1.0.0-alpha.1.jar"
```

## Usage

### Variables

The following __environment variables__ can be set before running the jar:

- `LOG4J_DETECTOR_LOG_PATH`: Sets the log path for the detector to log to. 
    - Type: File Path
    - Required: `No`
    - Default: `System.getProperty("user.dir")` (aka: Current user directory)
- `LOG4J_DETECTOR_JVM_TARGET_PATTERN`: Set's the JVM target matching pattern (regex)
    - Type: Regular Expression
    - Required: `No`
    - Default: `.*` (aka: __All__ JVMs)

### Scenario: Scan all JVMs

1. Run the detector
```sh
java -jar /path/to/Log4jDetector.jar
```

### Scenario: Scan specific JVMs

1. Set environment variable `LOG4J_DETECTOR_JVM_TARGET_PATTERN` equal to target pattern
```sh
export LOG4J_DETECTOR_JVM_TARGET_PATTERN=".*MyApp.jar.*"
```
2. Run the detector
```sh
java -jar /path/to/Log4jDetector.jar
```

### Scenario: Set specific log path

1. Set environment variable `LOG4J_DETECTOR_LOG_PATH` equal to file path to write logs in
```sh
export LOG4J_DETECTOR_LOG_PATH="/var/log"
```
2. Run the detector
```sh
java -jar /path/to/Log4jDetector.jar
```

### Logs

The detector implements Log4j for it's own logging.

#### Logging Format

See [log4j2.properties](./src/main/resources/log4j2.properties) file.

#### Logging Scans

Each scan is tagged with an id. The log entries associated to the scan are prefixed with the id. For example, the value `be30856f0d4148149907787f344c804f` in the log below is the scan id. This helps facilitate grouping log entries in various log aggregators (e.g. [Splunk® Transaction](https://docs.splunk.com/Documentation/Splunk/latest/SearchReference/Transaction))

```
2021-12-16T02:47:03,503-0600 INFO [Loader.run:49] - [be30856f0d4148149907787f344c804f] Scan start
2021-12-16T02:47:03,504-0600 INFO [Loader.run:56] - [be30856f0d4148149907787f344c804f] Searching for JVMs...
2021-12-16T02:47:03,585-0600 INFO [Loader.run:59] - [be30856f0d4148149907787f344c804f] Found JVM: Log4jTestApp.jar
2021-12-16T02:47:03,586-0600 INFO [Loader.run:59] - [be30856f0d4148149907787f344c804f] Found JVM: com.github.badsyntax.gradle.GradleServer 63727
2021-12-16T02:47:03,588-0600 INFO [Loader.run:64] - [be30856f0d4148149907787f344c804f] Found JVM matches target pattern:
2021-12-16T02:47:03,589-0600 INFO [Loader.run:65] - [be30856f0d4148149907787f344c804f] JVM Id: 32572
2021-12-16T02:47:03,590-0600 INFO [Loader.run:66] - [be30856f0d4148149907787f344c804f] JVM Display Name: Log4jTestApp.jar
2021-12-16T02:47:03,590-0600 INFO [Loader.run:68] - [be30856f0d4148149907787f344c804f] Attaching to the JVM...
2021-12-16T02:47:03,600-0600 INFO [Loader.run:71] - [be30856f0d4148149907787f344c804f] Loading agent into JVM...
2021-12-16T02:47:03,665-0600 INFO [Loader.run:74] - [be30856f0d4148149907787f344c804f] Returning agent results:
2021-12-16T02:47:03,669-0600 INFO [Loader.run:76] - [be30856f0d4148149907787f344c804f] 
Detected log4j class loaded: 
Class Name: org.apache.logging.log4j.core.util.BasicAuthorizationProvider$$Lambda$53/0x0000000800115040
Class Location: <SENSORED>/Log4jTestApp/log4j-2.13.3/log4j-core.jar
Package Name: org.apache.logging.log4j.core.util
Package Specification Title: Apache Log4j Core
Package Specification Vendor: The Apache Software Foundation
Package Specification Version: 2.13.3
Package Implementation Title: Apache Log4j Core
Package Implementation Vendor: The Apache Software Foundation
Package Implementation Version: 2.13.3
```

#### Logging Detections

When a detection occurs, a log entry like the following will occur:

```
...
Detected log4j class loaded: 
Class Name: <class name>
Class Location: <class location>
Package Name: <package name>
Package Specification Title: <package specification title>
Package Specification Vendor: <package specification vendor>
Package Specification Version: <package specification version>
Package Implementation Title: <package implementation title>
Package Implementation Vendor: <package implementation vendor>
Package Implementation Version: <package implementation version>
...
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to test as appropriate.

## License
[MIT License](LICENSE)

## Acknowledgements

Log4jDetector makes use of the open source projects listed on the [index.md](build/reports/index.md) in the build/reports directory. [Click here](build/reports/index.md) to be automatically redirected to the [index.md](build/reports/index.md).

## Donations

If you'd like to support the development of future projects, or say thanks for this one, you can donate ADA at the following address: `addr1q9xy47gj8nel06azt5mrlr2zwcut9d2m49xp8wmkmuwuml8y0xfqqy3cz50ly3xjc6z7g7cnhta4z9yansz6qzeq7kxstav289`

---

[![Follow on Github](https://img.shields.io/static/v1?label=Follow&message=theque5t&logo=github)](https://github.com/theque5t)
