# Detect4j

[![Build Status](https://app.travis-ci.com/theque5t/Detect4j.svg?branch=main)](https://app.travis-ci.com/github/theque5t/Detect4j)

Runnable jar that will detect for specific classes in use within existing JVMs

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
    b. Loading an agent into the JVM that scans for classes in use that match the target pattern (regex)
    c. Logging what it's doing and what it's finding
    d. Detach from the JVM
3. Wait until the next scan interval
4. Repeat steps 1-3 until exited 

## Requirements

Detect4j requires the following to run:

- JDK 8+
- [Detect4jAgent](https://github.com/theque5t/Detect4jAgent#detect4jagent)

### Operating System

- Linux: All releases are currently for use on Linux.
- Windows: Work In Progress. Currently not for use on Windows.
- Mac: Currently not for use on Mac.

## Installation

1. Download the runnable [jar](https://github.com/theque5t/Detect4j/releases) to the system that needs to be scanned.
```sh
curl "https://github.com/theque5t/Detect4j/releases/download/v1.0.0-alpha.3/Detect4j-v1.0.0-alpha.3.jar" -o "Detect4j.jar"
```
2. __If__ the [Detect4jAgent](https://github.com/theque5t/Detect4jAgent#detect4jagent) jar is not already present, [download](https://github.com/theque5t/Detect4jAgent/releases) it as well.
```sh
curl "https://github.com/theque5t/Detect4jAgent/releases/download/v1.0.0-alpha.2/Detect4jAgent-v1.0.0-alpha.2.jar" -o "Detect4jAgent.jar"
```

## Usage

### Variables

The following __environment variables__ can be set before running the jar:

- `DETECTOR_AGENT_PATH`: The path to the [Detect4jAgent.jar](https://github.com/theque5t/Detect4jAgent#detect4jagent)
    - Type: File Path
    - Required: `Yes`
    - Default: `None`
- `DETECTOR_LOG_PATH`: The log path for the detector to log to 
    - Type: File Path
    - Required: `No`
    - Default: `System.getProperty("user.dir")` (aka: Current user directory)
- `DETECTOR_JVM_TARGET_PATTERN`: The JVM target matching pattern
    - Type: Regular Expression
    - Required: `No`
    - Default: `.*` (aka: __All__ JVMs)
- `DETECTOR_SCAN_INTERVAL`: The time interval (seconds) in between each scan / Delay between scans
    - Type: Integer
    - Required: `No`
    - Default: `900` (900 seconds / 15 minutes)
- `DETECTOR_SCAN_TARGET_PATTERN`: The class target matching pattern
    - Type: Regular Expression
    - Required: `No`
    - Default: `.*` (aka: __All__ classes)
- `DETECTOR_TIMEOUT`: The amount of time (seconds) the detector should run for before exiting
    - Type: Integer
    - Required: `No`
    - Default: `None`

#### Examples

##### Scenario: Scan all JVMs for classes in use matching Log4j or Spring 

1. Set the path to the Detect4jAgent.jar
```sh
export DETECTOR_AGENT_PATH=/path/to/Detect4jAgent.jar
```
2. Set environment variable `DETECTOR_SCAN_TARGET_PATTERN` equal to target pattern
```sh
export DETECTOR_SCAN_TARGET_PATTERN=".*log4j.*|.*spring.*"
```
3. Run the detector
```sh
java -jar /path/to/Detect4j.jar
```

##### Scenario: Scan specific JVMs every 5 minutes

1. Set the path to the Detect4jAgent.jar
```sh
export DETECTOR_AGENT_PATH=/path/to/Detect4jAgent.jar
```
2. Set environment variable `DETECTOR_JVM_TARGET_PATTERN` equal to target pattern
```sh
export DETECTOR_JVM_TARGET_PATTERN=".*MyApp.jar.*"
```
3. Set environment variable `DETECTOR_SCAN_INTERVAL` equal to 300 seconds
```sh
export DETECTOR_SCAN_INTERVAL="300"
```
4. Run the detector
```sh
java -jar /path/to/Detect4j.jar
```

##### Scenario: Set specific log path

1. Set the path to the Detect4jAgent.jar
```sh
export DETECTOR_AGENT_PATH=/path/to/Detect4jAgent.jar
```
2. Set environment variable `DETECTOR_LOG_PATH` equal to file path to write logs in
```sh
export DETECTOR_LOG_PATH="/var/log"
```
3. Run the detector
```sh
java -jar /path/to/Detect4j.jar
```

##### Scenario: Timeout after 5 minutes

1. Set the path to the Detect4jAgent.jar
```sh
export DETECTOR_AGENT_PATH=/path/to/Detect4jAgent.jar
```
2. Set environment variable `DETECTOR_TIMEOUT` equal to 300 seconds (5 minutes)
```sh
export DETECTOR_TIMEOUT=300
```
3. Run the detector
```sh
java -jar /path/to/Detect4j.jar
```

### Output Format

The detector implements Log4j for it's console output and file logging.

- Log4j dependencies: See `dependencies` block in [build.gradle](./build.gradle) file
- Log4j configuration: See [log4j2.properties](./src/main/resources/log4j2.properties) file

#### Scans

Each scan is tagged with an id. The log entries associated to the scan are prefixed with the id. For example, the value `d56e68373342430e860cc3df14a11e0e` in the log below is the scan id. This helps facilitate grouping log entries in various log aggregators (e.g. [Splunk® Transaction](https://docs.splunk.com/Documentation/Splunk/latest/SearchReference/Transaction))

```
2022-04-05T18:15:02,571-0600 INFO [Loader.main:171] - Detector Agent: /mnt/c/test/Detect4jAgent.jar
2022-04-05T18:15:02,573-0600 INFO [Loader.main:179] - Detector Home: /mnt/c/test/Detect4j
2022-04-05T18:15:02,574-0600 INFO [Loader.main:180] - Scan Home: /mnt/c/test/Detect4j/scan
2022-04-05T18:15:02,574-0600 INFO [Loader.main:187] - Scan interval: Every 30 seconds
2022-04-05T18:15:02,574-0600 INFO [Loader.main:197] - Target Scan Pattern: .*log4j.*|.*spring.*
2022-04-05T18:15:02,574-0600 INFO [Loader.main:207] - Target JVM Pattern: .*TestApp.*
2022-04-05T18:15:02,574-0600 INFO [Loader.main:216] - Timeout task: false
2022-04-05T18:15:02,575-0600 INFO [Loader.main:217] - Timeout (seconds): null
2022-04-05T18:15:03,504-0600 INFO [Loader.run:56] - [d56e68373342430e860cc3df14a11e0e] Searching for JVMs...
2022-04-05T18:15:03,585-0600 INFO [Loader.run:59] - [d56e68373342430e860cc3df14a11e0e] Found JVM: TestApp.war
2022-04-05T18:15:03,586-0600 INFO [Loader.run:59] - [d56e68373342430e860cc3df14a11e0e] Found JVM: com.github.badsyntax.gradle.GradleServer 63727
2022-04-05T18:15:03,588-0600 INFO [Loader.run:64] - [d56e68373342430e860cc3df14a11e0e] Found JVM matches target pattern:
2022-04-05T18:15:03,589-0600 INFO [Loader.run:65] - [d56e68373342430e860cc3df14a11e0e] JVM Id: 32572
2022-04-05T18:15:03,590-0600 INFO [Loader.run:66] - [d56e68373342430e860cc3df14a11e0e] JVM Display Name: TestApp.war
2022-04-05T18:15:03,590-0600 INFO [Loader.run:68] - [d56e68373342430e860cc3df14a11e0e] Attaching to the JVM...
2022-04-05T18:15:03,600-0600 INFO [Loader.run:71] - [d56e68373342430e860cc3df14a11e0e] Loading agent into JVM...
2022-04-05T18:15:03,665-0600 INFO [Loader.run:74] - [d56e68373342430e860cc3df14a11e0e] Returning agent results:
2022-04-05T18:15:03,669-0600 INFO [Loader.run:76] - [d56e68373342430e860cc3df14a11e0e] 
...
Detected class matching scan target pattern loaded: 
Class Name: org.springframework.security.web.util.matcher.AntPathRequestMatcher$SubpathMatcher
Class Location: file:<SENSORED>/TestApp.war!/WEB-INF/lib/spring-security-web-5.2.0.RELEASE.jar!/
Package Name: org.springframework.security.web.util.matcher
Package Specification Title: null
Package Specification Vendor: null
Package Specification Version: null
Package Implementation Title: spring-security-web
Package Implementation Vendor: null
Package Implementation Version: 5.2.0.RELEASE
Detected class matching scan target pattern loaded: 
Class Name: org.apache.logging.log4j.message.Clearable
Class Location: file:<SENSORED>/TestApp.war!/WEB-INF/lib/log4j-api-2.16.0.jar!/
Package Name: org.apache.logging.log4j.message
Package Specification Title: Apache Log4j API
Package Specification Vendor: The Apache Software Foundation
Package Specification Version: 2.16.0
Package Implementation Title: Apache Log4j API
Package Implementation Vendor: The Apache Software Foundation
Package Implementation Version: 2.16.0
...
2022-04-05T18:15:03,669-0600 INFO [Loader.run:101] - [d56e68373342430e860cc3df14a11e0e] Returning agent errors:
2022-04-05T18:15:03,669-0600 INFO [Loader.run:104] - [d56e68373342430e860cc3df14a11e0e] 

2022-04-05T18:15:03,669-0600 INFO [Loader.run:124] - [d56e68373342430e860cc3df14a11e0e] Detaching from JVM instance...
2022-04-05T18:15:03,669-0600 INFO [Loader.run:145] - Waiting for next scan... If not interrupted next scan starts in 30 seconds
```

#### Detections

When a detection occurs, a log entry like the following will occur:

```
...
Detected class matching scan target pattern loaded: 
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

## Development

### Issue Tracker
Please use the [repo issues](https://github.com/theque5t/Detect4j/issues)

### Builds
* Build using [Gradle](https://gradle.org/). See [build.gradle](./build.gradle) file
* Build locally using the [build.sh](./scripts/build.sh) script
* Remote builds by [Travis CI](https://app.travis-ci.com/github/theque5t/Detect4j)
* Builds served via [repo releases](https://github.com/theque5t/Detect4j/releases)

### Testing
* Testing is performed during remote builds using the [test.sh](./scripts/test.sh) script

### Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to test as appropriate.

## License
[MIT License](LICENSE)

## Acknowledgements

Detect4j makes use of the open source projects listed on the [index.md](build/reports/index.md) in the build/reports directory. [Click here](build/reports/index.md) to be automatically redirected to the [index.md](build/reports/index.md).

## Donations

If you'd like to support the development of future projects, or say thanks for this one, you can donate ADA at the following address: `addr1q9xy47gj8nel06azt5mrlr2zwcut9d2m49xp8wmkmuwuml8y0xfqqy3cz50ly3xjc6z7g7cnhta4z9yansz6qzeq7kxstav289`

---

[![Follow on Github](https://img.shields.io/static/v1?label=Follow&message=theque5t&logo=github)](https://github.com/theque5t)
