#!/bin/env bash

VERSION=$(head -n1 project.clj | awk '{ print $3 }' | tr -d '"')

mkdir -p target/tarball/{opt/ipmailer,usr/lib/systemd/system}

cp "./target/uberjar/ipmailer-$VERSION-standalone.jar" "./target/tarball/opt/ipmailer/ipmailer-standalone.jar"
cp ".env" "./target/tarball/opt/ipmailer/.env"
cp "./systemd/ipmailer.service" "./target/tarball/usr/lib/systemd/system/ipmailer.service"

cd target/tarball

tar -czf ../ipmailer-$VERSION.tar.gz .

cd ../..

rm -rf ./target/tarball
