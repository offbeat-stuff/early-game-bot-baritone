FROM gitpod/workspace-full

RUN sudo apt-get -q update
RUN sudo apt -y install clang-format

RUN bash -c "/home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk update \
    && sdk install java 17.0.2-zulu \
    && sdk default java 17.0.2-zulu && sdk install scala"
