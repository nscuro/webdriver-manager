#!/bin/bash

./mvnw -P ossrh -Darguments="-DskipITs" release:clean release:prepare
