FROM gitpod/workspace-full

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk update && sdk install java 17.0.2-zulu && sdk use java 17.0.2-zulu"
