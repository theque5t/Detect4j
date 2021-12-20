#!/bin/bash
gradle -P version="${VERSION}" clean ShadowJar && ls build/libs/
