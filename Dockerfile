FROM anapsix/alpine-java:8_jdk

# Install extra alpine packages
RUN apk update && \
  apk upgrade && \
  apk add --no-cache \
    bash \
    curl \
    git \
    openssl

# Install sbt
ARG SBT_VERSION=0.13.15
ENV SBT_HOME=/usr/local/sbt
ENV PATH="${SBT_HOME}/bin:${PATH}"

RUN ( wget -O - "https://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" | gunzip | tar -x -C $(dirname $SBT_HOME) ) \
  && mkdir -p /root/.sbt/preloaded \
  && cp -r ${SBT_HOME}/lib/local-preloaded/* /root/.sbt/preloaded

# Create app folder
RUN mkdir -p /app
WORKDIR /app

# Add app project
ADD project /app/project

# Download sbt specific dependencies
RUN sbt update

# Add app dependency definitions
ADD build.sbt /app/

# Download sbt dependencies
RUN sbt update

# Add rest of sources
ADD . /app

# Compile application
RUN sbt compile

CMD ["sbt", "run"]
