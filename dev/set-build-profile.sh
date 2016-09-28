#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This script reads specification of the chosen profile from sbt config
# and exports variables with versions of the dependencies, e.g:
# SPARK_VERSION=2.0.0

set -e

if [[ "$#" -ne 1 ]]; then
  echo "Usage: set-build-profile.sh <build-profile>"
  exit 1
fi

set -a
eval `sbt --warn --error 'set showSuccess := false' -Dbuild.profile=$1 printProfile | grep '.*_VERSION=.*'`
set +a