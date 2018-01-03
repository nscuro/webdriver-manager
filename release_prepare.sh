#!/bin/bash

./mvnw -Darguments="-DskipITs" release:clean release:prepare
