#!/bin/bash
mvn clean compile
mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true
