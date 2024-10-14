FROM docker.atl-paas.net/sox/buildeng/agent-baseagent:latest

ARG MAVEN_VERSION=3.9.6
ARG NODE_VERSION=20.11.0
ARG YARN_VERSION=1.22.19
ARG NODE_GYP_VERSION=10.0.1

USER root
ENV HOME /root

#install all needed dependencies for common build and testing tools for AUI, WRM, etc.
RUN apt-get update -y \
    && apt-get install --no-install-recommends -y \
      apt-utils \
      bzip2 \
      curl \
      fontconfig \
      libgtk2.0-0 \
      libgtk-3-0 \
      libgbm-dev \
      libnotify-dev \
      libgconf-2-4 \
      libnss3 \
      libxss1 \
      libasound2 \
      libxtst6 \
      libxss1 \
      vim \
      xauth \
      xmlstarlet \
      xvfb \
      zip \
    && rm -rf /var/lib/apt/lists/*

# Remobve "global" node
RUN rm /usr/bin/node \
  && rm -rf /usr/lib/node_modules

# Prebake latest maven
RUN cd $HOME \
    && echo "mvn_version=$MAVEN_VERSION" > mvnvm.properties \
    && mvn --version \
    && rm mvnvm.properties

RUN curl https://mise.run | bash \
    && echo 'eval "$(~/.local/bin/mise activate bash)"' >> ~/.bashrc