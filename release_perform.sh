#!/bin/bash

./mvnw -P ossrh -Darguments="-DskipITs" release:perform
