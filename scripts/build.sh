#!/bin/bash
gradle -P version="${VERSION}" -P os="${OS}" clean ShadowJar && ls build/libs/
